package dev.smolinacadena.appliedcooking.compat.jade;

import dev.smolinacadena.appliedcooking.blockentity.KitchenStationBlockEntity;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class KitchenStationComponentProvider implements IComponentProvider, IServerDataProvider<BlockEntity> {

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getServerData().contains("securityStationPos") && accessor.getServerData().getString("securityStationPos") != "") {
            tooltip.add(new TranslatableComponent("jade.appliedcooking:online"));
            tooltip.add(new TranslatableComponent("jade.appliedcooking:kitchen_station", new TranslatableComponent("block.ae2.security_station"), accessor.getServerData().getString("securityStationPos")));
        } else {
            tooltip.add(new TranslatableComponent("jade.appliedcooking:offline"));
        }
    }

    @Override
    public void appendServerData(CompoundTag data, ServerPlayer player, Level world, BlockEntity blockEntity, boolean showDetails) {
        KitchenStationBlockEntity kitchenStation = (KitchenStationBlockEntity) blockEntity;
        data.putString("securityStationPos", kitchenStation.getSecurityStationPos());
    }
}