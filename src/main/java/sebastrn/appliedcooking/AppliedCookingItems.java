package sebastrn.appliedcooking;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import sebastrn.appliedcooking.item.KitchenStationBlockItem;

public final class AppliedCookingItems {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, AppliedCooking.ID);

    public static final DeferredHolder<Item, KitchenStationBlockItem> KITCHEN_STATION =
            ITEMS.register("kitchen_station", () -> new KitchenStationBlockItem(AppliedCookingBlocks.KITCHEN_STATION.get(), new Item.Properties()));

    private AppliedCookingItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
