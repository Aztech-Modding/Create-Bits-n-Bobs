package com.kipti.bnb.content.cogwheel_chain.graph;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public record PathedCogwheelNode(int side, boolean isLarge, Direction.Axis rotationAxis, BlockPos localPos,
                                 boolean offsetForSmallCogwheel) {

    public PathedCogwheelNode(final PlacingCogwheelNode partialNode, final int side) {
        this(side, partialNode.isLarge(), partialNode.rotationAxis(), partialNode.pos(), partialNode.hasOffsetForSmallCogwheel());
    }

    public void write(final CompoundTag nodeTag) {
        nodeTag.putBoolean("Side", side == 1);
        nodeTag.putBoolean("IsLarge", isLarge);
        nodeTag.putBoolean("OffsetForSmallCogwheel", offsetForSmallCogwheel);
        nodeTag.putInt("OffsetX", localPos.getX());
        nodeTag.putInt("OffsetY", localPos.getY());
        nodeTag.putInt("OffsetZ", localPos.getZ());
        nodeTag.putInt("RotationAxis", rotationAxis.ordinal());
    }

    public static PathedCogwheelNode read(final CompoundTag nodeTag) {
        final int side = nodeTag.getBoolean("Side") ? 1 : -1;
        final boolean isLarge = nodeTag.getBoolean("IsLarge");
        final BlockPos offset = new BlockPos(
                nodeTag.getInt("OffsetX"),
                nodeTag.getInt("OffsetY"),
                nodeTag.getInt("OffsetZ")
        );
        final Direction.Axis rotationAxis = Direction.Axis.values()[nodeTag.getInt("RotationAxis")];


        final boolean offsetForSmallCogwheel;
        if (nodeTag.contains("OffsetForSmallCogwheel")) {
            offsetForSmallCogwheel = nodeTag.getBoolean("OffsetForSmallCogwheel");
        } else {
            offsetForSmallCogwheel = !isLarge;
        }

        return new PathedCogwheelNode(side, isLarge, rotationAxis, offset, offsetForSmallCogwheel);
    }

    public float sideFactor() {
        return side * (isLarge ? 1 : 0.5f);
    }

    public Vec3 center() {
        return localPos.getCenter();
    }

    public Vec3 rotationAxisVec() {
        switch (rotationAxis) {
            case X -> {
                return new Vec3(1, 0, 0);
            }
            case Y -> {
                return new Vec3(0, 1, 0);
            }
            case Z -> {
                return new Vec3(0, 0, 1);
            }
        }
        return Vec3.ZERO;
    }

    public double dist(final PathedCogwheelNode other) {
        return center().distanceTo(other.center());
    }
}
