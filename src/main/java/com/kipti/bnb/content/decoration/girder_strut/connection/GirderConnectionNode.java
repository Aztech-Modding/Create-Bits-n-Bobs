package com.kipti.bnb.content.decoration.girder_strut.connection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record GirderConnectionNode(BlockPos relativeOffset, Direction peerFacing) {

    public static GirderConnectionNode fromAbsolute(final BlockPos origin, final BlockPos otherPos, final Direction otherFacing) {
        return new GirderConnectionNode(otherPos.subtract(origin).immutable(), otherFacing);
    }

    public BlockPos absoluteFrom(final BlockPos origin) {
        return origin.offset(relativeOffset);
    }
}
