package com.kipti.bnb.content.kinetics.cogwheel_chain.placement;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainPathfinder;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelNode;

import javax.annotation.Nullable;

public class ChainPlacementPathDisplayHelper {

    /**
     * For the chain in progress it checks out all the nodes and figures out if there's forced sides.
     * Each element is -1, 1, or 0 (free)
     */
    public static int[] getPathDisplaySides(final PlacingCogwheelChain chain) {
        final int[] sides = new int[chain.getNodes().size()];
        for (int i = 0; i < chain.getNodes().size(); i++) {
            final PlacingCogwheelNode previousNode = i > 0 ? chain.getNodes().get(i - 1) : null;
            final PlacingCogwheelNode currentNode = chain.getNodes().get(i);
            final PlacingCogwheelNode nextNode = i < chain.getNodes().size() - 1 ? chain.getNodes().get(i + 1) : null;

            int displayedSide = 0;
            for (int side = -1; side <= 1; side += 2) {
                if (doesNodePermitSide(previousNode, currentNode, nextNode, side)) {
                    displayedSide += side;
                }
            }
            sides[i] = displayedSide;
        }
        return sides;
    }

    public static boolean doesNodePermitSide(final @Nullable PlacingCogwheelNode previousNode,
                                             final PlacingCogwheelNode currentNode,
                                             final @Nullable PlacingCogwheelNode nextNode,
                                             final int side) {
        if (previousNode != null) {
            if (!CogwheelChainPathfinder.isValidPathStepInto(previousNode, currentNode, side)) return false;
        }
        if (nextNode != null) {
            return CogwheelChainPathfinder.isValidPathStepOutOf(currentNode, nextNode, side);
        }
        return true;
    }
}
