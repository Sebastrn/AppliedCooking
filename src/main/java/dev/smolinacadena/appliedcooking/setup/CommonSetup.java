package dev.smolinacadena.appliedcooking.setup;

import appeng.api.features.GridLinkables;
import dev.smolinacadena.appliedcooking.AppliedCooking;
import dev.smolinacadena.appliedcooking.AppliedCookingBlocks;
import dev.smolinacadena.appliedcooking.AppliedCookingItems;
import dev.smolinacadena.appliedcooking.blockentity.KitchenStationBlockEntity;
import dev.smolinacadena.appliedcooking.item.KitchenStationBlockItem;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public final class CommonSetup {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent e) {
        GridLinkables.register(AppliedCookingItems.KITCHEN_STATION.get(), KitchenStationBlockItem.LINKABLE_HANDLER);
    }

    @SubscribeEvent
    public static void onRegisterBlockEntities(RegistryEvent.Register<BlockEntityType<?>> e) {
        e.getRegistry().register(BlockEntityType.Builder.of(KitchenStationBlockEntity::new, AppliedCookingBlocks.KITCHEN_STATION.get()).build(null).setRegistryName(AppliedCooking.ID, "kitchen_station"));
    }
}
