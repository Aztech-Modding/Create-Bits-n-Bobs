package com.kipti.bnb.content.decoration.light.headlamp;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class HeadlampBlockItem extends BlockItem {

    public HeadlampBlockItem(final Block block, final Properties properties) {
        super(block, properties);
    }

    @Override //TODO FIX SHIFT-PLACE
    protected boolean canPlace(final BlockPlaceContext context, final BlockState state) {
        return super.canPlace(context, state) || context.getLevel().getBlockState(context.getClickedPos()).getBlock() instanceof HeadlampBlock;
    }

    protected boolean placeBlock(final BlockPlaceContext context, final BlockState state) {
        final BlockState oldState = context.getLevel().getBlockState(context.getClickedPos());
        final boolean defaultResult = super.placeBlock(context, state);
        if (oldState.getBlock().equals(state.getBlock()) && oldState.getBlock() instanceof HeadlampBlock) {
            return true;
        }
        return defaultResult;
    }


}

