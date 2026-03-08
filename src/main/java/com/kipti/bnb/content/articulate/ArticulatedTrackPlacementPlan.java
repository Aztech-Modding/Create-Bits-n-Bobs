package com.kipti.bnb.content.articulate;

import com.simibubi.create.content.trains.track.BezierConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class ArticulatedTrackPlacementPlan {

    private final List<Segment> segments;
    private final BlockPos startPos;
    private final BlockPos endPos;
    private final float startTilt;
    private final float endTilt;

    public ArticulatedTrackPlacementPlan(final List<Segment> segments, final BlockPos startPos, final BlockPos endPos,
                                         final float startTilt, final float endTilt) {
        this.segments = List.copyOf(segments);
        this.startPos = startPos.immutable();
        this.endPos = endPos.immutable();
        this.startTilt = startTilt;
        this.endTilt = endTilt;
    }

    public List<Segment> segments() {
        return segments;
    }

    public boolean isEmpty() {
        return segments.isEmpty();
    }

    public BlockPos startPos() {
        return startPos;
    }

    public BlockPos endPos() {
        return endPos;
    }

    public float startTilt() {
        return startTilt;
    }

    public float endTilt() {
        return endTilt;
    }

    public record Segment(BezierConnection curve, BlockState startState, BlockState endState) {
    }

}
