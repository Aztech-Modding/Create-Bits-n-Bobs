package com.kipti.bnb.content.decoration.grating;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GratingBlock extends Block implements IWrenchable {

    public GratingBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected boolean propagatesSkylightDown(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return true;
    }

    @Override
    protected boolean skipRendering(final BlockState state, final BlockState adjacentState, final Direction direction) {
        return adjacentState.getBlock() instanceof GratingBlock && !(adjacentState.getBlock() instanceof GratingPanelBlock) || super.skipRendering(state, adjacentState, direction);
    }
}
