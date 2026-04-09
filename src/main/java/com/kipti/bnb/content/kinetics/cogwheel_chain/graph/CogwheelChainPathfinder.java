package com.kipti.bnb.content.kinetics.cogwheel_chain.graph;

import com.google.common.collect.ImmutableList;
import com.kipti.bnb.content.kinetics.cogwheel_chain.placement.ChainInteractionFailedException;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Chain pathing version 3 cause this a b****
 * tldr: layer based flood fill, but we check for self intersections (in an appox way) which are an extra cost, and actually want to maximize distance (there isn't any loops so this is ok)
 * <p>
 * From tldraw (<a href="https://www.tldraw.com/f/P_q_3vRdCBanYptH-4qck?d=v-1769.-567.3320.1890._WwV4NqrB9v64-H-OpMyB">here</a>) of this:
 * <pre>
 * Steps of chain assembly:
 *
 * -> Put list of cogs together, which should have ANY valid path, this validation is good "enough"
 *
 * -> Use non tangential tangents to calculate the cw / ccw of the best path on each chainNode
 * 	-> Reject if no valid path found, but ultimatley should not be possible, maybe say
 * 		"Couldn't resolve valid chain path. Please report!"
 *
 * -> Build proper chain path, but some assumptions can be made
 * 	-> The path between axis changes will always lie on the valid one,
 * 		-> Essentially just reuse existing tangent code
 * </pre>
 */
public class CogwheelChainPathfinder {

    public record PartialPathFrontierData(
            ImmutableList<PathedCogwheelNode> traversed,
            double distance,
            int chainIntersections
    ) {
        public PartialPathFrontierData compare(final PartialPathFrontierData other) {
            if (this.chainIntersections != other.chainIntersections) {
                return this.chainIntersections < other.chainIntersections ? this : other;
            }
            return this.distance > other.distance ? this : other;
        }

        public PartialPathFrontierData extend(final PathedCogwheelNode nextNode, final double additionalDistance, final int additionalSelfIntersections) {
            final ArrayList<PathedCogwheelNode> newTraversed = new ArrayList<>(this.traversed);
            newTraversed.add(nextNode);
            return new PartialPathFrontierData(
                    ImmutableList.copyOf(newTraversed),
                    this.distance + additionalDistance,
                    this.chainIntersections + additionalSelfIntersections
            );
        }

    }

    public static List<PathedCogwheelNode> buildChainPath(final PlacingCogwheelChain worldSpaceChain) throws ChainInteractionFailedException {
        //Reconstruct chain to local space
        final PlacingCogwheelChain chain = worldSpaceChain.toLocalSpaceChain();

        PartialPathFrontierData leftPath = new PartialPathFrontierData(
                ImmutableList.of(new PathedCogwheelNode(chain.getNodes().getFirst(), 1)),
                0,
                0
        );
        PartialPathFrontierData rightPath = new PartialPathFrontierData(
                ImmutableList.of(new PathedCogwheelNode(chain.getNodes().getFirst(), -1)),
                0,
                0
        );

        PlacingCogwheelNode prevNode = chain.getNodes().get(0);
        for (int i = 1; i < chain.getNodes().size() * 2; i++) {
            final PlacingCogwheelNode nextNode = chain.getNodes().get(i % chain.getNodes().size());
            final PlacingCogwheelNode nextNextNode = chain.getNodes().get((i + 1) % chain.getNodes().size());

            PartialPathFrontierData nextLeftPath = null;
            PartialPathFrontierData nextRightPath = null;

            for (int fromSide = 1; fromSide >= -1; fromSide -= 2) {
                final PartialPathFrontierData fromPath = (fromSide == 1) ? leftPath : rightPath;
                if (fromPath == null) {
                    continue;
                }

                for (int toSide = 1; toSide >= -1; toSide -= 2) {
                    if (isValidPathStep(prevNode, fromSide, nextNode, toSide)) {
                        final PartialPathFrontierData extended = computeExtendedPath(prevNode, nextNode, fromSide, toSide, fromPath, nextNextNode, chain.getSize());
                        if (toSide == 1) {
                            nextLeftPath = merge(nextLeftPath, extended);
                        } else {
                            nextRightPath = merge(nextRightPath, extended);
                        }
                    }
                }
            }
            leftPath = nextLeftPath;
            rightPath = nextRightPath;

            if (leftPath == null && rightPath == null) {
                throw new ChainInteractionFailedException("pathfinding_failed_at_node");
            }
            prevNode = nextNode;
        }
        final PartialPathFrontierData finalPath = (leftPath != null && rightPath != null)
                ? leftPath.compare(rightPath)
                : (leftPath != null ? leftPath : rightPath);
        if (finalPath == null) return null;

        final ArrayList<PathedCogwheelNode> finalTraversed = new ArrayList<>(finalPath.traversed);

        finalTraversed.removeLast();
        for (int i = 0; i < chain.getNodes().size() - 1; i++) {//TODO reduce the amount its "overpathing" to just the 2 extra nodes on each sideFactor
            finalTraversed.removeFirst();
        }

        return finalTraversed;
    }

    private static PartialPathFrontierData merge(final PartialPathFrontierData existing, final PartialPathFrontierData candidate) {
        return existing == null ? candidate : existing.compare(candidate);
    }

    private static PartialPathFrontierData computeExtendedPath(final PlacingCogwheelNode prevNode, final PlacingCogwheelNode nextNode, final int fromSide, final int toSide, final PartialPathFrontierData fromPath, final PlacingCogwheelNode nextNextNode, final int size) {
        final Vec3 fromPos = prevNode.center().add(
                getPathingTangentOnCog(nextNode, prevNode, -fromSide)
        );
        final Vec3 toPos = nextNode.center().add(
                getPathingTangentOnCog(prevNode, nextNode, toSide)
        );
        final ImmutableList<PathedCogwheelNode> traversed = fromPath.traversed;
        final int traversedSize = traversed.size();

        final double distance = fromPos.distanceTo(toPos) + (getArcDistanceOnCog(
                new PathedCogwheelNode(prevNode, fromSide),
                new PathedCogwheelNode(nextNode, toSide),
                new PathedCogwheelNode(nextNextNode, isValidPathStep(nextNode, toSide, nextNextNode, toSide) ? toSide : -toSide)
        )) * 10f;

        final int selfIntersections = nextNextNode == prevNode ? (toSide != fromSide ? 1 : 0) : (traversedSize < 2 ? 0 :
                getSelfIntersection(
                        traversed.get(traversedSize - 2),
                        traversed.get(traversedSize - 1),
                        nextNode,
                        toSide
                ));

        return fromPath.extend(
                new PathedCogwheelNode(nextNode, toSide),
                distance,
                selfIntersections
        );
    }

    private static double getArcDistanceOnCog(final PathedCogwheelNode prevNode, final PathedCogwheelNode currentNode, final PathedCogwheelNode nextNode) {
        final Vec3 fromTangent = CogwheelChainGeometryBuilder.getTangentPointOnCircle(prevNode, currentNode, true);
        final Vec3 toTangent = CogwheelChainGeometryBuilder.getTangentPointOnCircle(nextNode, currentNode, false);

        final Vec3 incomingDiff = currentNode.center().subtract(prevNode.center());
        if (toTangent.distanceToSqr(fromTangent) < 1e-4) {
            return 0;
        }

        if (incomingDiff.normalize().dot(toTangent.subtract(fromTangent)) < 0) {
            return 0;
        }

        final double angle = Math.acos(
                Math.max(-1.0, Math.min(1.0,
                        fromTangent.normalize().dot(toTangent.normalize())
                ))
        );

        final double radius = currentNode.isLarge() ? 1.0f : 0.5f;

        return angle * radius;
    }

    public static int getSelfIntersection(final PathedCogwheelNode from,
                                          final PathedCogwheelNode middle,
                                          final PlacingCogwheelNode to,
                                          final int side) {
        if (from.rotationAxis() != to.rotationAxis() || middle.rotationAxis() != to.rotationAxis()) {
            return 0;
        }

        final Vec3 prevFromPathPos = getPathingTangentOnCog(middle, from, -from.side()).add(from.center());
        final Vec3 prevToPathPos = getPathingTangentOnCog(from, middle, middle.side()).add(middle.center());

        final Vec3 nextFromPathPos = getPathingTangentOnCog(to, middle, -middle.side()).add(middle.center());
        final Vec3 nextToPathPos = getPathingTangentOnCog(middle, to, side).add(to.center());

        return doLinesIntersectOnPlane(
                middle.rotationAxisVec(),
                prevFromPathPos.subtract(middle.center()),
                prevToPathPos.subtract(middle.center()),
                nextFromPathPos.subtract(middle.center()),
                nextToPathPos.subtract(middle.center())
        ) ? 1 : 0;
    }

    private static boolean doLinesIntersectOnPlane(final Vec3 axis, final Vec3 fromStart, final Vec3 fromEndWhichIsAroundCenter, final Vec3 toStartWhichIsAroundCenter, final Vec3 toEnd) {
        final Vec3 fromDir = fromEndWhichIsAroundCenter.subtract(fromStart);
        final Vec3 toDir = toEnd.subtract(toStartWhichIsAroundCenter);

        final Vec3 normal = axis;

        final Vec3 diff = toStartWhichIsAroundCenter.subtract(fromStart);
        final double denom = normal.dot(fromDir.cross(toDir));
        if (Math.abs(denom) < 1e-7) {
            return false; // Lines are parallel
        }

        final double t = normal.dot(diff.cross(toDir)) / denom;
        final double u = normal.dot(diff.cross(fromDir)) / denom;

        return t >= 0 && t <= 1 && u >= 0 && u <= 1;
    }

    public static boolean isValidPathStepOutOf(final PlacingCogwheelNode nodeA, final PlacingCogwheelNode nodeB, final int side) {
        return isValidPathStep(nodeA, side, nodeB, 1) || isValidPathStep(nodeA, side, nodeB, -1);
    }

    public static boolean isValidPathStepInto(final PlacingCogwheelNode nodeA, final PlacingCogwheelNode nodeB, final int side) {
        return isValidPathStep(nodeA, 1, nodeB, side) || isValidPathStep(nodeA, -1, nodeB, side);
    }

    /**
     * Check if the combination is valid, predominantly dedicated to axis changes
     */
    public static boolean isValidPathStep(final PlacingCogwheelNode from, final int fromSide, final PlacingCogwheelNode to, final int toSide) {
        if (from.rotationAxis() == to.rotationAxis()) {
            return true;
        }
        if (!from.isLarge() || !to.isLarge()) {
            return false;
        }

        final Vec3 diff = to.center().subtract(from.center());

        final Vec3 sideAxis = to.rotationAxisVec();
        final Vec3 upAxis = from.rotationAxisVec();

        final Vec3 fromTangentOffset = getPathingTangentOnCog(to, from, -fromSide);
        final Vec3 toTangentOffset = getPathingTangentOnCog(from, to, toSide);

        return sideAxis.multiply(diff).dot(fromTangentOffset) == 1 && upAxis.multiply(diff).dot(toTangentOffset) == -1;
    }

    /*
     * Get a list of sides (on the last node) which have any valid path to the new node
     * */
    public static List<Integer> getValidPathSteps(final PlacingCogwheelNode lastNode, final PlacingCogwheelNode newNode) {
        final List<Integer> validSides = new ArrayList<>();
        for (int side = 1; side >= -1; side -= 2) {
            if (isValidPathStep(lastNode, side, newNode, 1) || isValidPathStep(lastNode, side, newNode, -1)) {
                validSides.add(side);
            }
        }
        return validSides;
    }

    public static Vec3 getPathingTangentOnCog(final ICogwheelNode from, final ICogwheelNode to, final int toSide) {
        return getPathingTangentOnCog(from.center(), from.rotationAxisVec(), to.center(), to.isLarge(), to.rotationAxisVec(), toSide);
    }

    public static Vec3 getPathingTangentOnCog(final Vec3 fromCenter, final Vec3 fromRotationAxis, final Vec3 toCenter, final boolean toLarge, final Vec3 toRotationAxis, final int toSide) {
        final double toRadius = toLarge ? 1.0f : 0.5f;

        Vec3 differenceTo = toCenter.subtract(fromCenter);

        if (!fromRotationAxis.equals(toRotationAxis)) {
            differenceTo = projectDirToAxisPlane(projectDirToAxisPlane(differenceTo, toRotationAxis), fromRotationAxis);
        }

        return toRotationAxis.cross(differenceTo).normalize().scale(toSide * toRadius);
    }

    public static Vec3 projectDirToAxisPlane(final Vec3 vec, final Vec3 axisVec) {
        return vec.subtract(axisVec.multiply(vec));
    }
}

