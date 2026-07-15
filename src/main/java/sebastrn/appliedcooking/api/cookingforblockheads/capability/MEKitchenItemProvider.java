package sebastrn.appliedcooking.api.cookingforblockheads.capability;

import appeng.api.config.Actionable;
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

import java.util.Collection;

/**
 * Supplies ingredients to Cooking for Blockheads from the Applied Energistics 2 network the Kitchen Station is
 * linked to. Implements CFB's {@link KitchenItemProvider} contract: {@link #findIngredient} locates a matching
 * item in the network and returns an {@link IngredientToken} that extracts/inserts it on demand.
 */
public class MEKitchenItemProvider implements KitchenItemProvider {

    private final KitchenStationBlockEntity blockEntity;

    public MEKitchenItemProvider(KitchenStationBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    private MEStorage getNetworkStorage() {
        return blockEntity.getNetworkStorage();
    }

    private MachineSource source() {
        return new MachineSource(blockEntity.getActionHost());
    }

    @Override
    public IngredientToken findIngredient(Ingredient ingredient, Collection<IngredientToken> ingredientTokens, CacheHint cacheHint) {
        MEStorage storage = getNetworkStorage();
        if (storage == null) {
            return null;
        }
        KeyCounter stacks = storage.getAvailableStacks();

        if (cacheHint instanceof MEIngredientToken hint) {
            long available = stacks.get(hint.key);
            if (available > 0 && ingredient.test(hint.key.toStack()) && hasUsesLeft(hint.key, available, ingredientTokens)) {
                return new MEIngredientToken(hint.key);
            }
        }

        for (var entry : stacks) {
            if (entry.getKey() instanceof AEItemKey itemKey) {
                long available = entry.getLongValue();
                if (available > 0 && ingredient.test(itemKey.toStack()) && hasUsesLeft(itemKey, available, ingredientTokens)) {
                    return new MEIngredientToken(itemKey);
                }
            }
        }
        return null;
    }

    @Override
    public IngredientToken findIngredient(ItemStack itemStack, Collection<IngredientToken> ingredientTokens, CacheHint cacheHint) {
        MEStorage storage = getNetworkStorage();
        if (storage == null) {
            return null;
        }
        AEItemKey wanted = AEItemKey.of(itemStack);
        if (wanted == null) {
            return null;
        }
        long available = storage.getAvailableStacks().get(wanted);
        if (available > 0 && hasUsesLeft(wanted, available, ingredientTokens)) {
            return new MEIngredientToken(wanted);
        }
        return null;
    }

    @Override
    public CacheHint getCacheHint(IngredientToken ingredientToken) {
        return ingredientToken instanceof MEIngredientToken meToken ? meToken : CacheHint.NONE;
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

    public class MEIngredientToken implements IngredientToken, CacheHint {
        private final AEItemKey key;

        private MEIngredientToken(AEItemKey key) {
            this.key = key;
        }

        @Override
        public ItemStack peek() {
            MEStorage storage = getNetworkStorage();
            if (storage == null) {
                return ItemStack.EMPTY;
            }
            long available = storage.getAvailableStacks().get(key);
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
            if (inserted >= itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            ItemStack remainder = itemStack.copy();
            remainder.shrink((int) inserted);
            return remainder;
        }
    }
}
