package com.kipti.bnb.content.kinetics.cogwheel_chain.graph;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class CogwheelChainTangents {

    public static Vec3 getTangentOnCog(final PlacingCogwheelNode previousNode, final int previousSide, final PlacingCogwheelNode currentNode, final int currentSide) {
        final double previousRadius = previousNode.isLarge() ? 1.0f : 0.5f;
        final double currentRadius = currentNode.isLarge() ? 1.0f : 0.5f;

        final Vec3 incoming = currentNode.center().subtract(previousNode.center());

        if (previousNode.rotationAxis() != currentNode.rotationAxis()) {
            final Vec3 projectedIncoming = incoming
                    .subtract(getDirectionOfAxis(currentNode).scale(incoming.dot(getDirectionOfAxis(currentNode))))
                    .subtract(getDirectionOfAxis(previousNode).scale(incoming.dot(getDirectionOfAxis(previousNode))))
                    .normalize();
            if (projectedIncoming.lengthSqr() < 1e-7) {
                return null;
            }
            final int incomingSign = projectedIncoming.cross(getDirectionOfAxis(previousNode)).dot(getDirectionOfAxis(currentNode)) > 0 ? 1 : -1;
            if (incomingSign != previousSide || incomingSign != currentSide) {
                return null;
            }

            return getDirectionOfAxis(previousNode).scale(previousNode.pos().subtract(currentNode.pos()).get(previousNode.rotationAxis()));
        }

        if (previousSide == currentSide) {
            return incoming.normalize().cross(getDirectionOfAxis(currentNode)).scale(-currentRadius * currentSide);
        }

        final double factor = previousRadius / (previousRadius + currentRadius);

        final Vec3 tangentOrigin = incoming.scale(factor);
        final double distance = tangentOrigin.length();

        final double sineRatio = currentRadius / distance;

        final double cosRatio = Math.sqrt(1 - sineRatio * sineRatio);

        //Now to find the tangents positon
        final double perpendicularHeight = cosRatio * currentRadius;

        final double lengthAlongIncoming = sineRatio * currentRadius;

        return incoming.normalize().cross(getDirectionOfAxis(currentNode)).scale(-perpendicularHeight * currentSide)
                .add(incoming.normalize().scale(-lengthAlongIncoming));
    }

    private static @NotNull Vec3 getDirectionOfAxis(final PlacingCogwheelNode currentNode) {
        return Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(currentNode.rotationAxis(), Direction.AxisDirection.POSITIVE).getNormal());
    }

}

