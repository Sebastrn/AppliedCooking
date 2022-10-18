package dev.smolinacadena.appliedcooking.setup;

import appeng.api.features.GridLinkables;
import dev.smolinacadena.appliedcooking.AppliedCookingItems;
import dev.smolinacadena.appliedcooking.item.KitchenStationBlockItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public final class CommonSetup {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent e) {
        GridLinkables.register(AppliedCookingItems.KITCHEN_STATION.get(), KitchenStationBlockItem.LINKABLE_HANDLER);
    }
}
