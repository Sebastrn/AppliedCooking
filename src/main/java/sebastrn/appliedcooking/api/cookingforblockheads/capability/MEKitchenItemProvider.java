package sebastrn.appliedcooking.api.cookingforblockheads.capability;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.me.helpers.MachineSource;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.cookingforblockheads.api.CacheHint;
import net.blay09.mods.cookingforblockheads.api.IngredientToken;
import net.blay09.mods.cookingforblockheads.api.KitchenItemProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import sebastrn.appliedcooking.blockentity.KitchenStationBlockEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Supplies ingredients to Cooking for Blockheads from the Applied Energistics 2 network the Kitchen Station is
 * linked to. Implements CFB's {@link KitchenItemProvider} contract.
 * <p>
 * Two ways to satisfy a wanted ingredient:
 * <ul>
 *     <li><b>Item</b> — the network holds the matching item directly (extract/insert as normal).</li>
 *     <li><b>Fluid</b> — network-driven: for each fluid actually stored in the network, we make its bucket item
 *     and ask the recipe whether that satisfies the ingredient. If so, we synthesize the bucket from the fluid,
 *     mirroring CFB's own Sink: since CFB's crafting handler produces no recipe remainders, the container is
 *     virtual — {@code consume()} spends 1000mB and hands back the bucket; {@code restore()} refunds it. This is
 *     fluid-agnostic (water, lava, and any modded fluid whose {@code getBucket()} the recipe accepts). Going
 *     fluid → bucket avoids relying on the requested item exposing a fluid handler (e.g. {@code minecraft:milk_bucket}
 *     is not a fluid container).</li>
 * </ul>
 * The item path is always tried first, so real items are preferred over synthesized ones.
 * <p>
 * Performance: CFB calls {@link #findIngredient} once per (recipe × ingredient × provider) — thousands of times
 * per kitchen scan. To keep large networks from stalling, the network's available-stacks snapshot is built at
 * most once per game tick and reused across the whole burst; items are looked up by key rather than by scanning
 * the network; and the snapshot is invalidated whenever we extract/insert so multiple crafts in a tick stay
 * correct. Extraction is always validated against the real network, so a stale snapshot can never overdraw.
 */
public class MEKitchenItemProvider implements KitchenItemProvider {

    private final KitchenStationBlockEntity blockEntity;

    private KeyCounter cachedStacks;
    private List<AEFluidKey> cachedFluidKeys;
    private long cachedTick = Long.MIN_VALUE;

    public MEKitchenItemProvider(KitchenStationBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    private MEStorage getNetworkStorage() {
        return blockEntity.getNetworkStorage();
    }

    private MachineSource source() {
        return new MachineSource(blockEntity.getActionHost());
    }

    /**
     * The network's available-stacks snapshot, (re)built at most once per game tick and reused across the burst
     * of {@link #findIngredient} calls in a single kitchen scan. Invalidated by our own extract/insert. Returns
     * null when the station isn't connected to a network.
     */
    private KeyCounter snapshot() {
        MEStorage storage = getNetworkStorage();
        if (storage == null) {
            invalidateSnapshot();
            return null;
        }
        long now = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : cachedTick;
        if (cachedStacks == null || cachedTick != now) {
            cachedStacks = storage.getAvailableStacks();
            cachedFluidKeys = null;
            cachedTick = now;
        }
        return cachedStacks;
    }

    /** The fluid keys present in the current snapshot, extracted once per snapshot (fluids are few). */
    private List<AEFluidKey> fluidKeys(KeyCounter stacks) {
        if (cachedFluidKeys == null) {
            List<AEFluidKey> keys = new ArrayList<>();
            for (var entry : stacks) {
                if (entry.getKey() instanceof AEFluidKey fluidKey) {
                    keys.add(fluidKey);
                }
            }
            cachedFluidKeys = keys;
        }
        return cachedFluidKeys;
    }

    private void invalidateSnapshot() {
        cachedStacks = null;
        cachedFluidKeys = null;
    }

    @Override
    public IngredientToken findIngredient(Ingredient ingredient, Collection<IngredientToken> ingredientTokens, CacheHint cacheHint) {
        KeyCounter stacks = snapshot();
        if (stacks == null) {
            return null;
        }

        // Item cache-hint fast path.
        if (cacheHint instanceof MEIngredientToken hint) {
            long available = stacks.get(hint.key);
            if (available > 0 && ingredient.test(hint.key.toStack()) && hasUsesLeft(hint.key, available, ingredientTokens)) {
                return new MEIngredientToken(hint.key);
            }
        }

        // Item path: look the recipe's accepted items up by key instead of scanning the whole network. This
        // matches by exact key, so an NBT-variant item stored under a different key wouldn't be found — a fine
        // trade-off for cooking ingredients (which are plain) in exchange for scaling to huge networks.
        for (ItemStack accepted : ingredient.getItems()) {
            AEItemKey key = AEItemKey.of(accepted);
            if (key == null) {
                continue;
            }
            long available = stacks.get(key);
            if (available > 0 && hasUsesLeft(key, available, ingredientTokens)) {
                return new MEIngredientToken(key);
            }
        }

        // Fluid path: satisfy the ingredient from a fluid stored in the network.
        return findFluidIngredient(stacks, ingredient::test, ingredientTokens);
    }

    @Override
    public IngredientToken findIngredient(ItemStack itemStack, Collection<IngredientToken> ingredientTokens, CacheHint cacheHint) {
        KeyCounter stacks = snapshot();
        if (stacks == null) {
            return null;
        }

        // Item path.
        AEItemKey wanted = AEItemKey.of(itemStack);
        if (wanted != null) {
            long available = stacks.get(wanted);
            if (available > 0 && hasUsesLeft(wanted, available, ingredientTokens)) {
                return new MEIngredientToken(wanted);
            }
        }

        // Fluid fallback.
        return findFluidIngredient(stacks, candidate -> ItemStack.isSameItem(candidate, itemStack), ingredientTokens);
    }

    @Override
    public CacheHint getCacheHint(IngredientToken ingredientToken) {
        return ingredientToken instanceof CacheHint hint ? hint : CacheHint.NONE;
    }

    /**
     * @return true if, after accounting for the tokens already handed out for this key, at least one more item
     * of the given key can still be provided from the network.
     */
    private boolean hasUsesLeft(AEItemKey key, long available, Collection<IngredientToken> ingredientTokens) {
        long reserved = 0;
        for (IngredientToken token : ingredientTokens) {
            if (token instanceof MEIngredientToken meToken && key.equals(meToken.key)) {
                reserved++;
            }
        }
        return available - reserved > 0;
    }

    /**
     * For each fluid stored in the network, build its bucket item and ask {@code matches} whether that satisfies
     * the ingredient. If it does and the network holds at least a bucket (accounting for fluid tokens already
     * issued), return a token that synthesizes the bucket from the network fluid.
     */
    private IngredientToken findFluidIngredient(KeyCounter stacks, Predicate<ItemStack> matches, Collection<IngredientToken> ingredientTokens) {
        for (AEFluidKey fluidKey : fluidKeys(stacks)) {
            long available = stacks.get(fluidKey);
            if (available < AEFluidKey.AMOUNT_BUCKET) {
                continue;
            }
            ItemStack bucket = new ItemStack(fluidKey.getFluid().getBucket());
            if (bucket.isEmpty() || !matches.test(bucket)) {
                continue;
            }

            long reserved = 0;
            for (IngredientToken token : ingredientTokens) {
                if (token instanceof MEFluidIngredientToken fluidToken && fluidKey.equals(fluidToken.fluidKey)) {
                    reserved += fluidToken.amountPerItem;
                }
            }
            if (available - reserved >= AEFluidKey.AMOUNT_BUCKET) {
                return new MEFluidIngredientToken(fluidKey, AEFluidKey.AMOUNT_BUCKET, bucket);
            }
        }
        return null;
    }

    public class MEIngredientToken implements IngredientToken, CacheHint {
        private final AEItemKey key;

        private MEIngredientToken(AEItemKey key) {
            this.key = key;
        }

        @Override
        public ItemStack peek() {
            KeyCounter stacks = snapshot();
            if (stacks == null) {
                return ItemStack.EMPTY;
            }
            long available = stacks.get(key);
            return available > 0 ? key.toStack((int) Math.min(available, Integer.MAX_VALUE)) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack consume() {
            MEStorage storage = getNetworkStorage();
            if (storage == null) {
                return ItemStack.EMPTY;
            }
            long extracted = storage.extract(key, 1, Actionable.MODULATE, source());
            if (extracted <= 0) {
                return ItemStack.EMPTY;
            }
            invalidateSnapshot();
            ItemStack consumed = key.toStack((int) extracted);

            // Return crafting remainders (e.g. empty buckets) to the network.
            ItemStack remainder = Balm.getHooks().getCraftingRemainingItem(consumed);
            if (!remainder.isEmpty()) {
                AEItemKey remainderKey = AEItemKey.of(remainder);
                if (remainderKey != null) {
                    storage.insert(remainderKey, remainder.getCount(), Actionable.MODULATE, source());
                }
            }
            return consumed;
        }

        @Override
        public ItemStack restore(ItemStack itemStack) {
            MEStorage storage = getNetworkStorage();
            if (storage == null || itemStack.isEmpty()) {
                return itemStack;
            }
            AEItemKey insertKey = AEItemKey.of(itemStack);
            if (insertKey == null) {
                return itemStack;
            }
            long inserted = storage.insert(insertKey, itemStack.getCount(), Actionable.MODULATE, source());
            if (inserted > 0) {
                invalidateSnapshot();
            }
            if (inserted >= itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            ItemStack remainder = itemStack.copy();
            remainder.shrink((int) inserted);
            return remainder;
        }
    }

    /**
     * A container synthesized from a fluid in the ME network. The container is virtual: {@code consume()} drains
     * the fluid and yields the item; {@code restore()} refunds the fluid. Mirrors CFB's {@code SinkBlockEntity}.
     */
    public class MEFluidIngredientToken implements IngredientToken, CacheHint {
        private final AEFluidKey fluidKey;
        private final int amountPerItem;
        private final ItemStack resultItem;

        private MEFluidIngredientToken(AEFluidKey fluidKey, int amountPerItem, ItemStack resultItem) {
            this.fluidKey = fluidKey;
            this.amountPerItem = amountPerItem;
            this.resultItem = resultItem;
        }

        @Override
        public ItemStack peek() {
            KeyCounter stacks = snapshot();
            if (stacks == null) {
                return ItemStack.EMPTY;
            }
            return stacks.get(fluidKey) >= amountPerItem ? resultItem.copy() : ItemStack.EMPTY;
        }

        @Override
        public ItemStack consume() {
            MEStorage storage = getNetworkStorage();
            if (storage == null) {
                return ItemStack.EMPTY;
            }
            long extracted = storage.extract(fluidKey, amountPerItem, Actionable.MODULATE, source());
            if (extracted < amountPerItem) {
                // Not enough available after all — put back whatever we drained and give up (no partial loss).
                if (extracted > 0) {
                    storage.insert(fluidKey, extracted, Actionable.MODULATE, source());
                }
                invalidateSnapshot();
                return ItemStack.EMPTY;
            }
            // CFB produces no recipe remainders, so the container is virtual: spend the fluid, hand back the item.
            invalidateSnapshot();
            return resultItem.copy();
        }

        @Override
        public ItemStack restore(ItemStack itemStack) {
            MEStorage storage = getNetworkStorage();
            if (storage != null) {
                // Undo the fluid that consume() spent.
                storage.insert(fluidKey, amountPerItem, Actionable.MODULATE, source());
                invalidateSnapshot();
            }
            return ItemStack.EMPTY;
        }
    }
}
