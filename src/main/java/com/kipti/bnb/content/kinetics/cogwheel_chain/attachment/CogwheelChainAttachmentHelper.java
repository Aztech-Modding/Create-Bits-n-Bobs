package com.kipti.bnb.content.kinetics.cogwheel_chain.attachment;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.segment.CogwheelChainSegment;
import com.kipti.bnb.content.kinetics.cogwheel_chain.shape.ChainDriveShapeHelper;
import com.kipti.bnb.content.kinetics.cogwheel_chain.shape.CogwheelChainWholeShape;
import com.kipti.bnb.content.kinetics.cogwheel_chain.world.CogwheelChainWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Resolves the nearest valid chain attachment point from either a world position
 * (closest-point projection) or a ray intersection (reusing the OBB intersection
 * logic from {@link CogwheelChainWholeShape}).
 */
public class CogwheelChainAttachmentHelper {

    /**
     * Finds the nearest chain attachment to the given world position by projecting
     * onto every segment of every loaded chain.
     *
     * @return the attachment with the smallest distance, or {@code null} if no chains exist
     */
    @Nullable
    public static CogwheelChainAttachment findNearestAttachment(final Level level, final Vec3 worldPos) {
        final CogwheelChainWorld chainWorld = CogwheelChainWorld.get(level);

        double bestDistSq = Double.MAX_VALUE;
        BlockPos bestController = null;
        float bestChainDist = 0;

        for (final Map.Entry<BlockPos, CogwheelChain> entry : chainWorld.entries()) {
            final BlockPos controllerPos = entry.getKey();
            final CogwheelChain chain = entry.getValue();
            final List<CogwheelChainSegment> segments = chain.getSegments();
            if (segments.isEmpty()) continue;

            final Vec3 controllerBase = Vec3.atLowerCornerOf(controllerPos);

            for (final CogwheelChainSegment segment : segments) {
                final Vec3 from = segment.fromPosition().add(controllerBase);
                final Vec3 to = segment.toPosition().add(controllerBase);
                final float chainDist = projectOntoSegment(worldPos, from, to, segment);

                final Vec3 closest = interpolateWorld(from, to, segment, chainDist);
                final double distSq = worldPos.distanceToSqr(closest);
                if (distSq < bestDistSq) {
                    bestDistSq = distSq;
                    bestController = controllerPos;
                    bestChainDist = chainDist;
                }
            }
        }

        if (bestController == null) return null;
        return new CogwheelChainAttachment(bestController, bestChainDist);
    }

    /**
     * Finds the nearest chain attachment by casting a ray and using
     * {@link CogwheelChainWholeShape#intersect} for each chain, mirroring the approach
     * in {@link com.kipti.bnb.content.kinetics.cogwheel_chain.shape.CogwheelChainInteractionHandler}.
     *
     * @param origin      ray origin in world space
     * @param direction   ray direction (does not need to be normalized)
     * @param maxDistance maximum reach of the ray in blocks
     * @return the closest intersecting attachment, or {@code null} if none within range
     */
    @Nullable
    public static CogwheelChainAttachment findNearestAttachment(final Level level,
                                                                final Vec3 origin,
                                                                final Vec3 direction,
                                                                final double maxDistance) {
        final Vec3 normalizedDir = direction.normalize();
        final Vec3 target = origin.add(normalizedDir.scale(maxDistance));

        final ChainDriveShapeHelper.ChainShapeHit hit = ChainDriveShapeHelper.findClosestRayHit(
                level, origin, target, maxDistance * maxDistance);
        if (hit == null) {
            return null;
        }
        return new CogwheelChainAttachment(hit.controllerPos(), hit.chainPosition());
    }

    private static float projectOntoSegment(final Vec3 worldPos,
                                            final Vec3 from,
                                            final Vec3 to,
                                            final CogwheelChainSegment segment) {
        final Vec3 ab = to.subtract(from);
        final Vec3 ap = worldPos.subtract(from);
        final double lenSq = ab.lengthSqr();
        final double t = lenSq > 0 ? Mth.clamp(ap.dot(ab) / lenSq, 0, 1) : 0;
        return segment.startDist() + (float) (t * segment.length());
    }

    private static Vec3 interpolateWorld(final Vec3 from,
                                         final Vec3 to,
                                         final CogwheelChainSegment segment,
                                         final float chainDist) {
        final float t = segment.length() > 0
                ? Mth.clamp((chainDist - segment.startDist()) / segment.length(), 0, 1)
                : 0;
        return from.lerp(to, t);
    }
}
