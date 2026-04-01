package com.kipti.bnb.content.decoration.dyeable.pipes;

import com.kipti.bnb.content.decoration.dyeable.DyeableTransitionHelper;
import com.kipti.bnb.registry.content.BnbAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Custom BlockItem for fluid pipes that sets the dye color immediately during placement,
 * eliminating the visual desync where connections appear incorrect for a tick.
 */
public class DyeablePipeBlockItem extends BlockItem {

    public DyeablePipeBlockItem(final Block block, final Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(final BlockPlaceContext context) {
        final @Nullable DyeColor offhandDye = getOffhandDyeColor(context);
        if (offhandDye != null) {
            BnbAdvancements.DYE_FLUID_COMPONENT.awardTo(context.getPlayer());
            DyeableTransitionHelper.savePendingPlacementColor(
                    context.getLevel(), context.getClickedPos(), offhandDye
            );
        }
        try {
            final InteractionResult result = super.place(context);
            if (result.consumesAction() && offhandDye != null && context.getLevel().isClientSide()) {
                final DyeablePipeBehaviour behaviour = BlockEntityBehaviour.get(
                        context.getLevel(), context.getClickedPos(), DyeablePipeBehaviour.TYPE
                );
                if (behaviour != null) {
                    behaviour.applyColorClientOnly(offhandDye);
                }
            }
            return result;
        } finally {
            if (offhandDye != null) {
                DyeableTransitionHelper.consumePendingPlacementColor(
                        context.getLevel(), context.getClickedPos()
                );
            }
        }
    }

    @Override
    protected boolean updateCustomBlockEntityTag(
            final BlockPos pos,
            final Level level,
            final @Nullable Player player,
            final ItemStack stack,
            final BlockState state
    ) {
        final boolean result = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        if (!level.isClientSide() && player != null) {
            final ItemStack offhand = player.getOffhandItem();
            if (offhand.getItem() instanceof final DyeItem dyeItem) {
                final DyeablePipeBehaviour behaviour = BlockEntityBehaviour.get(
                        level, pos, DyeablePipeBehaviour.TYPE
                );
                if (behaviour != null) {
                    behaviour.setColor(dyeItem.getDyeColor());
                }
            }
        }
        return result;
    }

    @Nullable
    private static DyeColor getOffhandDyeColor(final BlockPlaceContext context) {
        if (context.getPlayer() == null) {
            return null;
        }
        final ItemStack offhand = context.getPlayer().getOffhandItem();
        if (offhand.getItem() instanceof final DyeItem dyeItem) {
            return dyeItem.getDyeColor();
        }
        return null;
    }

}
