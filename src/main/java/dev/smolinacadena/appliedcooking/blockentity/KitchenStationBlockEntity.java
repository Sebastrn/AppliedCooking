package dev.smolinacadena.appliedcooking.blockentity;

import appeng.api.features.Locatables;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.MEStorage;
import appeng.me.InWorldGridNode;
import com.google.common.collect.Lists;
import dev.smolinacadena.appliedcooking.AppliedCookingBlockEntities;
import dev.smolinacadena.appliedcooking.AppliedCookingBlocks;
import dev.smolinacadena.appliedcooking.api.cookingforblockheads.capability.KitchenItemProvider;
import dev.smolinacadena.appliedcooking.block.KitchenStationBlock;
import dev.smolinacadena.appliedcooking.item.KitchenStationBlockItem;
import net.blay09.mods.balm.api.provider.BalmProvider;
import net.blay09.mods.balm.common.BalmBlockEntity;
import net.blay09.mods.cookingforblockheads.api.capability.IKitchenItemProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class KitchenStationBlockEntity extends BalmBlockEntity {
    private final KitchenItemProvider itemProvider = new KitchenItemProvider(this);
    private Long gridKey;

    public KitchenStationBlockEntity(BlockPos pos, BlockState state) {
        super(AppliedCookingBlockEntities.KITCHEN_STATION.get(), pos, state);
    }

    @Override
    public List<BalmProvider<?>> getProviders() {
        return Lists.newArrayList(new BalmProvider<>(IKitchenItemProvider.class, itemProvider));
    }

    public void setConnected(boolean connected) {
        level.blockEvent(worldPosition, AppliedCookingBlocks.KITCHEN_STATION.get(), 0, 0);

        BlockState state = level.getBlockState(worldPosition);
        level.setBlockAndUpdate(worldPosition, state.setValue(KitchenStationBlock.CONNECTED, connected));
        setChanged();
    }

    public void applyDataFromItemToBlockEntity(ItemStack stack) {
        if (stack.hasTag()) {
            if (stack.getTag().contains(KitchenStationBlockItem.TAG_GRID_KEY)) {
                gridKey = stack.getTag().getLong(KitchenStationBlockItem.TAG_GRID_KEY);

                if (getActionHost() != null) {
                    setConnected(true);
                }
            }
        }

        setChanged();
    }

    public void applyDataFromBlockEntityToItem(ItemStack stack) {
        stack.setTag(new CompoundTag());

        if (stack.getTag().contains(KitchenStationBlockItem.TAG_GRID_KEY)) {
            stack.getTag().putLong(KitchenStationBlockItem.TAG_GRID_KEY, gridKey);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (gridKey != null) {
            tag.putLong(KitchenStationBlockItem.TAG_GRID_KEY, gridKey.longValue());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains(KitchenStationBlockItem.TAG_GRID_KEY)) {
            gridKey = tag.getLong(KitchenStationBlockItem.TAG_GRID_KEY);
        }
    }

    public MEStorage getNetworkStorage() {
        if (getActionHost() != null) {
            var n = getActionHost().getActionableNode();
            if (n != null) {
                return n.getGrid().getStorageService().getInventory();
            }
        }
        return null;
    }

    public String getSecurityStationPos() {
        if (getActionHost() != null) {
            InWorldGridNode securityStation = (InWorldGridNode) getActionHost().getActionableNode();
            return securityStation.getLocation().getX() + ", " + securityStation.getLocation().getY() + ", " + securityStation.getLocation().getZ();
        }
        return "";
    }

    public IActionHost getActionHost() {
        if (gridKey != null) {
            return Locatables.securityStations().get(this.level, gridKey.longValue());
        }
        return null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, KitchenStationBlockEntity blockEntity) {
        blockEntity.serverTick();
    }

    public void serverTick() {
        if (getActionHost() != null) {
            setConnected(true);
        } else {
            setConnected(false);
        }
    }
}
