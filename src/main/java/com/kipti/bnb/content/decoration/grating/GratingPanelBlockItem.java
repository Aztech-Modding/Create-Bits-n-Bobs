package com.kipti.bnb.content.decoration.grating;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class GratingPanelBlockItem extends BlockItem {

    public GratingPanelBlockItem(final GratingPanelBlock block, final Properties properties) {
        super(block, properties);
    }

    @Override
    public @NotNull InteractionResult place(final BlockPlaceContext context) {
        final InteractionResult result = super.place(context);
        if (!result.consumesAction()) {
            return result;
        }

        this.tryPlaceOffhandShaft(context);
        return result;
    }

    private void tryPlaceOffhandShaft(final BlockPlaceContext context) {
        final Player player = context.getPlayer();
        if (player == null || context.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        final ItemStack offhandStack = player.getOffhandItem();
        if (!AllBlocks.SHAFT.isIn(offhandStack)) {
            return;
        }

        final Level level = context.getLevel();
        final BlockState placedState = level.getBlockState(context.getClickedPos());
        if (!(placedState.getBlock() instanceof final GratingPanelBlock gratingPanelBlock)) {
            return;
        }

        KineticBlockEntity.switchToBlockState(
                level,
                context.getClickedPos(),
                gratingPanelBlock.getEncasedShaftState(placedState, player.getNearestViewDirection().getAxis())
        );
        level.playSound(null, context.getClickedPos(), SoundEvents.METAL_HIT, SoundSource.BLOCKS, 0.5f, 1.25f);
        this.consumeOffhandShaft(level, player, offhandStack);
    }

    private void consumeOffhandShaft(final Level level, final Player player, final ItemStack offhandStack) {
        if (level.isClientSide || player.isCreative()) {
            return;
        }

        offhandStack.shrink(1);
        if (offhandStack.isEmpty()) {
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        }
    }
}
