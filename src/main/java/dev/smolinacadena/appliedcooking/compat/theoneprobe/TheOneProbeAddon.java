package dev.smolinacadena.appliedcooking.compat.theoneprobe;

import dev.smolinacadena.appliedcooking.AppliedCooking;
import dev.smolinacadena.appliedcooking.block.KitchenStationBlock;
import dev.smolinacadena.appliedcooking.blockentity.KitchenStationBlockEntity;
import dev.smolinacadena.appliedcooking.compat.Compat;
import mcjty.theoneprobe.api.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.InterModComms;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class TheOneProbeAddon {

    public static void register() {
        InterModComms.sendTo(Compat.THEONEPROBE, "getTheOneProbe", TopInitializer::new);
    }

    public static class TopInitializer implements Function<ITheOneProbe, Void> {
        @Nullable
        @Override
        public Void apply(@Nullable ITheOneProbe top) {
            if (top != null) {
                top.registerProvider(new ProbeInfoProvider());
            }
            return null;
        }
    }

    public static class ProbeInfoProvider implements IProbeInfoProvider {
        @Override
        public ResourceLocation getID() {
            return new ResourceLocation(AppliedCooking.ID, AppliedCooking.ID);
        }

        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player playerEntity, Level level, BlockState state, IProbeHitData data) {
            if (state.getBlock() instanceof KitchenStationBlock) {
                var kitchenStationBlockEntity = tryGetTileEntity(level, data.getPos(), KitchenStationBlockEntity.class);
                if (kitchenStationBlockEntity != null) {
                    if (kitchenStationBlockEntity.getSecurityStationPos() != "") {
                        info.mcText(new TranslatableComponent("jade.appliedcooking:online").withStyle(ChatFormatting.GRAY));
                        info.mcText(new TranslatableComponent("jade.appliedcooking:kitchen_station", new TranslatableComponent("block.ae2.security_station"), kitchenStationBlockEntity.getSecurityStationPos()).withStyle(ChatFormatting.GRAY));
                    } else {
                        info.mcText(new TranslatableComponent("jade.appliedcooking:offline").withStyle(ChatFormatting.GRAY));
                    }
                }
            }
        }

        @Nullable
        @SuppressWarnings("unchecked")
        private static <T extends BlockEntity> T tryGetTileEntity(Level level, BlockPos pos, Class<T> tileClass) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null && tileClass.isAssignableFrom(blockEntity.getClass())) {
                return (T) blockEntity;
            }
            return null;
        }
    }
}
