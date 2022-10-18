package dev.smolinacadena.appliedcooking;

import dev.smolinacadena.appliedcooking.block.KitchenStationBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class AppliedCookingBlocks {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AppliedCooking.ID);

    public static final RegistryObject<KitchenStationBlock> KITCHEN_STATION;

    static {
        KITCHEN_STATION = BLOCKS.register("kitchen_station", KitchenStationBlock::new);
    }

    private AppliedCookingBlocks(){
    }

    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
