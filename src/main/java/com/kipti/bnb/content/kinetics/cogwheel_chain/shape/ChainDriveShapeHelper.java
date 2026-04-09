package com.kipti.bnb.content.kinetics.cogwheel_chain.shape;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.world.CogwheelChainWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Provides read-only accessors for building temporary shapes of every loaded chain drive.
 * Shapes are derived directly from {@link CogwheelChainWorld} without storing additional state.
 */
public class ChainDriveShapeHelper {

    /**
     * Performs a ray cast against every loaded chain shape and returns the closest hit within the
     * provided squared reach.
     *
     * @param maxDistanceSq squared distance cap, or {@code Double.MAX_VALUE} for unlimited reach
     */
    public static @Nullable ChainShapeHit findClosestRayHit(final Level level,
                                                            final Vec3 origin,
                                                            final Vec3 target,
                                                            final double maxDistanceSq) {
        final CogwheelChainWorld chainWorld = CogwheelChainWorld.get(level);
        ChainShapeHit bestHit = null;
        double bestDistanceSq = maxDistanceSq;

        for (final Map.Entry<BlockPos, CogwheelChain> entry : chainWorld.entries()) {
            final CogwheelChainWholeShape shape = CogwheelChainWholeShape.buildShape(entry.getValue());
            if (shape == null) {
                continue;
            }

            final ChainShapeHit hit = testRayHit(entry.getKey(), shape, origin, target);
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

    private static @Nullable ChainShapeHit testRayHit(final BlockPos controllerPos,
                                                      final CogwheelChainWholeShape shape,
                                                      final Vec3 origin,
                                                      final Vec3 target) {
        final Vec3 controllerBase = Vec3.atLowerCornerOf(controllerPos);
        final Vec3 localFrom = origin.subtract(controllerBase);
        final Vec3 localTo = target.subtract(controllerBase);
        final Vec3 intersectionLocal = shape.intersect(localFrom, localTo);
        if (intersectionLocal == null) {
            return null;
        }

        final Vec3 worldHit = intersectionLocal.add(controllerBase);
        final double distanceSq = worldHit.distanceToSqr(origin);
        final float chainPosition = shape.getChainPosition(intersectionLocal);
        final Vec3 bakedPosition = shape.getVec(controllerPos, chainPosition);
        return new ChainShapeHit(controllerPos, shape, bakedPosition, chainPosition, distanceSq);
    }

    public record ChainShapeHit(BlockPos controllerPos,
                                CogwheelChainWholeShape shape,
                                Vec3 bakedPosition,
                                float chainPosition,
                                double distanceSq) {
    }
}
