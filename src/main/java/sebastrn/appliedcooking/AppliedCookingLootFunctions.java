package sebastrn.appliedcooking;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import sebastrn.appliedcooking.lootable.KitchenStationBlockLootFunction;

public final class AppliedCookingLootFunctions {

    private static final DeferredRegister<LootItemFunctionType> LOOT_ITEM_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, AppliedCooking.ID);

    public static final DeferredHolder<LootItemFunctionType, LootItemFunctionType> KITCHEN_STATION =
            LOOT_ITEM_FUNCTIONS.register("kitchen_station", () -> new LootItemFunctionType(KitchenStationBlockLootFunction.CODEC));

    private AppliedCookingLootFunctions() {
    }

    public static void register(IEventBus modEventBus) {
        LOOT_ITEM_FUNCTIONS.register(modEventBus);
    }
}
