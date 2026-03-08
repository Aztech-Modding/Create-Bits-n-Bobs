package com.kipti.bnb.content.articulate;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class ArticulatedTrackLogic {

    public static PlacementSegmentPlan planSegmentBreakpoints(final double totalLength,
                                                              final double end1Extent, final double axis1Length,
                                                              final double end2Extent, final double axis2Length,
                                                              final float startTilt, final float endTilt) {
        final double curveStartDistance = end1Extent * axis1Length;
        final double curveEndDistance = totalLength - end2Extent * axis2Length;
        final float curveStartTilt = ArticulatedTrackUtils.snapToNearest(totalLength <= 0d
                ? startTilt
                : ArticulatedTrackUtils.interpolateTilt((float) Mth.clamp(curveStartDistance / totalLength, 0.0d, 1.0d), startTilt, endTilt));
        final float curveEndTilt = ArticulatedTrackUtils.snapToNearest(totalLength <= 0d
                ? endTilt
                : ArticulatedTrackUtils.interpolateTilt((float) Mth.clamp(curveEndDistance / totalLength, 0.0d, 1.0d), startTilt, endTilt));
        return new PlacementSegmentPlan(end1Extent > 0d, curveStartDistance, curveStartTilt, curveEndDistance, curveEndTilt, end2Extent > 0d);
    }

    public static Vec3 outwardStraightJoinAxis(final Vec3 curveAxis) {
        return curveAxis.normalize().scale(-1d);
    }

    public static StraightTiltProfile straightTiltProfile(final Vec3 startNormal, final Vec3 startDirection,
                                                          final Vec3 endNormal, final Vec3 endDirection) {
        return new StraightTiltProfile(
                ArticulatedTrackUtils.extractCanonicalTiltDegrees(startNormal, startDirection),
                ArticulatedTrackUtils.extractCanonicalTiltDegrees(endNormal, endDirection)
        );
    }

    public record PlacementSegmentPlan(boolean hasEntryStraight, double curveStartDistance, float curveStartTilt,
                                       double curveEndDistance, float curveEndTilt, boolean hasExitStraight) {

        public int segmentCount() {
            return 1 + (hasEntryStraight ? 1 : 0) + (hasExitStraight ? 1 : 0);
        }
    }

    public record StraightTiltProfile(float startTilt, float endTilt) {

        public float tiltAt(final double t) {
            return ArticulatedTrackUtils.interpolateTilt((float) Mth.clamp(t, 0.0d, 1.0d), startTilt, endTilt);
        }
    }

}
