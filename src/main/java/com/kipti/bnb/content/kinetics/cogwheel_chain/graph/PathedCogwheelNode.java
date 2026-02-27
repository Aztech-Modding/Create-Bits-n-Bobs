package com.kipti.bnb.content.kinetics.cogwheel_chain.graph;

import com.simibubi.create.content.contraptions.StructureTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public record PathedCogwheelNode(int side, boolean isLarge, Direction.Axis rotationAxis, BlockPos localPos,
                                 boolean hasSmallCogwheelOffset) implements ICogwheelNode {

    @Override
    public BlockPos pos() {
        return localPos;
    }

    public PathedCogwheelNode(final PlacingCogwheelNode partialNode, final int side) {
        this(side, partialNode.isLarge(), partialNode.rotationAxis(), partialNode.pos(), partialNode.hasSmallCogwheelOffset());
    }

    public void write(final CompoundTag nodeTag) {
        nodeTag.putBoolean("Side", side == 1);
        nodeTag.putBoolean("IsLarge", isLarge);
        nodeTag.putBoolean("OffsetForSmallCogwheel", hasSmallCogwheelOffset);
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

    public double dist(final PathedCogwheelNode other) {
        return center().distanceTo(other.center());
    }

    public PathedCogwheelNode transform(final StructureTransform transform) {
        final BlockPos transformedPos = transform.applyWithoutOffset(localPos);
        final Direction transformedAxisResult = transform.rotateFacing(Direction.fromAxisAndDirection(rotationAxis, Direction.AxisDirection.POSITIVE));
        return new PathedCogwheelNode(
                (transformedAxisResult.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : -1) * side,
                isLarge,
                transformedAxisResult.getAxis(),
                transformedPos,
                hasSmallCogwheelOffset
        );
    }
}

