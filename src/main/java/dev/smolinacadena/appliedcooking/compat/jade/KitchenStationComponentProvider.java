package dev.smolinacadena.appliedcooking.compat.jade;

import dev.smolinacadena.appliedcooking.AppliedCooking;
import dev.smolinacadena.appliedcooking.blockentity.KitchenStationBlockEntity;
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
        if (accessor.getServerData().contains("accessPointPos") && accessor.getServerData().getString("accessPointPos") != "") {
            tooltip.add(Component.translatable("jade.appliedcooking:online"));
            tooltip.add(Component.translatable("jade.appliedcooking:kitchen_station", Component.translatable("block.ae2.wireless_access_point"), accessor.getServerData().getString("securityStationPos")));
        } else {
            tooltip.add(Component.translatable("jade.appliedcooking:offline"));
        }
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        KitchenStationBlockEntity kitchenStation = (KitchenStationBlockEntity) accessor.getBlockEntity();
        data.putString("accessPointPos", kitchenStation.getAccessPointPos());
    }

    @Override
    public ResourceLocation getUid() {
        return KITCHEN_STATION_UID;
    }

}