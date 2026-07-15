package sebastrn.appliedcooking.blockentity;

import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.MEStorage;
import appeng.util.Platform;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.blay09.mods.balm.api.provider.BalmProvider;
import net.blay09.mods.balm.common.BalmBlockEntity;
import net.blay09.mods.cookingforblockheads.api.KitchenItemProvider;
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
import sebastrn.appliedcooking.AppliedCookingBlockEntities;
import sebastrn.appliedcooking.api.cookingforblockheads.capability.MEKitchenItemProvider;
import sebastrn.appliedcooking.block.KitchenStationBlock;
import sebastrn.appliedcooking.item.KitchenStationBlockItem;

import java.util.List;

public class KitchenStationBlockEntity extends BalmBlockEntity {

    /** Re-resolve the AE2 link at most this often (ticks). The link is a live handle, so sub-second refresh is wasteful. */
    private static final int NETWORK_REFRESH_INTERVAL = 20;

    private final MEKitchenItemProvider itemProvider = new MEKitchenItemProvider(this);
    private GlobalPos accessPointPos = null;
    private IActionHost actionHost = null;
    private IGrid grid = null;
    private MEStorage meStorage = null;
    /** Ticks since the last {@link #setNetworkProperties()}; starts at the interval so the first tick after load resolves. */
    private int ticksSinceNetworkRefresh = NETWORK_REFRESH_INTERVAL;

    public KitchenStationBlockEntity(BlockPos pos, BlockState state) {
        super(AppliedCookingBlockEntities.KITCHEN_STATION.get(), pos, state);
    }

    @Override
    public List<BalmProvider<?>> getProviders() {
        return Lists.newArrayList(new BalmProvider<>(KitchenItemProvider.class, itemProvider));
    }

    /** CONNECTED drives the block model, so only rewrite the state (and re-save) when it actually flips. */
    private void updateConnectedState(boolean connected) {
        BlockState state = level.getBlockState(worldPosition);
        if (state.getValue(KitchenStationBlock.CONNECTED) != connected) {
            level.setBlockAndUpdate(worldPosition, state.setValue(KitchenStationBlock.CONNECTED, connected));
            setChanged();
        }
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
        if (accessPointPos != null) {
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

    /**
     * True only while the station is linked to an access point that is currently active and on a network — i.e.
     * items can actually flow. This is the signal both the CONNECTED block model and the Jade/TOP tooltips use;
     * a linked-but-unpowered access point counts as disconnected (its block entity still exists, but {@code grid}
     * is null). Not merely "the access point block exists".
     */
    public boolean isConnected() {
        return grid != null;
    }

    public String getAccessPointPos() {
        if (isConnected() && accessPointPos != null) {
            return accessPointPos.pos().getX() + ", " + accessPointPos.pos().getY() + ", " + accessPointPos.pos().getZ();
        }
        return "";
    }

    public IActionHost getActionHost() {
        return actionHost;
    }

    public void setNetworkProperties() {
        actionHost = null;
        grid = null;
        meStorage = null;

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (accessPointPos == null) {
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
        if (++ticksSinceNetworkRefresh < NETWORK_REFRESH_INTERVAL) {
            return;
        }
        ticksSinceNetworkRefresh = 0;
        setNetworkProperties();
        updateConnectedState(isConnected());
    }
}
