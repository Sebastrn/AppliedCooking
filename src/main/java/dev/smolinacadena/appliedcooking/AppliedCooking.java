package dev.smolinacadena.appliedcooking;

import dev.smolinacadena.appliedcooking.item.group.MainCreativeModeTab;
import dev.smolinacadena.appliedcooking.setup.ClientSetup;
import dev.smolinacadena.appliedcooking.setup.CommonSetup;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
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
        AppliedCookingBlockEntities.REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
