package com.kipti.bnb.content.kinetics.cogwheel_chain.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface IExclusiveCogwheelChainBlock {
    boolean isLargeCog();

    Direction.Axis getRotationAxis(BlockState state);
}

