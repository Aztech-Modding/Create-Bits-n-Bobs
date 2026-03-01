package com.kipti.bnb.content.kinetics.cogwheel_chain.graph;

import net.createmod.catnip.data.Pair;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CogwheelChainGeometryBuilder {

    public static List<RenderedChainPathNode> buildFullChainFromPathNodes(final List<PathedCogwheelNode> pathNodes) {
        final List<RenderedChainPathNode> resultNodes = new ArrayList<>();
        final List<Pair<Vec3, Vec3>> offsetsAtNodes = new ArrayList<>();
        final int n = pathNodes.size();
        for (int i = 0; i < n; i++) {
            final PathedCogwheelNode previousNode = pathNodes.get((n + i - 1) % n);
            final PathedCogwheelNode currentNode = pathNodes.get(i);
            final PathedCogwheelNode nextNode = pathNodes.get((i + 1) % n);

            final Pair<Vec3, Vec3> inOutPositionsAtThisNode = calculateOffsets(
                    previousNode,
                    currentNode,
                    nextNode
            );
            offsetsAtNodes.add(inOutPositionsAtThisNode);
        }

        for (int i = 0; i < n; i++) {
            final PathedCogwheelNode previousNode = pathNodes.get((n + i - 1) % n);
            final PathedCogwheelNode currentNode = pathNodes.get(i);
            final PathedCogwheelNode nextNode = pathNodes.get((i + 1) % n);

            final Pair<Vec3, Vec3> previousOffsets = offsetsAtNodes.get((i - 1 + n) % n);
            final Pair<Vec3, Vec3> currentOffsets = offsetsAtNodes.get(i);
            final Pair<Vec3, Vec3> nextOffsets = offsetsAtNodes.get((i + 1) % n);

            resultNodes.add(new RenderedChainPathNode(currentNode.localPos(), currentOffsets.getFirst(), currentNode.rotationAxisVec()));
            resultNodes.addAll(
                    wrappedArcBetweenPoints(
                            currentNode,
                            previousOffsets.getSecond().add(previousNode.center()),
                            currentOffsets.getFirst().add(currentNode.center()),
                            currentOffsets.getSecond().add(currentNode.center()),
                            nextOffsets.getFirst().add(nextNode.center())
                    )
            );
            resultNodes.add(new RenderedChainPathNode(currentNode.localPos(), currentOffsets.getSecond(), currentNode.rotationAxisVec()));
        }

        return resultNodes;
    }

    private static List<RenderedChainPathNode> wrappedArcBetweenPoints(
            final PathedCogwheelNode currentNode,
            final Vec3 outPreviousPositionWorld, final Vec3 inCurrentOffsetWorld,
            final Vec3 outCurrentOffsetWorld, final Vec3 inNextPositionWorld) {
        // Move to chainNode-local frame (center = origin) for rotation math
        final Vec3 center = currentNode.localPos().getCenter();

        final Vec3 prevLocal = outPreviousPositionWorld.subtract(center);
        final Vec3 startLocal = inCurrentOffsetWorld.subtract(center);
        final Vec3 endLocal = outCurrentOffsetWorld.subtract(center);
        final Vec3 nextLocal = inNextPositionWorld.subtract(center);

        // Basic checks
        final double r = startLocal.length();
        if (r <= 1e-9) {
            // degenerate radius zero — nothing to do
            return List.of();
        }

        Vec3 inDirection = startLocal.subtract(prevLocal);
        if (inDirection.length() < 1e-9) inDirection = nextLocal.subtract(startLocal); // fallback
        inDirection = inDirection.normalize();

        Vec3 outDirection = endLocal.subtract(nextLocal);
        if (outDirection.length() < 1e-9) outDirection = prevLocal.subtract(endLocal);
        outDirection = outDirection.normalize();

        final Vec3 axis = getDirectionOfAxis(currentNode).normalize();
        if (axis == null) throw new IllegalStateException("axis null for chainNode " + currentNode);

        final Vec3 u = startLocal.normalize();
        final Vec3 w = endLocal.normalize();

        // signed minimal angle from u -> w around axis in range (-PI, PI]
        final double crossDot = axis.dot(u.cross(w));
        final double dot = Math.max(-1.0, Math.min(1.0, u.dot(w)));
        double signedAngle = Math.atan2(crossDot, dot); // minimal signed

        // tangent for positive increase at start: dP/dθ = axis x start
        Vec3 tangentAtStart = axis.cross(startLocal);
        final double tangentLen = tangentAtStart.length();
        double tangentDot = 0.0;
        if (tangentLen > 1e-9) {
            tangentAtStart = tangentAtStart.scale(1.0 / tangentLen);
            tangentDot = inDirection.dot(tangentAtStart);
        } else {
            // degenerate tangent (start radial zero?) - fallback
            tangentDot = inDirection.dot(u) > 0 ? 1.0 : -1.0;
        }

        // If the incoming direction indicates the chain moves opposite to the signedAngle,
        // prefer the other arc (i.e., add/subtract 2π) so motion along the arc aligns with inDirection.
        final double EPS = 1e-9;
        if (Math.abs(tangentDot) > EPS) {
            final double angleSign = Math.signum(signedAngle);
            final double desiredSign = (tangentDot > 0) ? 1.0 : -1.0;

            if (angleSign == 0.0) {
                // start and end nearly colinear: either same point or opposite
                if (dot > 0.999999) {
                    signedAngle = 0.0; // identical: no arc
                } else {
                    // opposite: choose pi or -pi based on desired tangent
                    signedAngle = desiredSign * Math.PI;
                }
            } else if (angleSign != desiredSign) {
                // choose the long way so the tangent direction matches
                signedAngle = signedAngle - Math.signum(signedAngle) * 2.0 * Math.PI;
            }
        }

        // Choose segments based on arc length for consistent spacing
        final double absAngle = Math.abs(signedAngle);
        final double approxArcLength = r * absAngle;
        final int segments = Math.max(1, (int) (Math.ceil(approxArcLength) * 3f));
        final List<RenderedChainPathNode> result = new ArrayList<>();

        // generate interior points (exclude endpoints)
        for (int i = 1; i < segments; i++) {
            final double t = (double) i / (double) segments;
            final double theta = signedAngle * t;
            final Vec3 rotatedLocal = rotateAroundAxis(startLocal, axis, theta);
            result.add(new RenderedChainPathNode(currentNode.localPos(), rotatedLocal, currentNode.rotationAxisVec()));
        }

        return result;
    }

    private static Vec3 rotateAroundAxis(final Vec3 v, final Vec3 axis, final double angle) {
        final double cos = Math.cos(angle);
        final double sin = Math.sin(angle);
        return v.scale(cos)
                .add(axis.cross(v).scale(sin))
                .add(axis.scale(axis.dot(v) * (1 - cos)));
    }

    private static int getLooped(final int[] concavities, final int i) {
        return concavities[(i + concavities.length) % concavities.length];
    }

    public static Pair<Vec3, Vec3> calculateOffsets(final PathedCogwheelNode previousNode,
                                                    final PathedCogwheelNode currentNode,
                                                    final PathedCogwheelNode nextNode) {

//        Vec3 incomingDirection = getConnectionDirection(previousNode, currentNode);
//        Vec3 outgoingDirection = getConnectionDirection(currentNode, nextNode);

        final Vec3 incomingPointOnCircle = getTangentPointOnCircle(previousNode, currentNode, true);
        final Vec3 outgoingPointOnCircle = getTangentPointOnCircle(nextNode, currentNode, false);

        return Pair.of(incomingPointOnCircle, outgoingPointOnCircle);
    }

    private static @NotNull Vec3 getDirectionOfAxis(final PathedCogwheelNode currentNode) {
        return Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(currentNode.rotationAxis(), Direction.AxisDirection.POSITIVE).getNormal());
    }

    private static @NotNull Vec3 getConnectionDirection(final PathedCogwheelNode previousNode, final PathedCogwheelNode currentNode) {
        Vec3 incomingADiff = currentNode.center().subtract(previousNode.center());

        if (previousNode.rotationAxis() != currentNode.rotationAxis()) {
            final Vec3 previousAxis = getDirectionOfAxis(previousNode);
            incomingADiff = incomingADiff.subtract(previousAxis.scale(incomingADiff.dot(previousAxis)));
        }

        final Vec3 axis = getDirectionOfAxis(currentNode);
        return incomingADiff.subtract(axis.scale(axis.dot(incomingADiff)));
    }

    public static Vec3 getTangentPointOnCircle(final PathedCogwheelNode previousNode,
                                               final PathedCogwheelNode currentNode,
                                               final boolean isIncoming) {
        final Vec3 axis = getDirectionOfAxis(currentNode);
        final Vec3 incoming = isIncoming ? getConnectionDirection(previousNode, currentNode) : getConnectionDirection(currentNode, previousNode);

        final double previousRadius = previousNode.isLarge() ? 1.0f : 0.5f + (previousNode.hasSmallCogwheelOffset() ? 1 / 8f : 0.0f);
        final double currentRadius = currentNode.isLarge() ? 1.0f : 0.5f + (currentNode.hasSmallCogwheelOffset() ? 1 / 8f : 0.0f);

        if (previousNode.rotationAxis() != currentNode.rotationAxis()) {
            //Find the common line
//            Direction.Axis other = Direction.Axis.values()[Integer.numberOfTrailingZeros(0b111 ^ (1 << currentNode.rotationAxis().ordinal()) ^ (1 << previousNode.rotationAxis().ordinal()))];
//            Vec3 otherAxis = Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(other, Direction.AxisDirection.POSITIVE).getNormal());
//            return otherAxis.cross(axis).normalize().scale(
//                currentNode.pos().subtract(previousNode.pos()).get(previousNode.rotationAxis()) * (isIncoming ? 1 : -1)
//            );
            return getDirectionOfAxis(previousNode).scale(previousNode.localPos().subtract(currentNode.localPos()).get(previousNode.rotationAxis()));
        }

        if (previousNode.side() == currentNode.side()) {
            return incoming.normalize().cross(axis).scale(-currentRadius * currentNode.side());
        }

        final double factor = previousRadius / (previousRadius + currentRadius);

        final Vec3 tangentOrigin = incoming.scale(factor);
        final double distance = (isIncoming ? 1 : -1) * tangentOrigin.length();

        final double sineRatio = previousRadius / distance;

        final double cosRatio = Math.sqrt(1 - sineRatio * sineRatio);

        //Now to find the tangents positon
        final double perpendicularHeight = cosRatio * currentRadius;

        final double lengthAlongIncoming = sineRatio * currentRadius;

        return incoming.normalize().cross(axis).scale(-perpendicularHeight * currentNode.side())
                .add(incoming.normalize().scale(-lengthAlongIncoming));
    }

}
