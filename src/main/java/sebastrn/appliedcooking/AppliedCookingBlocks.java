package sebastrn.appliedcooking;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import sebastrn.appliedcooking.block.KitchenStationBlock;

public final class AppliedCookingBlocks {

    // 26.1 requires every block to carry its registry id on the Properties (Properties.setId). The DeferredRegister.Blocks
    // helper's registerBlock does that for us; a plain DeferredRegister<Block> leaves the id unset and registration NPEs
    // with "Block id not set".
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AppliedCooking.ID);

    public static final DeferredBlock<KitchenStationBlock> KITCHEN_STATION =
            BLOCKS.registerBlock("kitchen_station",
                    KitchenStationBlock::new,
                    () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.5f));

    private AppliedCookingBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
