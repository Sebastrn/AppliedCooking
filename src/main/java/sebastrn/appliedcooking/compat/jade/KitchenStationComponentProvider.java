package sebastrn.appliedcooking.compat.jade;

import sebastrn.appliedcooking.AppliedCooking;
import sebastrn.appliedcooking.blockentity.KitchenStationBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class KitchenStationComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final ResourceLocation KITCHEN_STATION_UID = new ResourceLocation(AppliedCooking.ID, "kitchen_station");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getServerData().contains("accessPointPos") && !accessor.getServerData().getString("accessPointPos").isEmpty()) {
            tooltip.add(Component.translatable("jade.appliedcooking:online"));
            tooltip.add(Component.translatable("jade.appliedcooking:kitchen_station", Component.translatable("block.ae2.wireless_access_point"), accessor.getServerData().getString("accessPointPos")));
            tooltip.add(Component.translatable("jade.appliedcooking:power_drain", String.valueOf(accessor.getServerData().getDouble("powerDrain"))));
        } else {
            tooltip.add(Component.translatable("jade.appliedcooking:offline"));
        }
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        KitchenStationBlockEntity kitchenStation = (KitchenStationBlockEntity) accessor.getBlockEntity();
        data.putString("accessPointPos", kitchenStation.getAccessPointPos());
        // Send the drain rather than reading the config client-side: it's a server config, so the client's copy is
        // only correct once synced, and on a server the authoritative value is the one we're actually charging.
        data.putDouble("powerDrain", KitchenStationBlockEntity.idlePowerDrain());
    }

    @Override
    public ResourceLocation getUid() {
        return KITCHEN_STATION_UID;
    }

}