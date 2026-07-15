package com.kipti.bnb.content.decoration.strut;

import com.cake.struts.content.StrutModelType;

public class GirderStrutBlock extends BnbStrutBlock {

//    public static final BooleanProperty FLUSH_ANCHOR = BooleanProperty.create("flush_anchor");

    public GirderStrutBlock(final Properties properties, final StrutModelType modelType) {
        super(properties, modelType);
//        this.registerDefaultState(this.defaultBlockState().setValue(FLUSH_ANCHOR, false));
    }

//    @Override
//    public void setPlacedBy(final Level p_49847_, final BlockPos p_49848_, final BlockState p_49849_, @Nullable final LivingEntity p_49850_, final ItemStack p_49851_) {
//        super.setPlacedBy(p_49847_, p_49848_, p_49849_, p_49850_, p_49851_);
//        p_49847_.setBlock(p_49848_, this.withFlushState(p_49847_, p_49848_, p_49849_), 3);
//    }
//
//    @Override
//    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
//        super.createBlockStateDefinition(builder);
//        builder.add(FLUSH_ANCHOR);
//    }
//
//    @Override
//    public @NotNull BlockState updateShape(final BlockState oldState, @NotNull final Direction direction, @NotNull final BlockState neighbourState, @NotNull final LevelAccessor world, @NotNull final BlockPos pos, @NotNull final BlockPos neighbourPos) {
//        BlockState state = super.updateShape(oldState, direction, neighbourState, world, pos, neighbourPos);
//        state = this.withFlushState(world, pos, state);
//        return state;
//    }
//
//    private @NonNull BlockState withFlushState(@NonNull final LevelAccessor world, @NonNull final BlockPos pos, BlockState state) {
//        final Direction anchorFacing = state.getValue(FACING);
//        if (anchorFacing.getAxis().equals(Direction.Axis.Y)) {
//            final BlockState againstState = world.getBlockState(pos.relative(anchorFacing.getOpposite()));
//            state = state.setValue(FLUSH_ANCHOR, this.isVerticalFlushAgainst(againstState, anchorFacing.getOpposite()));
//        }
//        return state;
//    }
//
//    protected boolean isVerticalFlushAgainst(final BlockState againstState, final Direction side) {
//        if (againstState.getBlock() instanceof GirderBlock) {
//            return side == Direction.UP ? againstState.getValue(GirderBlock.TOP) : againstState.getValue(GirderBlock.BOTTOM);
//        }
//        return false;
//    }
}
