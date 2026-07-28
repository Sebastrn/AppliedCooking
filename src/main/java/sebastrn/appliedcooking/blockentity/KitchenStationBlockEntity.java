package sebastrn.appliedcooking.blockentity;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.MEStorage;
import appeng.util.Platform;
import net.blay09.mods.cookingforblockheads.api.KitchenItemProvider;
import net.blay09.mods.cookingforblockheads.capability.KitchenItemProviderHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import sebastrn.appliedcooking.AppliedCooking;
import sebastrn.appliedcooking.AppliedCookingBlockEntities;
import sebastrn.appliedcooking.api.cookingforblockheads.capability.MEKitchenItemProvider;
import sebastrn.appliedcooking.block.KitchenStationBlock;
import sebastrn.appliedcooking.block.KitchenStationBlock.LinkState;

public class KitchenStationBlockEntity extends BlockEntity implements KitchenItemProviderHolder {

    /**
     * Key for the linked access point in the block entity's own NBT. The *item* carries the same link in AE2's
     * {@link AEComponents#WIRELESS_LINK_TARGET} data component instead — block entities still save to NBT.
     */
    private static final String TAG_ACCESS_POINT_POS = "accessPointPos";

    /** Re-resolve the AE2 link at most this often (ticks). The link is a live handle, so sub-second refresh is wasteful. */
    private static final int NETWORK_REFRESH_INTERVAL = 20;

    /**
     * Power drawn from the linked network every tick while connected (AE/t). When the network can't pay, the station
     * goes offline. Server-configurable (see {@code ServerConfig}); read through this accessor rather than cached, so
     * a config reload takes effect without a restart.
     */
    public static double idlePowerDrain() {
        return AppliedCooking.SERVER_CONFIG.getKitchenStation().getIdlePowerDrain();
    }

    private final MEKitchenItemProvider itemProvider = new MEKitchenItemProvider(this);
    private GlobalPos accessPointPos = null;
    /** The linked wireless access point; re-found periodically (an expensive cross-dimension block-entity lookup). */
    private IWirelessAccessPoint accessPoint = null;
    /** Live grid derived from {@link #accessPoint} each tick; non-null only while connected, active, and paying power. */
    private IGrid grid = null;
    private MEStorage meStorage = null;
    /** Ticks since the last {@link #resolveAccessPoint()}; starts at the interval so the first tick after load resolves. */
    private int ticksSinceNetworkRefresh = NETWORK_REFRESH_INTERVAL;

    public KitchenStationBlockEntity(BlockPos pos, BlockState state) {
        super(AppliedCookingBlockEntities.KITCHEN_STATION.get(), pos, state);
    }

    /**
     * Exposes the Kitchen Station's item provider to CFB. CFB's {@code ModCapabilities.KITCHEN_ITEM_PROVIDER}
     * capability is wired to this holder for our block-entity type in {@link AppliedCooking#registerCapabilities}.
     */
    @Override
    public KitchenItemProvider getKitchenItemProvider() {
        return itemProvider;
    }

    /** LINK_STATE drives the block model, so only rewrite the state (and re-save) when it actually changes. */
    private void updateLinkState() {
        BlockState state = level.getBlockState(worldPosition);
        LinkState current = getLinkState();
        if (state.getValue(KitchenStationBlock.LINK_STATE) != current) {
            level.setBlockAndUpdate(worldPosition, state.setValue(KitchenStationBlock.LINK_STATE, current));
            setChanged();
        }
    }

    public void applyDataFromItemToBlockEntity(ItemStack stack) {
        accessPointPos = stack.get(AEComponents.WIRELESS_LINK_TARGET);
        if (accessPointPos != null) {
            setNetworkProperties();
        }

        setChanged();
    }

