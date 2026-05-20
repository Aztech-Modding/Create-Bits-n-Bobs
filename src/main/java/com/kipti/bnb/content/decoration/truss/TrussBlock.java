package com.kipti.bnb.content.decoration.truss;

import com.kipti.bnb.registry.client.BnbShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;

public class TrussBlock extends RotatedPillarBlock implements IWrenchable {

    public TrussBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull VoxelShape getShape(final BlockState p_60555_,
                                           final @NonNull BlockGetter p_60556_,
                                           final @NonNull BlockPos p_60557_,
                                           final @NonNull CollisionContext p_60558_) {
        return BnbShapes.TRUSS.get(p_60555_.getValue(AXIS));
    }

//    @Override
//    protected @NotNull ItemInteractionResult useItemOn(final @NonNull ItemStack stack,
//                                                       final @NonNull BlockState state,
//                                                       final @NonNull Level level,
//                                                       final @NonNull BlockPos pos,
//                                                       final @NonNull Player player,
//                                                       final @NonNull InteractionHand hand,
//                                                       final @NonNull BlockHitResult hitResult) {
//        if (AllBlocks.SHAFT.isIn(stack)) {
//            final Direction.Axis shaftAxis = player.getNearestViewDirection().getAxis();
//            KineticBlockEntity.switchToBlockState(level, pos, this.getEncasedShaftState(state, shaftAxis));
//            level.playSound(null, pos, SoundEvents.METAL_HIT, SoundSource.BLOCKS, 0.5f, 1.25f);
//            if (!level.isClientSide && !player.isCreative()) {
//                stack.shrink(1);
//                if (stack.isEmpty())
//                    player.setItemInHand(hand, ItemStack.EMPTY);
//            }
//            return ItemInteractionResult.SUCCESS;
//        }
//        if (AllBlocks.FLUID_PIPE.isIn(stack)) {
//            final BlockState pipeState = this.getEncasedPipeState(state, level, pos, player);
//            FluidTransportBehaviour.cacheFlows(level, pos);
//            level.setBlockAndUpdate(pos, pipeState);
//            FluidTransportBehaviour.loadFlows(level, pos);
//            level.playSound(null, pos, SoundEvents.METAL_HIT, SoundSource.BLOCKS, 0.5f, 1.25f);
//            if (!level.isClientSide && !player.isCreative()) {
//                stack.shrink(1);
//                if (stack.isEmpty())
//                    player.setItemInHand(hand, ItemStack.EMPTY);
//            }
//            return ItemInteractionResult.SUCCESS;
//        }
//        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
//    }
//
//    public BlockState getEncasedShaftState(final BlockState state, final Direction.Axis shaftAxis) {
//        return BnbDecorativeBlocks.INDUSTRIAL_TRUSS_ENCASED_SHAFT.getDefaultState()
//                .setValue(RotatedPillarBlock.AXIS, shaftAxis)
//                .setValue(TrussEncasedShaftBlock.TRUSS_AXIS, state.getValue(AXIS))
//                .setValue(TrussEncasedShaftBlock.ALTERNATING, state.getValue(ALTERNATING));
//    }
//
//    public BlockState getEncasedPipeState(final BlockState state, final Level level, final BlockPos pos,
//                                            final Player player) {
//        BlockState result = BnbDecorativeBlocks.INDUSTRIAL_TRUSS_ENCASED_PIPE.getDefaultState()
//                .setValue(TrussPipeBlock.TRUSS_AXIS, state.getValue(AXIS))
//                .setValue(TrussPipeBlock.ALTERNATING, state.getValue(ALTERNATING));
//
//        final Direction.Axis trussAxis = state.getValue(AXIS);
//        final Direction positiveStrut = Direction.get(Direction.AxisDirection.POSITIVE, trussAxis);
//        final Direction negativeStrut = Direction.get(Direction.AxisDirection.NEGATIVE, trussAxis);
//
//        final boolean strutPositive = FluidPipeBlock.canConnectTo(level, pos.relative(positiveStrut),
//                level.getBlockState(pos.relative(positiveStrut)), positiveStrut);
//        final boolean strutNegative = FluidPipeBlock.canConnectTo(level, pos.relative(negativeStrut),
//                level.getBlockState(pos.relative(negativeStrut)), negativeStrut);
//        final boolean hasStrutConnection = strutPositive || strutNegative;
//
//        if (hasStrutConnection) {
//            result = result.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(positiveStrut), strutPositive);
//            result = result.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(negativeStrut), strutNegative);
//            for (final Direction d : Direction.values()) {
//                if (d.getAxis() == trussAxis) continue;
//                result = result.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(d),
//                        FluidPipeBlock.canConnectTo(level, pos.relative(d),
//                                level.getBlockState(pos.relative(d)), d));
//            }
//            return result;
//        }
//
//        boolean hasAdjacentPipe = false;
//        for (final Direction d : Direction.values()) {
//            final boolean canConnect = FluidPipeBlock.canConnectTo(level, pos.relative(d),
//                    level.getBlockState(pos.relative(d)), d);
//            result = result.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(d), canConnect);
//            if (canConnect) {
//                hasAdjacentPipe = true;
//            }
//        }
//
//        if (!hasAdjacentPipe && player != null) {
//            final Direction playerDirection = player.getNearestViewDirection();
//            result = result.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(playerDirection), true);
//            result = result.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(playerDirection.getOpposite()), true);
//        }
//
//        return result;
//    }
}

