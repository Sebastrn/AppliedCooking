package dev.smolinacadena.appliedcooking;

import dev.smolinacadena.appliedcooking.compat.Compat;
import dev.smolinacadena.appliedcooking.compat.theoneprobe.TheOneProbeAddon;
import dev.smolinacadena.appliedcooking.setup.ClientSetup;
import dev.smolinacadena.appliedcooking.setup.CommonSetup;
import net.blay09.mods.balm.api.Balm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AppliedCooking.ID)
public final class AppliedCooking {
    public static final String ID = "appliedcooking";

    public AppliedCooking() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::onModelBake);
        });

        AppliedCookingBlocks.register();
        AppliedCookingItems.register();
        AppliedCookingLootFunctions.register();
        AppliedCookingCreativeTab.register();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(CommonSetup::onCommonSetup);
        AppliedCookingBlockEntities.REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
    }

    private void enqueueIMC(InterModEnqueueEvent event) {
        if (Balm.isModLoaded(Compat.THEONEPROBE)) {
            TheOneProbeAddon.register();
        }
    }
}
