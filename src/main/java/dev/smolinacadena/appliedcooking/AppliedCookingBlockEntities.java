package dev.smolinacadena.appliedcooking;

import dev.smolinacadena.appliedcooking.blockentity.KitchenStationBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AppliedCookingBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AppliedCooking.ID);

    public static final RegistryObject<BlockEntityType<KitchenStationBlockEntity>> KITCHEN_STATION =
            REGISTRY.register("kitchen_station", () -> BlockEntityType.Builder.of(KitchenStationBlockEntity::new, AppliedCookingBlocks.KITCHEN_STATION.get()).build(null));

    private AppliedCookingBlockEntities() {}
}
