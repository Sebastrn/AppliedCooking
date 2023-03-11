package dev.smolinacadena.appliedcooking;

import dev.smolinacadena.appliedcooking.lootable.KitchenStationBlockLootFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class AppliedCookingLootFunctions {

    public static final RegistryObject<LootItemFunctionType> KITCHEN_STATION;

    private static final DeferredRegister<LootItemFunctionType> LOOT_ITEM_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, AppliedCooking.ID);

    static {
        KITCHEN_STATION = LOOT_ITEM_FUNCTIONS.register("kitchen_station", () -> new LootItemFunctionType(new KitchenStationBlockLootFunction.Serializer()));
    }

    private AppliedCookingLootFunctions() {
    }

    public static void register() {
        LOOT_ITEM_FUNCTIONS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
