package com.kipti.bnb.content.decoration.truss;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public class TrussBlock extends RotatedPillarBlock {

    public TrussBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected boolean propagatesSkylightDown(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return true;
    }
}
