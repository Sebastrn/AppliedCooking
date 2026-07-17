package sebastrn.appliedcooking;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import sebastrn.appliedcooking.item.KitchenStationBlockItem;

public final class AppliedCookingItems {

    // 26.1 requires every item to carry its registry id on the Item.Properties (Properties.setId). registerItem on the
    // DeferredRegister.Items helper sets it before the factory runs; a plain DeferredRegister<Item> leaves it unset.
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AppliedCooking.ID);

    // useBlockDescriptionPrefix() makes the item take the block's translation key (block.appliedcooking.kitchen_station)
    // instead of the item.* default that setId now assigns — otherwise the item shows the raw item.* key in-game.
    public static final DeferredItem<KitchenStationBlockItem> KITCHEN_STATION =
            ITEMS.registerItem("kitchen_station",
                    props -> new KitchenStationBlockItem(AppliedCookingBlocks.KITCHEN_STATION.get(), props),
                    () -> new Item.Properties().useBlockDescriptionPrefix());

    private AppliedCookingItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
