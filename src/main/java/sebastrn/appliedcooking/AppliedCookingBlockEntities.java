package sebastrn.appliedcooking;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import sebastrn.appliedcooking.blockentity.KitchenStationBlockEntity;

public final class AppliedCookingBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AppliedCooking.ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<KitchenStationBlockEntity>> KITCHEN_STATION =
            REGISTRY.register("kitchen_station", () -> BlockEntityType.Builder.of(KitchenStationBlockEntity::new, AppliedCookingBlocks.KITCHEN_STATION.get()).build(null));

    private AppliedCookingBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        REGISTRY.register(modEventBus);
    }
}
