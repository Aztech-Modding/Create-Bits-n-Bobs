package com.kipti.bnb.content.decoration.truss;

import net.minecraft.world.item.BlockItem;

public class TrussBlockItem extends BlockItem {

    public TrussBlockItem(final TrussBlock block, final Properties properties) {
        super(block, properties);
    }

//    @Override
//    public @NotNull InteractionResult place(final BlockPlaceContext context) {
//        final InteractionResult result = super.place(context);
//        if (!result.consumesAction()) {
//            return result;
//        }
//
//        this.tryPlaceOffhandShaft(context);
//        return result;
//    }
//
//    private void tryPlaceOffhandShaft(final BlockPlaceContext context) {
//        final Player player = context.getPlayer();
//        if (player == null || context.getHand() != InteractionHand.MAIN_HAND) {
//            return;
//        }
//
//        final ItemStack offhandStack = player.getOffhandItem();
//        if (!AllBlocks.SHAFT.isIn(offhandStack)) {
//            return;
//        }
//
//        final Level level = context.getLevel();
//        final BlockState placedState = level.getBlockState(context.getClickedPos());
//        if (!(placedState.getBlock() instanceof final TrussBlock trussBlock)) {
//            return;
//        }
//
//        KineticBlockEntity.switchToBlockState(
//                level,
//                context.getClickedPos(),
//                trussBlock.getEncasedShaftState(placedState, player.getNearestViewDirection().getAxis())
//        );
//        level.playSound(null, context.getClickedPos(), SoundEvents.METAL_HIT, SoundSource.BLOCKS, 0.5f, 1.25f);
//        this.consumeOffhandShaft(level, player, offhandStack);
//    }
//
//    private void consumeOffhandShaft(final Level level, final Player player, final ItemStack offhandStack) {
//        if (level.isClientSide || player.isCreative()) {
//            return;
//        }
//
//        offhandStack.shrink(1);
//        if (offhandStack.isEmpty()) {
//            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
//        }
//    }
}
