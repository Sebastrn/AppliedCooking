package sebastrn.appliedcooking.api.cookingforblockheads.capability;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.me.helpers.MachineSource;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.cookingforblockheads.api.CacheHint;
import net.blay09.mods.cookingforblockheads.api.IngredientToken;
import net.blay09.mods.cookingforblockheads.api.KitchenItemProvider;
import net.blay09.mods.cookingforblockheads.tag.ModItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import sebastrn.appliedcooking.blockentity.KitchenStationBlockEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Supplies ingredients to Cooking for Blockheads from the Applied Energistics 2 network the Kitchen Station is
 * linked to. Implements CFB's {@link KitchenItemProvider} contract.
 * <p>
 * Three ways to satisfy a wanted ingredient, tried in order:
 * <ul>
 *     <li><b>Item</b>, the network holds the matching item directly (extract/insert as normal).</li>
 *     <li><b>Water/milk fast-path</b>, these are the fluids CFB recipes actually request, and (mirroring CFB's
 *     own Sink / Milk Jar) they're identified by item <em>tag</em> ({@link ModItemTags#WATER}/{@link ModItemTags#MILK}):
 *     if a wanted item carries the tag and the network holds ≥1000mB of the fluid, we drain a bucket and yield the
 *     requested item. Looking the fluid up by key fails fast when it isn't stored (the common case), satisfies
 *     modded tagged variants, water bottles, {@code pamhc2foodcore:freshmilkitem}, …, that the bucket-only path
 *     below can't, and makes <b>milk reliable</b> by never depending on the milk fluid's {@code getBucket()}.</li>
 *     <li><b>Fluid (network-driven fallback)</b>, for any <em>other</em> fluid stored in the network (lava, modded
 *     fluids) we make its bucket item and ask the recipe whether that satisfies the ingredient. If so we synthesize
 *     the bucket from the fluid: the container is virtual, so {@code consume()} spends 1000mB and hands back the
 *     bucket. This keeps the feature fluid-agnostic for any fluid whose {@code getBucket()} the recipe accepts.</li>
 * </ul>
 * The item path is always tried first, so real stored items are preferred over synthesized ones.
 * <p>
 * <b>Greedy mode.</b> CFB's {@code findIngredient} takes a {@code greedy} flag (added in the 26.1 API): when set,
 * a returned token reserves the <em>whole</em> available amount instead of a single item, and reports it through
 * {@link IngredientToken#reservedCount()}. CFB uses this only in {@code CraftingContext.countAvailable} to total
 * "how many can I craft" in one pass; the actual craft calls with {@code greedy == false}, so {@code consume()}
 * still spends exactly one item per call either way. Reservation accounting therefore sums {@code reservedCount()}
 * rather than counting one per token, so a greedy token correctly excludes the full amount it laid claim to.
 * <p>
 * <b>Crafting remainders belong to CFB, not to us.</b> Its crafting handler assembles the recipe and then offers
 * each remainder back through {@link IngredientToken#restore}, once per crafting-grid slot, passing {@code EMPTY}
 * for the slots that left no remainder. So {@code consume()} must not return remainders itself, and
 * {@code restore()} must key off the stack it is handed rather than assume it means "undo". CFB did neither of
 * these at 1.20.4 (its handler ignored remainders entirely), which is why this class used to do both; doing them
 * now would refund everything twice.
 * <p>
 * Performance: CFB calls {@link #findIngredient} once per (recipe × ingredient × provider), thousands of times
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
    public IngredientToken findIngredient(Ingredient ingredient, Collection<IngredientToken> ingredientTokens, CacheHint cacheHint, boolean greedy) {
        KeyCounter stacks = snapshot();
        if (stacks == null) {
            return null;
        }

        // Item cache-hint fast path.
        if (cacheHint instanceof MEIngredientToken hint) {
            long available = stacks.get(hint.key);
            if (available > 0 && ingredient.test(hint.key.toStack())) {
                long left = usesLeft(hint.key, available, ingredientTokens);
                if (left > 0) {
                    return new MEIngredientToken(hint.key, itemCount(left, greedy));
                }
            }
        }

        // Item path: look the recipe's accepted items up by key instead of scanning the whole network. This
        // matches by exact key, so an NBT-variant item stored under a different key wouldn't be found, a fine
        // trade-off for cooking ingredients (which are plain) in exchange for scaling to huge networks.
        // 26.1 dropped Ingredient.getItems(); items() yields the accepted item holders, one plain stack each.
        List<ItemStack> items = ingredient.items().map(holder -> new ItemStack(holder.value())).toList();
        for (ItemStack accepted : items) {
            AEItemKey key = AEItemKey.of(accepted);
            if (key == null) {
                continue;
            }
            long available = stacks.get(key);
            if (available > 0) {
                long left = usesLeft(key, available, ingredientTokens);
                if (left > 0) {
                    return new MEIngredientToken(key, itemCount(left, greedy));
                }
            }
        }

        // Fluid fast-paths: water and milk are requested by item tag and satisfied by draining that fluid.
        // Checked before the network-driven loop below because they're the fluids CFB recipes actually use, 
        // a cheap key lookup fails fast when the fluid isn't stored, they satisfy modded water/milk item
        // variants (bottles, freshwateritem, …) that the bucket-only loop can't, and milk is reliable this way
        // (we never depend on the milk fluid's getBucket()). Other fluids (lava, modded) fall through to the loop.
        IngredientToken water = findTaggedFluidIngredient(stacks, items, ModItemTags.WATER, Fluids.WATER, ingredientTokens, greedy);
        if (water != null) {
            return water;
        }
        IngredientToken milk = findTaggedFluidIngredient(stacks, items, ModItemTags.MILK, Balm.modSupport().milkFluid().get(), ingredientTokens, greedy);
        if (milk != null) {
            return milk;
        }

        // Fluid path: satisfy the ingredient from any other fluid stored in the network.
        return findFluidIngredient(stacks, ingredient::test, ingredientTokens, greedy);
    }

    @Override
    public IngredientToken findIngredient(ItemStack itemStack, Collection<IngredientToken> ingredientTokens, CacheHint cacheHint, boolean greedy) {
        KeyCounter stacks = snapshot();
        if (stacks == null) {
            return null;
        }

        // Item path.
        AEItemKey wanted = AEItemKey.of(itemStack);
        if (wanted != null) {
            long available = stacks.get(wanted);
            if (available > 0) {
                long left = usesLeft(wanted, available, ingredientTokens);
                if (left > 0) {
                    return new MEIngredientToken(wanted, itemCount(left, greedy));
                }
            }
        }

        // Fluid fast-paths (see the Ingredient overload): water/milk by tag before the network-driven loop.
        List<ItemStack> candidates = List.of(itemStack);
        IngredientToken water = findTaggedFluidIngredient(stacks, candidates, ModItemTags.WATER, Fluids.WATER, ingredientTokens, greedy);
        if (water != null) {
            return water;
        }
        IngredientToken milk = findTaggedFluidIngredient(stacks, candidates, ModItemTags.MILK, Balm.modSupport().milkFluid().get(), ingredientTokens, greedy);
        if (milk != null) {
            return milk;
        }

        // Fluid fallback.
        return findFluidIngredient(stacks, candidate -> ItemStack.isSameItem(candidate, itemStack), ingredientTokens, greedy);
    }

    @Override
    public CacheHint getCacheHint(IngredientToken ingredientToken) {
        return ingredientToken instanceof CacheHint hint ? hint : CacheHint.NONE;
    }

    /** How many items a freshly issued token should reserve: the whole remaining amount when greedy, else one. */
    private static int itemCount(long usesLeft, boolean greedy) {
        return greedy ? (int) Math.min(usesLeft, Integer.MAX_VALUE) : 1;
    }

    /**
     * @return how many more items of {@code key} can still be provided from the network after subtracting what the
     * tokens already handed out for this key have reserved ({@link IngredientToken#reservedCount()} each, which is
     * the full claimed amount for a greedy token, or 1 otherwise).
     */
    private long usesLeft(AEItemKey key, long available, Collection<IngredientToken> ingredientTokens) {
        long reserved = 0;
        for (IngredientToken token : ingredientTokens) {
            if (token instanceof MEIngredientToken meToken && key.equals(meToken.key)) {
                reserved += meToken.reservedCount();
            }
        }
        return available - reserved;
    }

    /**
     * Water/milk fast-path: these fluids are requested by item tag (mirroring CFB's Sink / Milk Jar). If the
     * network holds at least a bucket of {@code fluid} (after fluid already reserved this operation) and one of
     * {@code candidates} carries {@code tag}, return a token that drains a bucket and yields that requested item.
     * Yielding the requested item, not the fluid's own bucket, is what lets a modded water/milk variant (a water
     * bottle, {@code pamhc2foodcore:freshmilkitem}, …) be satisfied. Returns null (leaving the fluid to
     * {@link #findFluidIngredient}) when the fluid is absent/unregistered or no candidate carries the tag.
     */
    private IngredientToken findTaggedFluidIngredient(KeyCounter stacks, List<ItemStack> candidates, TagKey<Item> tag, Fluid fluid, Collection<IngredientToken> ingredientTokens, boolean greedy) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            return null;
        }
        AEFluidKey fluidKey = AEFluidKey.of(fluid);
        long unitsLeft = (stacks.get(fluidKey) - reservedFluid(fluidKey, ingredientTokens)) / AEFluidKey.AMOUNT_BUCKET;
        if (unitsLeft < 1) {
            return null;
        }
        for (ItemStack candidate : candidates) {
            if (candidate.is(tag)) {
                return new MEFluidIngredientToken(fluidKey, AEFluidKey.AMOUNT_BUCKET, candidate.copyWithCount(1), itemCount(unitsLeft, greedy));
            }
        }
        return null;
    }

    /**
     * For each fluid stored in the network, build its bucket item and ask {@code matches} whether that satisfies
     * the ingredient. If it does and the network holds at least a bucket (accounting for fluid already reserved),
     * return a token that synthesizes the bucket from the network fluid. This is the fluid-agnostic fallback for
     * lava and modded fluids; water and milk are handled first by {@link #findTaggedFluidIngredient}.
     */
    private IngredientToken findFluidIngredient(KeyCounter stacks, Predicate<ItemStack> matches, Collection<IngredientToken> ingredientTokens, boolean greedy) {
        for (AEFluidKey fluidKey : fluidKeys(stacks)) {
            long total = stacks.get(fluidKey);
            if (total < AEFluidKey.AMOUNT_BUCKET) {
                continue;
            }
            ItemStack bucket = new ItemStack(fluidKey.getFluid().getBucket());
            if (bucket.isEmpty() || !matches.test(bucket)) {
                continue;
            }
            long unitsLeft = (total - reservedFluid(fluidKey, ingredientTokens)) / AEFluidKey.AMOUNT_BUCKET;
            if (unitsLeft >= 1) {
                return new MEFluidIngredientToken(fluidKey, AEFluidKey.AMOUNT_BUCKET, bucket, itemCount(unitsLeft, greedy));
            }
        }
        return null;
    }

    /** Total fluid (mB) already reserved for {@code fluidKey} by the fluid tokens issued this operation. */
    private long reservedFluid(AEFluidKey fluidKey, Collection<IngredientToken> ingredientTokens) {
        long reserved = 0;
        for (IngredientToken token : ingredientTokens) {
            if (token instanceof MEFluidIngredientToken fluidToken && fluidKey.equals(fluidToken.fluidKey)) {
                reserved += (long) fluidToken.amountPerItem * fluidToken.reservedCount();
            }
        }
        return reserved;
    }

    public class MEIngredientToken implements IngredientToken, CacheHint {
        private final AEItemKey key;
        /** Items this token lays claim to for reservation accounting, the full amount when greedy, else 1. */
        private final int count;

        private MEIngredientToken(AEItemKey key, int count) {
            this.key = key;
            this.count = count;
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
            // Crafting remainders are NOT handled here: CFB hands each one back through restore(). Returning
            // them here as well would insert every remainder twice, see the class note on restore().
            return key.toStack((int) extracted);
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

        @Override
        public int reservedCount() {
            return count;
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
        /** Result items this token lays claim to (each backed by {@link #amountPerItem} mB), full amount when greedy, else 1. */
        private final int count;

        private MEFluidIngredientToken(AEFluidKey fluidKey, int amountPerItem, ItemStack resultItem, int count) {
            this.fluidKey = fluidKey;
            this.amountPerItem = amountPerItem;
            this.resultItem = resultItem;
            this.count = count;
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
                // Not enough available after all, put back whatever we drained and give up (no partial loss).
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
            // CFB calls restore() once per crafting-grid slot, passing EMPTY wherever the recipe left no
            // remainder, so an unconditional refund here would hand the fluid straight back and the ingredient
            // would never be spent at all.
            if (itemStack.isEmpty()) {
                return ItemStack.EMPTY;
            }

            MEStorage storage = getNetworkStorage();
            if (storage == null) {
                return itemStack;
            }

            if (ItemStack.isSameItemSameComponents(itemStack, resultItem)) {
                // The item consume() handed out is coming back untouched (e.g. the oven was full), so undo the drain.
                storage.insert(fluidKey, amountPerItem, Actionable.MODULATE, source());
                invalidateSnapshot();
                return ItemStack.EMPTY;
            }

            // Anything else is the recipe's remainder for the container we synthesized, the empty bucket left
            // behind by a water bucket we made out of stored fluid. That container never existed, so putting it
            // in the network would mint a bucket from nothing. Swallow it; the fluid stays spent, as it should.
            return ItemStack.EMPTY;
        }

        @Override
        public int reservedCount() {
            return count;
        }
    }
}
