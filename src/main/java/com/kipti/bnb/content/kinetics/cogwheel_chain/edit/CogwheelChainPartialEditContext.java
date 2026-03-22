package com.kipti.bnb.content.kinetics.cogwheel_chain.edit;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelNode;
import com.kipti.bnb.content.kinetics.cogwheel_chain.segment.CogwheelChainSegment;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;

/**
 * Captures the exact chain segment a partial edit session is anchored to.
 */
public record CogwheelChainPartialEditContext(
        BlockPos controllerPos,
        float chainPosition,
        CogwheelChainSegment segment,
        int startNodeIndex,
        int endNodeIndex,
        PlacingCogwheelNode startNode,
        PlacingCogwheelNode endNode,
        CogwheelChainType chainType,
        Item chainItemType
) {

    public CogwheelChainPartialEditContext {
        controllerPos = controllerPos.immutable();
    }
}
