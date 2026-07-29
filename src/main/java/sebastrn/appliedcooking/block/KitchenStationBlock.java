package sebastrn.appliedcooking.block;

import com.mojang.serialization.MapCodec;
import net.blay09.mods.cookingforblockheads.block.BaseKitchenBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import sebastrn.appliedcooking.AppliedCookingBlockEntities;
import sebastrn.appliedcooking.blockentity.KitchenStationBlockEntity;

public class KitchenStationBlock extends BaseKitchenBlock {

    public static final MapCodec<KitchenStationBlock> CODEC = simpleCodec(KitchenStationBlock::new);

    // Single-box hitbox sized to the model's overall extent. SHAPE_NORTH is the raw model (facing=north, y=0 in the
    // blockstate); the other three are it rotated about the block centre to match the blockstate's y-rotation, so the
    // hitbox and model always turn together. The top (y=12) is the peak of the back-tilted 40-degree screen; refit if
    // the screen angle changes.
    private static final VoxelShape SHAPE_NORTH = Block.box(1, 0, 1.5, 15, 12, 14);
    private static final VoxelShape SHAPE_EAST = Block.box(2, 0, 1, 14.5, 12, 15);
    private static final VoxelShape SHAPE_SOUTH = Block.box(1, 0, 2, 15, 12, 14.5);
    private static final VoxelShape SHAPE_WEST = Block.box(1.5, 0, 1, 14, 12, 15);
    /**
     * The Kitchen Station's link/connection state, set by the block entity each tick and read by the blockstate
     * models, the item, and the Jade/TOP tooltips. Three values so the model can show every case distinctly:
     * <ul>
     *   <li>{@link LinkState#UNLINKED} - no access point saved (dark screen).</li>
     *   <li>{@link LinkState#LINKED_OFFLINE} - linked, but the network is unreachable/unpowered (red-amber screen).</li>
     *   <li>{@link LinkState#ONLINE} - linked and drawing power from a live grid (purple screen, full glow).</li>
     * </ul>
     * Replaces the old {@code connected} boolean, which folded the linked-but-unreachable case into "disconnected".
     */
    public enum LinkState implements StringRepresentable {
        UNLINKED("unlinked"),
        LINKED_OFFLINE("linked_offline"),
        ONLINE("online");

        private final String name;

        LinkState(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final EnumProperty<LinkState> LINK_STATE = EnumProperty.create("link_state", LinkState.class);

    public KitchenStationBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(LINK_STATE, LinkState.UNLINKED));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LINK_STATE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING)) {
            case NORTH:
                return SHAPE_NORTH;
            case WEST:
                return SHAPE_WEST;
            case EAST:
                return SHAPE_EAST;
            case SOUTH:
            default:
                return SHAPE_SOUTH;
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

        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof KitchenStationBlockEntity kitchenStation) {
            kitchenStation.applyDataFromItemToBlockEntity(stack);
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, AppliedCookingBlockEntities.KITCHEN_STATION.get(), KitchenStationBlockEntity::serverTick);
    }
}
