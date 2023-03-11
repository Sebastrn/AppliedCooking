package dev.smolinacadena.appliedcooking;

import dev.smolinacadena.appliedcooking.compat.Compat;
import dev.smolinacadena.appliedcooking.compat.theoneprobe.TheOneProbeAddon;
import dev.smolinacadena.appliedcooking.setup.ClientSetup;
import dev.smolinacadena.appliedcooking.setup.CommonSetup;
import net.blay09.mods.balm.api.Balm;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.CreativeModeTabEvent;
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

        FMLJavaModLoadingContext.get().getModEventBus().addListener(CommonSetup::onCommonSetup);
        AppliedCookingBlockEntities.REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addCreative);
    }

    private void enqueueIMC(InterModEnqueueEvent event) {
        if (Balm.isModLoaded(Compat.THEONEPROBE)) {
            TheOneProbeAddon.register();
        }
    }

    private void addCreative(CreativeModeTabEvent.Register event) {
        event.registerCreativeModeTab(new ResourceLocation(ID, "creativetab"), builder ->
                builder.title(Component.translatable("itemGroup.appliedcooking"))
                        .icon(() -> new ItemStack(AppliedCookingItems.KITCHEN_STATION.get()))
                        .displayItems((enabledFlags, populator, hasPermissions) -> {
                            populator.accept(AppliedCookingItems.KITCHEN_STATION.get());
                        })
        );
    }
}
