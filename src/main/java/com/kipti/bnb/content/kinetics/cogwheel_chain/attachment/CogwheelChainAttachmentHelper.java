package com.kipti.bnb.content.kinetics.cogwheel_chain.attachment;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.segment.CogwheelChainSegment;
import com.kipti.bnb.content.kinetics.cogwheel_chain.shape.ChainCoordinateSpace;
import com.kipti.bnb.content.kinetics.cogwheel_chain.shape.ChainDriveShapeHelper;
import com.kipti.bnb.content.kinetics.cogwheel_chain.world.CogwheelChainWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class CogwheelChainAttachmentHelper {

    @Nullable
    public static CogwheelChainAttachment findNearestAttachment(final Level level, final Vec3 worldPos) {
        return findNearestAttachment(level, worldPos, Double.MAX_VALUE);
    }

    @Nullable
    public static CogwheelChainAttachment findNearestAttachment(final Level level,
                                                                final Vec3 worldPos,
                                                                final double maxDistanceSq) {
        final CogwheelChainWorld chainWorld = CogwheelChainWorld.get(level);

        double bestDistSq = maxDistanceSq;
        BlockPos bestController = null;
        float bestChainDist = 0;

        for (final Map.Entry<BlockPos, CogwheelChain> entry : chainWorld.entries()) {
            final BlockPos controllerPos = entry.getKey();
            final CogwheelChain chain = entry.getValue();
            final List<CogwheelChainSegment> segments = chain.getSegments();
            if (segments.isEmpty()) continue;

            final ChainCoordinateSpace coordinateSpace = ChainCoordinateSpace.forLogical(level, controllerPos);
            final Vec3 localWorldPos = coordinateSpace.toLocal(worldPos);

            for (final CogwheelChainSegment segment : segments) {
                final float chainDist = projectOntoSegment(
                        localWorldPos,
                        segment.fromPosition(),
                        segment.toPosition(),
                        segment
                );

                final Vec3 closest = interpolateSegment(
                        segment.fromPosition(),
                        segment.toPosition(),
                        segment,
                        chainDist
                );
                final double distSq = worldPos.distanceToSqr(coordinateSpace.toWorld(closest));
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

    private static float projectOntoSegment(final Vec3 point,
                                             final Vec3 from,
                                             final Vec3 to,
                                             final CogwheelChainSegment segment) {
        final Vec3 ab = to.subtract(from);
        final Vec3 ap = point.subtract(from);
        final double lenSq = ab.lengthSqr();
        final double t = lenSq > 0 ? Mth.clamp(ap.dot(ab) / lenSq, 0, 1) : 0;
        return segment.startDist() + (float) (t * segment.length());
    }

    private static Vec3 interpolateSegment(final Vec3 from,
                                           final Vec3 to,
                                           final CogwheelChainSegment segment,
                                           final float chainDist) {
        final float t = segment.length() > 0
                ? Mth.clamp((chainDist - segment.startDist()) / segment.length(), 0, 1)
                : 0;
        return from.lerp(to, t);
    }
}