    public void applyDataFromBlockEntityToItem(ItemStack stack) {
        if (accessPointPos != null) {
            stack.set(AEComponents.WIRELESS_LINK_TARGET, accessPointPos);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        // storeNullable no-ops on null, so no explicit guard is needed. 26.1's ValueOutput takes the Codec directly.
        output.storeNullable(TAG_ACCESS_POINT_POS, GlobalPos.CODEC, accessPointPos);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        accessPointPos = input.read(TAG_ACCESS_POINT_POS, GlobalPos.CODEC).orElse(null);
    }

    public MEStorage getNetworkStorage() {
        // Gate on the full connected state (link + power) so the provider serves nothing while the station is unpowered.
        return isConnected() ? meStorage : null;
    }

    /**
     * True only while the station is linked to an active access point on a grid that can pay the
     * {@link #idlePowerDrain() idle power cost}. {@link #grid} is set only when all of that holds, so this is simply
     * "do we have a live grid". {@link #getNetworkStorage()} keys off it, and it is what separates
     * {@link LinkState#ONLINE} from {@link LinkState#LINKED_OFFLINE}: a linked-but-unpowered station is not connected.
     */
    public boolean isConnected() {
        return grid != null;
    }

    /** True once an access point is saved (from linking at a Wireless Access Point), whether or not it is reachable. */
    public boolean isLinked() {
        return accessPointPos != null;
    }

    /**
     * The three-way link state read by the blockstate model and the Jade/TOP tooltips. Derived, not stored:
     * a live {@link #grid} means ONLINE, a saved-but-unreachable link means LINKED_OFFLINE, otherwise UNLINKED.
     */
    public LinkState getLinkState() {
        if (isConnected()) {
            return LinkState.ONLINE;
        }
        return isLinked() ? LinkState.LINKED_OFFLINE : LinkState.UNLINKED;
    }

    /** Formatted access-point coordinates, shown whenever the station is linked (online or offline); "" when unlinked. */
    public String getAccessPointPos() {
        if (accessPointPos != null) {
            return accessPointPos.pos().getX() + ", " + accessPointPos.pos().getY() + ", " + accessPointPos.pos().getZ();
        }
        return "";
    }

    public IActionHost getActionHost() {
        return accessPoint;
    }

    /** Full resolve (access point + live connection). Used on placement; the tick loop splits these to throttle the expensive half. */
    public void setNetworkProperties() {
        resolveAccessPoint();
        refreshConnection();
    }

    /** Re-find the linked wireless access point — a cross-dimension block-entity lookup, so the caller throttles this. */
    private void resolveAccessPoint() {
        accessPoint = null;

        if (!(level instanceof ServerLevel serverLevel) || accessPointPos == null) {
            return;
        }

        var linkedLevel = serverLevel.getServer().getLevel(accessPointPos.dimension());
        if (linkedLevel == null) {
            return;
        }

        if (Platform.getTickingBlockEntity(linkedLevel, accessPointPos.pos()) instanceof IWirelessAccessPoint found) {
            accessPoint = found;
        }
    }

    /**
     * Re-derive the live grid/storage from the cached access point (cheap) and pay this tick's idle power. Run every
     * tick so the connection state and power draw stay current between the throttled {@link #resolveAccessPoint()}
     * calls. {@link #grid} ends up non-null only when the access point is active, on a grid, and that grid paid up —
     * so an unpowered network cleanly reads as disconnected.
     */
    private void refreshConnection() {
        grid = null;
        meStorage = null;

        if (accessPoint == null || !accessPoint.isActive()) {
            return;
        }

        IGrid liveGrid = accessPoint.getGrid();
        if (liveGrid == null || !drainIdlePower(liveGrid)) {
            return;
        }

        grid = liveGrid;
        meStorage = liveGrid.getStorageService().getInventory();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, KitchenStationBlockEntity blockEntity) {
        blockEntity.serverTick();
    }

    public void serverTick() {
        if (++ticksSinceNetworkRefresh >= NETWORK_REFRESH_INTERVAL) {
            ticksSinceNetworkRefresh = 0;
            resolveAccessPoint();
        }
        refreshConnection();
        updateLinkState();
    }

    /**
     * Charge {@code grid} the configured {@link #idlePowerDrain() idle power cost} for this tick, but only if it can
     * pay in full — we don't drain the last scraps for a service we then won't provide.
     *
     * @return true if the network paid the full idle cost.
     */
    private boolean drainIdlePower(IGrid grid) {
        // Read once: the config is reloadable, and simulating against one value then modulating against another
        // could drain an amount we never checked.
        double drain = idlePowerDrain();
        if (drain <= 0) {
            return true;
        }

        IEnergyService energy = grid.getEnergyService();
        if (energy.extractAEPower(drain, Actionable.SIMULATE, PowerMultiplier.CONFIG) < drain) {
            return false;
        }
        energy.extractAEPower(drain, Actionable.MODULATE, PowerMultiplier.CONFIG);
        return true;
    }
}
