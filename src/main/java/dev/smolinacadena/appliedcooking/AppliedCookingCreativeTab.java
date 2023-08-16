package dev.smolinacadena.appliedcooking;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class AppliedCookingCreativeTab {

    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AppliedCooking.ID);

    public static final RegistryObject<CreativeModeTab> CREATIVE_MODE_TAB = CREATIVE_TABS.register(AppliedCooking.ID,
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(AppliedCookingItems.KITCHEN_STATION.get()))
                    .title(Component.translatable("itemGroup." + AppliedCooking.ID))
                    .displayItems((features, output) -> {
                        output.accept(AppliedCookingItems.KITCHEN_STATION.get());
                    })
                    .build());

    public static void register() {
        CREATIVE_TABS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
