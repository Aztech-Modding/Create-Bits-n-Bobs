package com.kipti.bnb.content.kinetics.cogwheel_chain.shape;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.world.CogwheelChainWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ChainDriveShapeHelper {

    public static @Nullable ChainShapeHit findClosestRayHit(final Level level,
                                                            final Vec3 origin,
                                                            final Vec3 target,
                                                            final double maxDistanceSq) {
        return findClosestRayHit(level, origin, target, maxDistanceSq, false);
    }

    public static @Nullable ChainShapeHit findClosestRenderedRayHit(final Level level,
                                                                    final Vec3 origin,
                                                                    final Vec3 target,
                                                                    final double maxDistanceSq) {
        return findClosestRayHit(level, origin, target, maxDistanceSq, true);
    }

    private static @Nullable ChainShapeHit findClosestRayHit(final Level level,
                                                            final Vec3 origin,
                                                            final Vec3 target,
                                                            final double maxDistanceSq,
                                                            final boolean renderPose) {
        final CogwheelChainWorld chainWorld = CogwheelChainWorld.get(level);
        ChainShapeHit bestHit = null;
        double bestDistanceSq = maxDistanceSq;

        for (final Map.Entry<BlockPos, CogwheelChain> entry : chainWorld.entries()) {
            final CogwheelChainWholeShape shape = CogwheelChainWholeShape.buildShape(entry.getValue());
            if (shape == null) {
                continue;
            }

            final ChainCoordinateSpace coordinateSpace = renderPose
                    ? ChainCoordinateSpace.forRender(level, entry.getKey())
                    : ChainCoordinateSpace.forLogical(level, entry.getKey());
            final ChainShapeHit hit = testRayHit(coordinateSpace, shape, origin, target);
            if (hit == null) {
                continue;
            }

            if (hit.distanceSq() < bestDistanceSq) {
                bestDistanceSq = hit.distanceSq();
                bestHit = hit;
            }
        }

        return bestHit;
    }

    public static @Nullable ChainShapeHit findClosestRayHit(final Level level,
                                                            final Vec3 origin,
                                                            final Vec3 target) {
        return findClosestRayHit(level, origin, target, Double.MAX_VALUE);
    }

    private static @Nullable ChainShapeHit testRayHit(final ChainCoordinateSpace coordinateSpace,
                                                       final CogwheelChainWholeShape shape,
                                                       final Vec3 origin,
                                                       final Vec3 target) {
        final Vec3 localFrom = coordinateSpace.toLocal(origin);
        final Vec3 localTo = coordinateSpace.toLocal(target);
        final Vec3 intersectionLocal = shape.intersect(localFrom, localTo);
        if (intersectionLocal == null) {
            return null;
        }

        final Vec3 worldHit = coordinateSpace.toWorld(intersectionLocal);
        final double distanceSq = worldHit.distanceToSqr(origin);
        final float chainPosition = shape.getChainPosition(intersectionLocal);
        final Vec3 bakedPosition = coordinateSpace.toWorld(shape.getLocalVec(chainPosition));
        return new ChainShapeHit(coordinateSpace.getControllerPos(), shape, bakedPosition, chainPosition, distanceSq);
    }

    public record ChainShapeHit(BlockPos controllerPos,
                                CogwheelChainWholeShape shape,
                                Vec3 bakedPosition,
                                float chainPosition,
                                double distanceSq) {
    }
}
