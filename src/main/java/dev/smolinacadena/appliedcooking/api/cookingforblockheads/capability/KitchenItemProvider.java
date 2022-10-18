package dev.smolinacadena.appliedcooking.api.cookingforblockheads.capability;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.me.helpers.MachineSource;
import dev.smolinacadena.appliedcooking.blockentity.KitchenStationBlockEntity;
import net.blay09.mods.cookingforblockheads.api.SourceItem;
import net.blay09.mods.cookingforblockheads.api.capability.IKitchenItemProvider;
import net.blay09.mods.cookingforblockheads.api.capability.IngredientPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

public class KitchenItemProvider implements IKitchenItemProvider {

    private final KitchenStationBlockEntity blockEntity;
    private final HashMap<String, Integer> usedStackSizes;

    public KitchenItemProvider(KitchenStationBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.usedStackSizes = new HashMap<>();
    }

    public MEStorage getNetworkStorage(){
        return this.blockEntity.getNetworkStorage();
    }

    @Override
    public void resetSimulation() {
        usedStackSizes.replaceAll((n, v) -> 0);
    }

    @Override
    public int getSimulatedUseCount(int slot) {
        return 0;
    }

    public int getSimulatedUseCount(String key) {
        return usedStackSizes.getOrDefault(key, 0);
    }

    @Override
    public ItemStack useItemStack(int slot, int amount, boolean simulate, List<IKitchenItemProvider> inventories, boolean requireBucket) {
        return ItemStack.EMPTY;
    }

    public void useItemStack(ItemStack itemStack, int amount, boolean simulate, List<IKitchenItemProvider> inventories, boolean requireBucket) {
        String itemRegistryName = ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString();
        if (itemStack.getCount() - (simulate ? usedStackSizes.getOrDefault(itemRegistryName, 0) : 0) >= amount) {
            var result = getNetworkStorage().extract(AEItemKey.of(itemStack), amount, simulate ? Actionable.SIMULATE : Actionable.MODULATE, new MachineSource(blockEntity.getActionHost()));
            if (simulate && result > 1) {
                usedStackSizes.put(itemRegistryName, usedStackSizes.getOrDefault(itemRegistryName, 0) + (int)result);
            }
        }
    }

    @Override
    public ItemStack returnItemStack(ItemStack itemStack, SourceItem sourceItem) {
        getNetworkStorage().insert(AEItemKey.of(itemStack), itemStack.getCount(), Actionable.MODULATE, new MachineSource(blockEntity.getActionHost()));
        return itemStack;
    }

    @Override
    public int getSlots() {
        return getNetworkStorage() != null ? getNetworkStorage().getAvailableStacks().size() : 0;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack returnItemStack(ItemStack itemStack) {
        return itemStack;
    }

    @Nullable
    @Override
    public SourceItem findSource(IngredientPredicate predicate, int maxAmount, List<IKitchenItemProvider> inventories, boolean requireBucket, boolean simulate) {
        if(getNetworkStorage() != null) {
            var availableStacks = getNetworkStorage().getAvailableStacks();
            for (var entry : availableStacks) {
                if(entry.getKey() instanceof AEItemKey itemKey) {
                    String registryName = ForgeRegistries.ITEMS.getKey(itemKey.getItem()).toString();
                    if (entry.getLongValue() > 0 && predicate.test(itemKey.toStack((int)entry.getLongValue()), (int)entry.getLongValue() - getSimulatedUseCount(registryName))) {
                        return new SourceItem(this, 0, itemKey.toStack((int)entry.getLongValue()).copy());
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public SourceItem findSourceAndMarkAsUsed(IngredientPredicate predicate, int maxAmount, List<IKitchenItemProvider> inventories, boolean requireBucket, boolean simulate) {
        SourceItem sourceItem = findSource(predicate, maxAmount, inventories, requireBucket, simulate);
        if (sourceItem != null) {
            useItemStack(sourceItem.getSourceStack(), Math.min(sourceItem.getSourceStack().getCount(), maxAmount), simulate, inventories, requireBucket);
        }
        return sourceItem;
    }

    @Override
    public void consumeSourceItem(SourceItem sourceItem, int maxAmount, List<IKitchenItemProvider> inventories, boolean requireContainer) {
        if (sourceItem.getSourceSlot() < 0) {
            return;
        }
        useItemStack(sourceItem.getSourceStack(), maxAmount, false, inventories, requireContainer);
    }

    @Override
    public void markAsUsed(SourceItem sourceItem, int maxAmount, List<IKitchenItemProvider> inventories, boolean requireBucket) {
        useItemStack(sourceItem.getSourceStack(), Math.min(sourceItem.getSourceStack().getCount(), maxAmount), true, inventories, requireBucket);
    }
}
