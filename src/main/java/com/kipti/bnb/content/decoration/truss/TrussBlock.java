package com.kipti.bnb.content.decoration.truss;

import com.kipti.bnb.registry.client.BnbShapes;
import com.kipti.bnb.registry.content.blocks.deco.BnbDecorativeBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class TrussBlock extends RotatedPillarBlock {

    public static final BooleanProperty ALTERNATING = BooleanProperty.create("alternating");

    public TrussBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(ALTERNATING, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ALTERNATING);
    }

    @Override
    protected @NotNull BlockState updateShape(final BlockState state,
                                              final @NotNull Direction direction,
                                              final @NotNull BlockState neighborState,
                                              final @NotNull LevelAccessor level,
                                              final @NotNull BlockPos pos,
                                              final @NotNull BlockPos neighborPos) {
        final Direction positiveAxis = Direction.get(Direction.AxisDirection.POSITIVE, state.getValue(AXIS));
        if (direction == positiveAxis) {
            final boolean isAlternating = neighborState.hasProperty(ALTERNATING) && neighborState.getValue(ALTERNATING);
            return state.setValue(ALTERNATING, !isAlternating);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public @NotNull BlockState getStateForPlacement(final @NotNull BlockPlaceContext context) {
        final BlockState state = super.getStateForPlacement(context);
        final Direction positiveAxis = Direction.get(Direction.AxisDirection.POSITIVE, state.getValue(AXIS));
        final BlockState neighborState = context.getLevel().getBlockState(context.getClickedPos().relative(positiveAxis));
        final boolean isAlternating = neighborState.hasProperty(ALTERNATING) && neighborState.getValue(ALTERNATING);
        return state.setValue(ALTERNATING, !isAlternating);
    }

    @Override
    protected @NonNull VoxelShape getShape(final BlockState p_60555_,
                                           final @NonNull BlockGetter p_60556_,
                                           final @NonNull BlockPos p_60557_,
                                           final @NonNull CollisionContext p_60558_) {
        return BnbShapes.ALTERNATING_TRUSS.get(p_60555_.getValue(AXIS));
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(final @NonNull ItemStack stack,
                                                       final @NonNull BlockState state,
                                                       final @NonNull Level level,
                                                       final @NonNull BlockPos pos,
                                                       final @NonNull Player player,
                                                       final @NonNull InteractionHand hand,
                                                       final @NonNull BlockHitResult hitResult) {
        if (AllBlocks.SHAFT.isIn(stack)) {
            final Direction.Axis shaftAxis = player.getNearestViewDirection().getAxis();
            KineticBlockEntity.switchToBlockState(
                    level, pos, BnbDecorativeBlocks.INDUSTRIAL_TRUSS_ENCASED_SHAFT.getDefaultState()
                            .setValue(RotatedPillarBlock.AXIS, shaftAxis)
                            .setValue(TrussEncasedShaftBlock.TRUSS_AXIS, state.getValue(AXIS))
                            .setValue(TrussEncasedShaftBlock.ALTERNATING, state.getValue(ALTERNATING))
            );
            level.playSound(null, pos, SoundEvents.METAL_HIT, SoundSource.BLOCKS, 0.5f, 1.25f);
            if (!level.isClientSide && !player.isCreative()) {
                stack.shrink(1);
                if (stack.isEmpty())
                    player.setItemInHand(hand, ItemStack.EMPTY);
            }
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}

