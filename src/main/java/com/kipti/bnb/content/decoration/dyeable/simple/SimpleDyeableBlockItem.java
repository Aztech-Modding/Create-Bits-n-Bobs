package com.kipti.bnb.content.decoration.dyeable.simple;

import com.kipti.bnb.content.decoration.dyeable.DyeableBlockItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SimpleDyeableBlockItem extends BlockItem {

    public SimpleDyeableBlockItem(final Block block, final Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(final BlockPlaceContext context) {
        final @Nullable DyeColor offhandDye = DyeableBlockItemHelper.getOffhandDyeColor(context);
        if (offhandDye != null) {
            DyeableBlockItemHelper.savePendingPlacementColor(context, context.getClickedPos(), offhandDye);
        }
        try {
            final InteractionResult result = super.place(context);
            if (result.consumesAction() && offhandDye != null) {
                DyeableBlockItemHelper.applyColorClientOnly(
                        context.getLevel(),
                        context.getClickedPos(),
                        SimpleDyeableBehaviour.TYPE,
                        offhandDye
                );
            }
            return result;
        } finally {
            if (offhandDye != null) {
                DyeableBlockItemHelper.consumePendingPlacementColor(context.getLevel(), context.getClickedPos());
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
        if (player != null) {
            final DyeColor color = DyeableBlockItemHelper.getOffhandDyeColor(player);
            if (color != null) {
                DyeableBlockItemHelper.setColor(level, pos, SimpleDyeableBehaviour.TYPE, color);
            }
        }
        return result;
    }

}
