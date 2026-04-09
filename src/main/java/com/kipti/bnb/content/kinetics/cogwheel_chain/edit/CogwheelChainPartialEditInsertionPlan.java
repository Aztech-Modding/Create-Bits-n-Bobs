package com.kipti.bnb.content.kinetics.cogwheel_chain.edit;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelNode;

/**
 * Authoritative partial-edit insertion plan for a selected chain segment.
 */
public record CogwheelChainPartialEditInsertionPlan(
        PlacingCogwheelNode startNode,
        PlacingCogwheelNode endNode,
        PlacingCogwheelNode proposedNode,
        int insertionIndex,
        PlacingCogwheelChain rebuiltChain,
        int oldCost,
        int newCost
) {

    public int costDelta() {
        return this.newCost - this.oldCost;
    }

    public int addedCost() {
        return Math.max(this.costDelta(), 0);
    }

    public int refundedCost() {
        return Math.max(-this.costDelta(), 0);
    }
}
