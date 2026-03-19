package com.kipti.bnb.content.kinetics.cogwheel_chain.segment;

import net.minecraft.world.phys.Vec3;

/**
 * Represents a continuous portion of a cogwheel chain between two rendered path positions.
 * Each segment carries typed metadata ({@link SegmentType}) and cumulative distance information
 * for chain traversal and carriage constraint systems.
 *
 * @param fromPosition start of the segment in world-relative space
 * @param toPosition   end of the segment in world-relative space
 * @param type         whether this segment arcs around a cogwheel or spans between two cogwheels
 * @param startDist    cumulative distance from chain origin to the start of this segment
 * @param endDist      cumulative distance from chain origin to the end of this segment
 */
public record CogwheelChainSegment(Vec3 fromPosition, Vec3 toPosition, SegmentType type,
                                   float startDist, float endDist) {

    private static final double EPSILON = 1e-4;

    public enum SegmentType {
        /** Segment follows a curved arc around a single cogwheel. */
        NODE_ARC,
        /** Straight segment spanning between two different cogwheel positions. */
        BETWEEN_NODES
    }

    /**
     * @return the length of this individual segment
     */
    public float length() {
        return this.endDist - this.startDist;
    }

    /**
     * Returns {@code true} if this segment has meaningful vertical displacement.
     * This is the primary guard for carriage movement constraints.
     */
    public boolean hasVerticalDisplacement() {
        return Math.abs(this.toPosition.y - this.fromPosition.y) > EPSILON;
    }
}
