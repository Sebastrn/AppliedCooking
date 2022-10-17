package dev.smolinacadena.appliedcooking;

import dev.smolinacadena.appliedcooking.item.KitchenStationBlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class AppliedCookingItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AppliedCooking.ID);
    public static final RegistryObject<KitchenStationBlockItem> KITCHEN_STATION;

    static {
        KITCHEN_STATION = ITEMS.register("kitchen_station", () -> new KitchenStationBlockItem(AppliedCookingBlocks.KITCHEN_STATION.get(), new Item.Properties().tab(AppliedCooking.CREATIVE_MODE_TAB).stacksTo(1)));
    }

    private AppliedCookingItems() {
    }

    public static void register() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
