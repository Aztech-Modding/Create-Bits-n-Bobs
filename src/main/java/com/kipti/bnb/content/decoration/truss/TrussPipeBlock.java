package com.kipti.bnb.content.decoration.truss;

import com.kipti.bnb.content.decoration.dyeable.DyeableTransitionHelper;
import com.kipti.bnb.registry.client.BnbShapes;
import com.kipti.bnb.registry.content.BnbBlockEntities;
import com.kipti.bnb.registry.content.blocks.deco.BnbDecorativeBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.decoration.encasing.EncasedBlock;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TrussPipeBlock extends FluidPipeBlock implements EncasedBlock, SpecialBlockItemRequirement {

    public static final EnumProperty<Direction.Axis> TRUSS_AXIS = EnumProperty.create(
            "truss_axis",
            Direction.Axis.class
    );
    public static final BooleanProperty ALTERNATING = TrussBlock.ALTERNATING;

    public TrussPipeBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                                          .setValue(TRUSS_AXIS, Direction.Axis.Y)
                                          .setValue(ALTERNATING, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TRUSS_AXIS, ALTERNATING);
    }

    @Override
    public @NotNull BlockState getStateForPlacement(final @NotNull BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        final Direction.Axis trussAxis = context.getNearestLookingDirection().getAxis();
        state = state.setValue(TRUSS_AXIS, trussAxis);
        final Direction positiveAxis = Direction.get(Direction.AxisDirection.POSITIVE, trussAxis);
        final BlockState neighborState = context.getLevel().getBlockState(context.getClickedPos().relative(positiveAxis));
        final boolean isAlternating = neighborState.hasProperty(ALTERNATING) && neighborState.getValue(ALTERNATING);
        return state.setValue(ALTERNATING, !isAlternating);
    }

    @Override
    public @NotNull BlockState updateShape(final @NotNull BlockState state,
                                           final @NotNull Direction direction,
                                           final @NotNull BlockState neighborState,
                                           final @NotNull LevelAccessor level,
                                           final @NotNull BlockPos pos,
                                           final @NotNull BlockPos neighborPos) {
        final Direction positiveAxis = Direction.get(Direction.AxisDirection.POSITIVE, state.getValue(TRUSS_AXIS));
        BlockState updatedState = state;
        if (direction == positiveAxis) {
            final boolean isAlternating = neighborState.hasProperty(ALTERNATING) && neighborState.getValue(ALTERNATING);
            updatedState = updatedState.setValue(ALTERNATING, !isAlternating);
        }
        return super.updateShape(updatedState, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public BlockEntityType<? extends FluidPipeBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.ENCASED_PIPE.get();
    }

    @Override
    public Block getCasing() {
        return BnbDecorativeBlocks.INDUSTRIAL_TRUSS.get();
    }

    @Override
    public void handleEncasing(final BlockState state,
                               final Level level,
                               final BlockPos pos,
                               final ItemStack heldItem,
                               final Player player,
                               final InteractionHand hand,
                               final BlockHitResult ray) {
        DyeableTransitionHelper.saveCurrentDye(level, pos);
        FluidTransportBehaviour.cacheFlows(level, pos);
        BlockState encased = EncasedPipeBlock.transferSixWayProperties(state, this.defaultBlockState())
                .setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
        final Direction.Axis trussAxis = player.getNearestViewDirection().getAxis();
        encased = encased.setValue(TRUSS_AXIS, trussAxis);
        final Direction positiveAxis = Direction.get(Direction.AxisDirection.POSITIVE, trussAxis);
        final BlockState neighborState = level.getBlockState(pos.relative(positiveAxis));
        final boolean isAlternating = neighborState.hasProperty(ALTERNATING) && neighborState.getValue(ALTERNATING);
        encased = encased.setValue(ALTERNATING, !isAlternating);
        level.setBlockAndUpdate(pos, encased);
        FluidTransportBehaviour.loadFlows(level, pos);
        DyeableTransitionHelper.applyPreviousDye(level, pos);
    }

    @Override
    public @NotNull InteractionResult onWrenched(final BlockState state, final UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onSneakWrenched(final BlockState state, final UseOnContext context) {
        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();

        level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
        level.setBlockAndUpdate(
                pos, BnbDecorativeBlocks.INDUSTRIAL_TRUSS.getDefaultState()
                        .setValue(RotatedPillarBlock.AXIS, state.getValue(TRUSS_AXIS))
                        .setValue(TrussBlock.ALTERNATING, state.getValue(ALTERNATING))
        );

        if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
            context.getPlayer().getInventory().placeItemBackInInventory(AllBlocks.FLUID_PIPE.asStack());
        }
        IWrenchable.playRemoveSound(level, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(final @NotNull BlockState state,
                                                final @NotNull HitResult target,
                                                final @NotNull LevelReader level,
                                                final @NotNull BlockPos pos,
                                                final @NotNull Player player) {
        return AllBlocks.FLUID_PIPE.asStack();
    }

    @Override
    public ItemRequirement getRequiredItems(final BlockState state, final @Nullable BlockEntity blockEntity) {
        return ItemRequirement.of(AllBlocks.FLUID_PIPE.getDefaultState(), blockEntity)
                .union(ItemRequirement.of(BnbDecorativeBlocks.INDUSTRIAL_TRUSS.getDefaultState(), blockEntity));
    }

    @Override
    protected @NotNull VoxelShape getShape(final @NotNull BlockState state,
                                           final @NotNull BlockGetter level,
                                           final @NotNull BlockPos pos,
                                           final @NotNull CollisionContext context) {
        return BnbShapes.ALTERNATING_TRUSS.get(state.getValue(TRUSS_AXIS));
    }

    @Override
    protected boolean isPathfindable(final @NotNull BlockState state,
                                     final @NotNull PathComputationType type) {
        return false;
    }

    @Override
    public @NotNull BlockState rotate(final @NotNull BlockState state, final @NotNull Rotation rotation) {
        BlockState rotated = super.rotate(state, rotation);
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
    public @NotNull BlockState transform(final BlockState state, final StructureTransform transform) {
        final Direction trussDir = Direction.fromAxisAndDirection(
                state.getValue(TRUSS_AXIS),
                Direction.AxisDirection.POSITIVE
        );
        final Direction newTrussDir = transform.rotateFacing(trussDir);
        return super.transform(state, transform).setValue(TRUSS_AXIS, newTrussDir.getAxis());
    }
}
