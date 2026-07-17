package sebastrn.appliedcooking;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import sebastrn.appliedcooking.lootable.KitchenStationBlockLootFunction;

public final class AppliedCookingLootFunctions {

    // 26.1 dropped LootItemFunctionType: the LOOT_FUNCTION_TYPE registry now holds the MapCodec directly.
    private static final DeferredRegister<MapCodec<? extends LootItemFunction>> LOOT_ITEM_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, AppliedCooking.ID);

    public static final DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<KitchenStationBlockLootFunction>> KITCHEN_STATION =
            LOOT_ITEM_FUNCTIONS.register("kitchen_station", () -> KitchenStationBlockLootFunction.CODEC);

    private AppliedCookingLootFunctions() {
    }

    public static void register(IEventBus modEventBus) {
        LOOT_ITEM_FUNCTIONS.register(modEventBus);
    }
}
