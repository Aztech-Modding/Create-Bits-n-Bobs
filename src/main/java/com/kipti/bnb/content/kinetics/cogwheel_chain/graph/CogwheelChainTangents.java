package com.kipti.bnb.content.kinetics.cogwheel_chain.graph;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class CogwheelChainTangents {

    public static Vec3 getTangentOnCog(PlacingCogwheelNode previousNode, int previousSide, PlacingCogwheelNode currentNode, int currentSide) {
        double previousRadius = previousNode.isLarge() ? 1.0f : 0.5f;
        double currentRadius = currentNode.isLarge() ? 1.0f : 0.5f;

        Vec3 incoming = currentNode.center().subtract(previousNode.center());

        if (previousNode.rotationAxis() != currentNode.rotationAxis()) {
            Vec3 projectedIncoming = incoming
                    .subtract(getDirectionOfAxis(currentNode).scale(incoming.dot(getDirectionOfAxis(currentNode))))
                    .subtract(getDirectionOfAxis(previousNode).scale(incoming.dot(getDirectionOfAxis(previousNode))))
                    .normalize();
            if (projectedIncoming.lengthSqr() < 1e-7) {
                return null;
            }
            int incomingSign = projectedIncoming.cross(getDirectionOfAxis(previousNode)).dot(getDirectionOfAxis(currentNode)) > 0 ? 1 : -1;
            if (incomingSign != previousSide || incomingSign != currentSide) {
                return null;
            }

            return getDirectionOfAxis(previousNode).scale(previousNode.pos().subtract(currentNode.pos()).get(previousNode.rotationAxis()));
        }

        if (previousSide == currentSide) {
            return incoming.normalize().cross(getDirectionOfAxis(currentNode)).scale(-currentRadius * currentSide);
        }

        double factor = previousRadius / (previousRadius + currentRadius);

        Vec3 tangentOrigin = incoming.scale(factor);
        double distance = tangentOrigin.length();

        double sineRatio = currentRadius / distance;

        double cosRatio = Math.sqrt(1 - sineRatio * sineRatio);

        //Now to find the tangents positon
        double perpendicularHeight = cosRatio * currentRadius;

        double lengthAlongIncoming = sineRatio * currentRadius;

        return incoming.normalize().cross(getDirectionOfAxis(currentNode)).scale(-perpendicularHeight * currentSide)
                .add(incoming.normalize().scale(-lengthAlongIncoming));
    }

    private static @NotNull Vec3 getDirectionOfAxis(PlacingCogwheelNode currentNode) {
        return Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(currentNode.rotationAxis(), Direction.AxisDirection.POSITIVE).getNormal());
    }

}
