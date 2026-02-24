package com.kipti.bnb.content.kinetics.cogwheel_chain.render;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.RenderedChainPathNode;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CogwheelChainRenderGeometry {

    public static List<ChainSegment> buildSegments(final CogwheelChain chain, final Vec3 origin) {
        if (chain == null || chain.getChainPathNodes().isEmpty()) {
            return List.of();
        }

        final List<RenderedChainPathNode> nodes = chain.getChainPathNodes();
        final int size = nodes.size();
        final ArrayList<ChainSegment> out = new ArrayList<>(size);
        double accumulatedUV = 0;

        for (int i = 0; i < size; i++) {
            final RenderedChainPathNode node0 = nodes.get((size + i - 1) % size);
            final RenderedChainPathNode node1 = nodes.get(i);
            final RenderedChainPathNode node2 = nodes.get((i + 1) % size);
            final RenderedChainPathNode node3 = nodes.get((i + 2) % size);

            final Vec3 preFrom = node3.getPosition().add(origin);
            final Vec3 from = node2.getPosition().add(origin);
            final Vec3 to = node1.getPosition().add(origin);
            final Vec3 postTo = node0.getPosition().add(origin);

            final double distance = from.distanceTo(to);
            out.add(new ChainSegment(
                    preFrom,
                    from,
                    to,
                    postTo,
                    node2.sourceCogwheelAxis(),
                    node1.sourceCogwheelAxis(),
                    accumulatedUV,
                    distance
            ));

            accumulatedUV += distance;
        }

        return out;
    }

    public static double calculateTotalChainDistance(final CogwheelChain chain, final Vec3 origin) {
        double totalDistance = 0;
        for (final ChainSegment segment : buildSegments(chain, origin)) {
            totalDistance += segment.distance();
        }
        return totalDistance;
    }

    public static List<Vec3> getPointsInClosestOrder(final List<Vec3> destinationPoints, final List<Vec3> sourcePoints) {
        if (destinationPoints.size() != 4 || sourcePoints.size() != 4) {
            return new ArrayList<>(destinationPoints);
        }

        double bestScore = Double.POSITIVE_INFINITY;
        List<Vec3> best = new ArrayList<>(destinationPoints);

        // Only allow cyclic orderings: 4 rotations * (normal/reversed winding) = 8 candidates
        for (int reversed = 0; reversed <= 1; reversed++) {
            for (int shift = 0; shift < 4; shift++) {
                final ArrayList<Vec3> candidate = new ArrayList<>(4);
                double pointScore = 0.0;

                for (int i = 0; i < 4; i++) {
                    final int j = (reversed == 0)
                            ? ((i + shift) & 3)
                            : ((shift - i + 4) & 3);

                    final Vec3 d = destinationPoints.get(j);
                    candidate.add(d);
                    pointScore += sourcePoints.get(i).distanceToSqr(d);
                }

                // Edge-direction term to discourage "crossed" face mapping
                double edgeScore = 0.0;
                for (int i = 0; i < 4; i++) {
                    final Vec3 sEdge = sourcePoints.get((i + 1) & 3).subtract(sourcePoints.get(i)).normalize();
                    final Vec3 dEdge = candidate.get((i + 1) & 3).subtract(candidate.get(i)).normalize();
                    edgeScore += 1.0 - sEdge.dot(dEdge); // 0 is best
                }

                // Small bias against reversing winding unless clearly better
                final double windingPenalty = reversed == 1 ? 1e-4 : 0.0;
                final double score = pointScore + edgeScore * 0.25 + windingPenalty;

                if (score < bestScore) {
                    bestScore = score;
                    best = candidate;
                }
            }
        }

        return best;
    }

    public static List<Vec3> getEndPointsForChainJoint(final Vec3 before, final Vec3 point, final Vec3 after, final CogwheelChainType.ChainRenderInfo chainRenderInfo, final Vec3 cogwheelAxis) {
        final float radius = (float) ((chainRenderInfo.getVertexShape() == CogwheelChainType.VertexShape.CROSS ? Math.sqrt(2f) / 2f : 1f) * 1f / 16f);
        final Vec3 dirToBefore = point.subtract(before).normalize();
        final Vec3 dirToAfter = after.subtract(point).normalize();

        Vec3 averagedDir = dirToBefore.add(dirToAfter).normalize();

        final Matrix3f transform;

        if (chainRenderInfo.getVertexShape() == CogwheelChainType.VertexShape.CROSS) {
            transform = new Quaternionf()
                    .rotationTo(0, 1, 0, (float) averagedDir.x, (float) averagedDir.y, (float) averagedDir.z)
                    .get(new Matrix3f());
        } else {
            // Project averagedDir to axis if needed
            if (averagedDir.dot(cogwheelAxis) > 1e-4) {
                averagedDir = averagedDir.subtract(cogwheelAxis.multiply(averagedDir)).normalize();
            }

            // Fallback if projection degenerates
            if (averagedDir.lengthSqr() < 1e-4) {
                averagedDir = cogwheelAxis.cross(new Vec3(1, 0, 0));
            }
            if (averagedDir.lengthSqr() < 1e-4) {
                averagedDir = cogwheelAxis.cross(new Vec3(0, 1, 0));
            }

            final Vec3 perpendicular = cogwheelAxis.cross(averagedDir);
            transform = new Matrix3f(
                    (float) perpendicular.x, (float) perpendicular.y, (float) perpendicular.z,
                    (float) averagedDir.x, (float) averagedDir.y, (float) averagedDir.z,
                    (float) cogwheelAxis.x, (float) cogwheelAxis.y, (float) cogwheelAxis.z
            );
        }

        final Vector3f localAxis1Joml = transform.transform(1f, 0f, 0f, new Vector3f());
        final Vec3 localAxis1Direction = new Vec3(localAxis1Joml.x, localAxis1Joml.y, localAxis1Joml.z).normalize();
        final Vec3 localAxis1 = localAxis1Direction.scale(chainRenderInfo.getHeight() / 2f);
        final Vector3f localAxis2Joml = transform.transform(0f, 0f, 1f, new Vector3f());
        final Vec3 localAxis2 = new Vec3(localAxis2Joml.x, localAxis2Joml.y, localAxis2Joml.z).normalize().scale(chainRenderInfo.getWidth() / 2f);

        return Stream.of(
                        point.add(localAxis1.add(localAxis2).scale(radius)),
                        point.add(localAxis1.subtract(localAxis2).scale(radius)),
                        point.add(localAxis2.scale(-1).subtract(localAxis1).scale(radius)),
                        point.add(localAxis2.subtract(localAxis1).scale(radius))
                )
                .map(e -> chainRenderInfo.getHeight() < 3 ? e.add(localAxis1Direction.scale((3f - chainRenderInfo.getHeight()) / (2 * 3 * 16))) : e)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public record ChainSegment(
            Vec3 preFrom,
            Vec3 from,
            Vec3 to,
            Vec3 postTo,
            Vec3 fromCogwheelAxis,
            Vec3 toCogwheelAxis,
            double uvStart,
            double distance
    ) {
    }
}
