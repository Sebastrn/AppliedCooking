package dev.smolinacadena.appliedcooking.compat.jade;

import dev.smolinacadena.appliedcooking.AppliedCooking;
import dev.smolinacadena.appliedcooking.blockentity.KitchenStationBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class KitchenStationComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockEntity> {

    public static final ResourceLocation KITCHEN_STATION_UID = new ResourceLocation(AppliedCooking.ID, "kitchen_station");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getServerData().contains("securityStationPos") && accessor.getServerData().getString("securityStationPos") != "") {
            tooltip.add(Component.translatable("jade.appliedcooking:online"));
            tooltip.add(Component.translatable("jade.appliedcooking:kitchen_station", Component.translatable("block.ae2.security_station"), accessor.getServerData().getString("securityStationPos")));
        } else {
            tooltip.add(Component.translatable("jade.appliedcooking:offline"));
        }
    }

    @Override
    public void appendServerData(CompoundTag data, ServerPlayer player, Level world, BlockEntity blockEntity, boolean showDetails) {
        KitchenStationBlockEntity kitchenStation = (KitchenStationBlockEntity) blockEntity;
        data.putString("securityStationPos", kitchenStation.getSecurityStationPos());
    }

    @Override
    public ResourceLocation getUid() {
        return KITCHEN_STATION_UID;
    }

}