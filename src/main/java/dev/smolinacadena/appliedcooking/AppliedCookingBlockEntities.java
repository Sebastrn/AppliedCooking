package dev.smolinacadena.appliedcooking;

import dev.smolinacadena.appliedcooking.blockentity.KitchenStationBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(AppliedCooking.ID)
public class AppliedCookingBlockEntities {
    @ObjectHolder("kitchen_station")
    public static final BlockEntityType<KitchenStationBlockEntity> KITCHEN_STATION = null;
}
