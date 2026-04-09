package com.kipti.bnb.content.decoration.grating;

import com.kipti.bnb.registry.content.blocks.encased.BnbSpecialEncasedBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GratingPanelBlock extends GratingBlock implements IGratingPanel {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public GratingPanelBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(final @NotNull BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, this.getPanelFacing(context.getPlayer()));
    }

    public BlockState getEncasedShaftState(final BlockState state, final Direction.Axis shaftAxis) {
        return BnbSpecialEncasedBlocks.INDUSTRIAL_GRATING_PANEL.getDefaultState()
                .setValue(GratingEncasedShaftBlock.AXIS, shaftAxis)
                .setValue(FACING, state.getValue(FACING));
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(final @NotNull ItemStack stack,
                                                       final @NotNull BlockState state,
                                                       final @NotNull Level level,
                                                       final @NotNull BlockPos pos,
                                                       final Player player,
                                                       final @NotNull InteractionHand hand,
                                                       final @NotNull BlockHitResult hitResult) {
        final ItemInteractionResult shaftInsertResult = this.tryInsertShaft(stack, state, level, pos, player, hand);
        if (shaftInsertResult.consumesAction()) {
            return shaftInsertResult;
        }

        final ItemInteractionResult pipeInsertResult = this.tryInsertPipe(stack, state, level, pos, player, hand);
        if (pipeInsertResult.consumesAction()) {
            return pipeInsertResult;
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected @NotNull VoxelShape getShape(final BlockState state,
                                           final @NotNull BlockGetter level,
                                           final @NotNull BlockPos pos,
                                           final @NotNull CollisionContext context) {
        return AllShapes.CASING_3PX.get(state.getValue(FACING));
    }

    @Override
    protected boolean isPathfindable(@NotNull final BlockState state,
                                     @NotNull final PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected boolean skipRendering(final BlockState state, final BlockState adjacentState, final Direction direction) {
        return adjacentState.getBlock() instanceof IGratingPanel && (adjacentState.getValue(FACING) == state.getValue(
                FACING)) || super.skipRendering(state, adjacentState, direction);
    }

    private Direction getPanelFacing(final @Nullable Player player) {
        return player == null ? Direction.UP : player.getNearestViewDirection().getOpposite();
    }

    public BlockState getEncasedPipeState(final BlockState state, final Level level, final BlockPos pos) {
        BlockState result = BnbSpecialEncasedBlocks.INDUSTRIAL_GRATING_PANEL_PIPE.getDefaultState()
                .setValue(FACING, state.getValue(FACING));
        for (Direction direction : Iterate.directions) {
            BlockPos neighborPos = pos.relative(direction);
            result = result.setValue(
                    PipeBlock.PROPERTY_BY_DIRECTION.get(direction),
                    FluidPipeBlock.canConnectTo(level, neighborPos, level.getBlockState(neighborPos), direction)
            );
        }
        return result;
    }

    private ItemInteractionResult tryInsertPipe(final ItemStack stack,
                                                final BlockState state,
                                                final Level level,
                                                final BlockPos pos,
                                                final Player player,
                                                final InteractionHand hand) {
        if (!AllBlocks.FLUID_PIPE.isIn(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        BlockState pipeState = this.getEncasedPipeState(state, level, pos);
        FluidTransportBehaviour.cacheFlows(level, pos);
        level.setBlockAndUpdate(pos, pipeState);
        FluidTransportBehaviour.loadFlows(level, pos);
        level.playSound(null, pos, SoundEvents.METAL_HIT, SoundSource.BLOCKS, 0.5f, 1.25f);
        if (!level.isClientSide && !player.isCreative()) {
            stack.shrink(1);
            if (stack.isEmpty()) {
                player.setItemInHand(hand, ItemStack.EMPTY);
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    private ItemInteractionResult tryInsertShaft(final ItemStack stack,
                                                 final BlockState state,
                                                 final Level level,
                                                 final BlockPos pos,
                                                 final Player player,
                                                 final InteractionHand hand) {
        if (!AllBlocks.SHAFT.isIn(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        KineticBlockEntity.switchToBlockState(
                level, pos, this.getEncasedShaftState(state, player.getNearestViewDirection().getAxis())
        );
        level.playSound(null, pos, SoundEvents.METAL_HIT, SoundSource.BLOCKS, 0.5f, 1.25f);
        if (!level.isClientSide && !player.isCreative()) {
            stack.shrink(1);
            if (stack.isEmpty()) {
                player.setItemInHand(hand, ItemStack.EMPTY);
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

}

