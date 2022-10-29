package dev.smolinacadena.appliedcooking.block;

import dev.smolinacadena.appliedcooking.AppliedCookingBlockEntities;
import dev.smolinacadena.appliedcooking.AppliedCookingBlocks;
import dev.smolinacadena.appliedcooking.blockentity.KitchenStationBlockEntity;
import net.blay09.mods.cookingforblockheads.block.BlockKitchen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class KitchenStationBlock extends BlockKitchen {

    private static final VoxelShape SHAPE_NORTH = Block.box(3, -1, 6, 13, 7.5, 13);
    private static final VoxelShape SHAPE_SOUTH = Block.box(3, -1, 3, 13, 7.5, 10);
    private static final VoxelShape SHAPE_EAST = Block.box(3, -1, 3, 10, 7.5, 13);
    private static final VoxelShape SHAPE_WEST = Block.box(6, -1, 3, 13, 7.5, 13);
    public static final BooleanProperty CONNECTED = BooleanProperty.create("connected");

    public KitchenStationBlock() {
        super(BlockBehaviour.Properties.of(Material.METAL).sound(SoundType.METAL).strength(2.5f), AppliedCookingBlocks.KITCHEN_STATION.getId());
        registerDefaultState(getStateDefinition().any().setValue(CONNECTED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CONNECTED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING)) {
            case UP:
            case DOWN:
            case SOUTH:
            default:
                return SHAPE_SOUTH;
            case NORTH:
                return SHAPE_NORTH;
            case WEST:
                return SHAPE_WEST;
            case EAST:
                return SHAPE_EAST;
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KitchenStationBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide) {
            ((KitchenStationBlockEntity) level.getBlockEntity(pos)).applyDataFromItemToBlockEntity(stack);
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, AppliedCookingBlockEntities.KITCHEN_STATION.get(), KitchenStationBlockEntity::serverTick);
    }
}
