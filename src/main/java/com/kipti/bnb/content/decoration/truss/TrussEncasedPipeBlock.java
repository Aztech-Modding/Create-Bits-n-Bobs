package com.kipti.bnb.content.decoration.truss;

import com.kipti.bnb.registry.client.BnbShapes;
import com.kipti.bnb.registry.content.BnbBlockEntities;
import com.kipti.bnb.registry.content.blocks.deco.BnbDecorativeBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.contraption.transformable.TransformableBlock;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.decoration.encasing.EncasedBlock;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockRotation;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

public class TrussEncasedPipeBlock extends Block
        implements IWrenchable, SpecialBlockItemRequirement, IBE<FluidPipeBlockEntity>, EncasedBlock, TransformableBlock {

    public static final EnumProperty<Direction.Axis> TRUSS_AXIS = EnumProperty.create("truss_axis", Direction.Axis.class);
    public static final BooleanProperty ALTERNATING = TrussBlock.ALTERNATING;
    public static final Map<Direction, BooleanProperty> FACING_TO_PROPERTY_MAP = PipeBlock.PROPERTY_BY_DIRECTION;

    private final Supplier<Block> casing;

    public TrussEncasedPipeBlock(final Properties properties) {
        super(properties);
        this.casing = BnbDecorativeBlocks.INDUSTRIAL_TRUSS::get;
        BlockState defaultState = this.defaultBlockState()
                .setValue(TRUSS_AXIS, Direction.Axis.Y)
                .setValue(ALTERNATING, false);
        for (final Direction d : Direction.values()) {
            defaultState = defaultState.setValue(FACING_TO_PROPERTY_MAP.get(d), false);
        }
        this.registerDefaultState(defaultState);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TRUSS_AXIS, ALTERNATING);
        for (final Direction d : Iterate.directions) {
            builder.add(FACING_TO_PROPERTY_MAP.get(d));
        }
        super.createBlockStateDefinition(builder);
    }

    @Override
    public @NotNull BlockState getStateForPlacement(final @NotNull BlockPlaceContext context) {
        final Direction.Axis trussAxis = context.getNearestLookingDirection().getAxis();
        final BlockState state = this.defaultBlockState().setValue(TRUSS_AXIS, trussAxis);
        final Direction positiveAxis = Direction.get(Direction.AxisDirection.POSITIVE, trussAxis);
        final BlockState neighborState = context.getLevel().getBlockState(context.getClickedPos().relative(positiveAxis));
        final boolean isAlternating = neighborState.hasProperty(ALTERNATING) && neighborState.getValue(ALTERNATING);
        return state.setValue(ALTERNATING, !isAlternating);
    }

    @Override
    protected @NotNull BlockState updateShape(final @NotNull BlockState state,
                                              final @NotNull Direction direction,
                                              final @NotNull BlockState neighborState,
                                              final @NotNull LevelAccessor level,
                                              final @NotNull BlockPos pos,
                                              final @NotNull BlockPos neighborPos) {
        final Direction positiveAxis = Direction.get(Direction.AxisDirection.POSITIVE, state.getValue(TRUSS_AXIS));
        if (direction == positiveAxis) {
            final boolean isAlternating = neighborState.hasProperty(ALTERNATING) && neighborState.getValue(ALTERNATING);
            return state.setValue(ALTERNATING, !isAlternating);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void onPlace(final @NotNull BlockState state, final @NotNull Level world,
                           final @NotNull BlockPos pos, final @NotNull BlockState oldState, final boolean isMoving) {
        if (!world.isClientSide && state != oldState)
            world.scheduleTick(pos, this, 1, TickPriority.HIGH);
    }

    @Override
    protected void neighborChanged(final @NotNull BlockState state, final @NotNull Level world,
                                   final @NotNull BlockPos pos, final @NotNull Block otherBlock,
                                   final @NotNull BlockPos neighborPos, final boolean isMoving) {
        DebugPackets.sendNeighborsUpdatePacket(world, pos);
        final Direction d = FluidPropagator.validateNeighbourChange(state, world, pos, otherBlock, neighborPos, isMoving);
        if (d == null)
            return;
        if (!state.getValue(FACING_TO_PROPERTY_MAP.get(d)))
            return;
        world.scheduleTick(pos, this, 1, TickPriority.HIGH);
    }

    @Override
    protected void tick(final @NotNull BlockState state, final @NotNull ServerLevel world,
                        final @NotNull BlockPos pos, final @NotNull RandomSource r) {
        FluidPropagator.propagateChangedPipe(world, pos, state);
    }

    @Override
    protected void onRemove(final @NotNull BlockState state, final @NotNull Level world,
                            final @NotNull BlockPos pos, final @NotNull BlockState newState, final boolean isMoving) {
        final boolean blockTypeChanged = state.getBlock() != newState.getBlock();
        if (blockTypeChanged && !world.isClientSide)
            FluidPropagator.propagateChangedPipe(world, pos, state);
        if (state.hasBlockEntity() && (blockTypeChanged || !newState.hasBlockEntity()))
            world.removeBlockEntity(pos);
    }

    @Override
    public void setPlacedBy(final @NotNull Level level, final @NotNull BlockPos pos,
                            final @NotNull BlockState state, final LivingEntity placer,
                            final @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        AdvancementBehaviour.setPlacedBy(level, pos, placer);
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(final @NotNull BlockState state, final @NotNull HitResult target,
                                                final @NotNull LevelReader level, final @NotNull BlockPos pos,
                                                final @NotNull Player player) {
        return AllBlocks.FLUID_PIPE.asStack();
    }

    @Override
    public InteractionResult onWrenched(final BlockState state, final UseOnContext context) {
        final Level world = context.getLevel();
        final BlockPos pos = context.getClickedPos();

        if (world.isClientSide)
            return InteractionResult.SUCCESS;

        world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));

        final BlockState equivalentPipe = transferSixWayProperties(state, AllBlocks.FLUID_PIPE.getDefaultState());
        Direction firstFound = Direction.UP;
        for (final Direction d : Iterate.directions) {
            if (state.getValue(FACING_TO_PROPERTY_MAP.get(d))) {
                firstFound = d;
                break;
            }
        }

        FluidTransportBehaviour.cacheFlows(world, pos);
        world.setBlockAndUpdate(pos, AllBlocks.FLUID_PIPE.get()
                .updateBlockState(equivalentPipe, firstFound, null, world, pos));
        FluidTransportBehaviour.loadFlows(world, pos);

        if (context.getPlayer() != null && !context.getPlayer().isCreative())
            context.getPlayer().getInventory().placeItemBackInInventory(BnbDecorativeBlocks.INDUSTRIAL_TRUSS.asStack());

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onSneakWrenched(final BlockState state, final UseOnContext context) {
        return this.onWrenched(state, context);
    }

    @Override
    public void handleEncasing(final BlockState state, final Level level, final BlockPos pos,
                               final ItemStack heldItem, final Player player, final InteractionHand hand,
                               final BlockHitResult ray) {
        FluidTransportBehaviour.cacheFlows(level, pos);
        BlockState encased = transferSixWayProperties(state, this.defaultBlockState());
        final Direction.Axis trussAxis = player.getNearestViewDirection().getAxis();
        encased = encased.setValue(TRUSS_AXIS, trussAxis);
        final Direction positiveAxis = Direction.get(Direction.AxisDirection.POSITIVE, trussAxis);
        final BlockState neighborState = level.getBlockState(pos.relative(positiveAxis));
        final boolean isAlternating = neighborState.hasProperty(ALTERNATING) && neighborState.getValue(ALTERNATING);
        encased = encased.setValue(ALTERNATING, !isAlternating);
        level.setBlockAndUpdate(pos, encased);
        FluidTransportBehaviour.loadFlows(level, pos);
    }

    public static BlockState transferSixWayProperties(final BlockState from, BlockState to) {
        for (final Direction d : Iterate.directions) {
            final BooleanProperty property = FACING_TO_PROPERTY_MAP.get(d);
            to = to.setValue(property, from.getValue(property));
        }
        return to;
    }

    @Override
    public Block getCasing() {
        return this.casing.get();
    }

    @Override
    public ItemRequirement getRequiredItems(final BlockState state, final BlockEntity be) {
        return ItemRequirement.of(AllBlocks.FLUID_PIPE.getDefaultState(), be)
                .union(ItemRequirement.of(BnbDecorativeBlocks.INDUSTRIAL_TRUSS.getDefaultState(), be));
    }

    @Override
    public Class<FluidPipeBlockEntity> getBlockEntityClass() {
        return FluidPipeBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FluidPipeBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.ENCASED_PIPE.get();
    }

    @Override
    protected @NotNull VoxelShape getShape(final @NotNull BlockState state, final @NotNull BlockGetter level,
                                           final @NotNull BlockPos pos, final @NotNull CollisionContext context) {
        return BnbShapes.ALTERNATING_TRUSS.get(state.getValue(TRUSS_AXIS));
    }

    @Override
    protected boolean isPathfindable(final @NotNull BlockState state, final @NotNull PathComputationType type) {
        return false;
    }

    @Override
    public @NotNull BlockState rotate(final @NotNull BlockState state, final @NotNull Rotation rotation) {
        BlockState rotated = FluidPipeBlockRotation.rotate(state, rotation);
        final Direction.Axis trussAxis = state.getValue(TRUSS_AXIS);
        if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) {
            if (trussAxis == Direction.Axis.X) {
                rotated = rotated.setValue(TRUSS_AXIS, Direction.Axis.Z);
            } else if (trussAxis == Direction.Axis.Z) {
                rotated = rotated.setValue(TRUSS_AXIS, Direction.Axis.X);
            }
        }
        return rotated;
    }

    @Override
    public @NotNull BlockState mirror(final @NotNull BlockState state, final @NotNull Mirror mirror) {
        return FluidPipeBlockRotation.mirror(state, mirror);
    }

    @Override
    public BlockState transform(final BlockState state, final StructureTransform transform) {
        final BlockState transformed = FluidPipeBlockRotation.transform(state, transform);
        final Direction trussDir = Direction.fromAxisAndDirection(state.getValue(TRUSS_AXIS), Direction.AxisDirection.POSITIVE);
        final Direction newTrussDir = transform.rotateFacing(trussDir);
        return transformed.setValue(TRUSS_AXIS, newTrussDir.getAxis());
    }
}
