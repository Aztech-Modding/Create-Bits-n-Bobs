package com.kipti.bnb.content.decoration.grating;

import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class GratingPanelCTBehaviour extends SimpleCTBehaviour {

    public GratingPanelCTBehaviour(final CTSpriteShiftEntry shift) {
        super(shift);
    }

    @Override
    public boolean connectsTo(final BlockState state, final BlockState other, final BlockAndTintGetter reader, final BlockPos pos,
                              final BlockPos otherPos, final Direction face) {
        if (!(other.getBlock() instanceof IGratingPanel))
            return false;
        if (!(state.getBlock() instanceof IGratingPanel))
            return false;
        return other.getValue(GratingPanelBlock.FACING) == state.getValue(GratingPanelBlock.FACING);
    }

    @Override
    public boolean buildContextForOccludedDirections() {
        return true;
    }
    
}

