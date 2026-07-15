package sebastrn.appliedcooking.setup;

import appeng.api.features.GridLinkables;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import sebastrn.appliedcooking.AppliedCookingItems;
import sebastrn.appliedcooking.item.KitchenStationBlockItem;

public final class CommonSetup {

    private CommonSetup() {
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() ->
                GridLinkables.register(AppliedCookingItems.KITCHEN_STATION.get(), KitchenStationBlockItem.LINKABLE_HANDLER));
    }
}
