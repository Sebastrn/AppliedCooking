package dev.smolinacadena.appliedcooking;

import dev.smolinacadena.appliedcooking.compat.Compat;
import dev.smolinacadena.appliedcooking.compat.theoneprobe.TheOneProbeAddon;
import dev.smolinacadena.appliedcooking.item.group.MainCreativeModeTab;
import dev.smolinacadena.appliedcooking.setup.ClientSetup;
import dev.smolinacadena.appliedcooking.setup.CommonSetup;
import net.blay09.mods.balm.api.Balm;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AppliedCooking.ID)
public final class AppliedCooking {
    public static final String ID = "appliedcooking";
    public static final CreativeModeTab CREATIVE_MODE_TAB = new MainCreativeModeTab(AppliedCooking.ID);

    public AppliedCooking() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::onModelBake);
        });

        AppliedCookingBlocks.register();
        AppliedCookingItems.register();
        AppliedCookingLootFunctions.register();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(CommonSetup::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(BlockEntityType.class, CommonSetup::onRegisterBlockEntities);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
    }

    private void enqueueIMC(InterModEnqueueEvent event) {
        if (Balm.isModLoaded(Compat.THEONEPROBE)) {
            TheOneProbeAddon.register();
        }
    }
}
