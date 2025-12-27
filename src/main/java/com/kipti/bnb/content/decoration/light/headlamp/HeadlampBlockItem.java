package com.kipti.bnb.content.decoration.light.headlamp;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class HeadlampBlockItem extends BlockItem {

    public HeadlampBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        BlockState oldState = context.getLevel().getBlockState(context.getClickedPos());
        boolean defaultResult = super.placeBlock(context, state);
        if (oldState.getBlock().equals(state.getBlock()) && oldState.getBlock() instanceof HeadlampBlock) {
            return true;
        }
        return defaultResult;
    }


}
