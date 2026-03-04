package com.kipti.bnb.content.decoration.grating;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class GratingBlock extends Block implements IWrenchable {

    public GratingBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected boolean propagatesSkylightDown(final @NotNull BlockState state, final @NotNull BlockGetter level, final @NotNull BlockPos pos) {
        return true;
    }

    @Override
    protected boolean skipRendering(final @NotNull BlockState state, final BlockState adjacentState, final @NotNull Direction direction) {
        return adjacentState.getBlock() instanceof GratingBlock && !(adjacentState.getBlock() instanceof GratingPanelBlock) || super.skipRendering(state, adjacentState, direction);
    }
}

