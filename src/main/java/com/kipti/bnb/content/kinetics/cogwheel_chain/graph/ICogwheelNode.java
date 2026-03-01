package com.kipti.bnb.content.kinetics.cogwheel_chain.graph;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * Shared interface for cogwheel node types used during chain placement ({@link PlacingCogwheelNode})
 * and in finalized chains ({@link PathedCogwheelNode}).
 */
public interface ICogwheelNode {

    BlockPos pos();

    Direction.Axis rotationAxis();

    boolean isLarge();

    boolean hasSmallCogwheelOffset();

    default Vec3 center() {
        return pos().getCenter();
    }

    default Vec3 rotationAxisVec() {
        return Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(rotationAxis(), Direction.AxisDirection.POSITIVE).getNormal());
    }
}
