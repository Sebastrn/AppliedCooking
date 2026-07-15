package sebastrn.appliedcooking;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AppliedCookingCreativeTab {

    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AppliedCooking.ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_MODE_TAB = CREATIVE_TABS.register(AppliedCooking.ID,
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(AppliedCookingItems.KITCHEN_STATION.get()))
                    .title(Component.translatable("itemGroup." + AppliedCooking.ID))
                    .displayItems((features, output) -> output.accept(AppliedCookingItems.KITCHEN_STATION.get()))
                    .build());

    private AppliedCookingCreativeTab() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
