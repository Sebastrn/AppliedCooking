package dev.smolinacadena.appliedcooking.blockentity;

import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.MEStorage;
import appeng.util.Platform;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import dev.smolinacadena.appliedcooking.AppliedCookingBlockEntities;
import dev.smolinacadena.appliedcooking.AppliedCookingBlocks;
import dev.smolinacadena.appliedcooking.api.cookingforblockheads.capability.KitchenItemProvider;
import dev.smolinacadena.appliedcooking.block.KitchenStationBlock;
import dev.smolinacadena.appliedcooking.item.KitchenStationBlockItem;
import net.blay09.mods.balm.api.provider.BalmProvider;
import net.blay09.mods.balm.common.BalmBlockEntity;
import net.blay09.mods.cookingforblockheads.api.capability.IKitchenItemProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KitchenStationBlockEntity extends BalmBlockEntity {
    private final KitchenItemProvider itemProvider = new KitchenItemProvider(this);
    private GlobalPos accessPointPos = null;
    private IActionHost actionHost = null;
    private IGrid grid = null;
    private MEStorage meStorage = null;

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
        var tag = stack.getTag();
        if (tag != null && tag.contains(KitchenStationBlockItem.TAG_ACCESS_POINT_POS, Tag.TAG_COMPOUND)) {
            accessPointPos = GlobalPos.CODEC.decode(NbtOps.INSTANCE, tag.get(KitchenStationBlockItem.TAG_ACCESS_POINT_POS))
                    .result()
                    .map(Pair::getFirst)
                    .orElse(null);
            setNetworkProperties();
        } else {
            accessPointPos = null;
        }

        setChanged();
    }

    public void applyDataFromBlockEntityToItem(ItemStack stack) {
        if(accessPointPos != null) {
            GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, accessPointPos)
                    .result()
                    .ifPresent(tagValue -> stack.getOrCreateTag().put(KitchenStationBlockItem.TAG_ACCESS_POINT_POS, tagValue));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (accessPointPos != null) {
            GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, accessPointPos)
                    .result()
                    .ifPresent(tagValue -> tag.put(KitchenStationBlockItem.TAG_ACCESS_POINT_POS, tagValue));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains(KitchenStationBlockItem.TAG_ACCESS_POINT_POS)) {
            accessPointPos = GlobalPos.CODEC.decode(NbtOps.INSTANCE, tag.get(KitchenStationBlockItem.TAG_ACCESS_POINT_POS))
                    .result()
                    .map(Pair::getFirst)
                    .orElse(null);
        }
    }

    public MEStorage getNetworkStorage() {
        return meStorage;
    }

    public String getAccessPointPos() {
        if (actionHost != null) {
            return accessPointPos.pos().getX() + ", " + accessPointPos.pos().getY() + ", " + accessPointPos.pos().getZ();
        }
        return "";
    }

    public IActionHost getActionHost() {
        return actionHost;
    }

    @Nullable
    public void setNetworkProperties() {
        actionHost = null;
        grid = null;
        meStorage = null;

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if(accessPointPos == null) {
            return;
        }

        var linkedLevel = serverLevel.getServer().getLevel(accessPointPos.dimension());
        if (linkedLevel == null) {
            return;
        }

        var accessPointBlockEntity = Platform.getTickingBlockEntity(linkedLevel, accessPointPos.pos());
        if (!(accessPointBlockEntity instanceof IWirelessAccessPoint accessPoint)) {
            return;
        }

        actionHost = accessPoint;
        if (accessPoint.isActive()) {
            grid = accessPoint.getGrid();
            if (grid != null) {
                meStorage = grid.getStorageService().getInventory();
            }
        }

    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, KitchenStationBlockEntity blockEntity) {
        blockEntity.serverTick();
    }

    public void serverTick() {
        setNetworkProperties();
        setConnected(grid != null);
    }
}
