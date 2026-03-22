package com.kipti.bnb.content.kinetics.cogwheel_chain.edit;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainCandidate;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainPathfinder;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PathedCogwheelNode;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelNode;
import com.kipti.bnb.content.kinetics.cogwheel_chain.segment.CogwheelChainSegment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the authoritative insertion plan for a partial cogwheel-chain edit.
 */
public class CogwheelChainPartialEditInsertionPlanner {

    /**
     * Plans an insertion using the in-world block state at the proposed position.
     * Used server-side after the cogwheel block has been placed.
     */
    public static @Nullable CogwheelChainPartialEditInsertionPlan plan(final CogwheelChain existingChain,
                                                                       final CogwheelChainPartialEditContext editContext,
                                                                       final BlockPos proposedPos,
                                                                       final BlockState proposedState) {
        final CogwheelChainCandidate candidate = CogwheelChainCandidate.getForBlock(proposedState);
        if (candidate == null)
            return null;
        if (!editContext.chainType().getCogwheelPredicate().test(proposedState.getBlock()))
            return null;
        return planWithCandidate(existingChain, editContext, proposedPos, candidate);
    }

    /**
     * Plans an insertion using a pre-computed candidate.
     * Used client-side for preview when the cogwheel block has not yet been placed.
     */
    public static @Nullable CogwheelChainPartialEditInsertionPlan planWithCandidate(final CogwheelChain existingChain,
                                                                                    final CogwheelChainPartialEditContext editContext,
                                                                                    final BlockPos proposedPos,
                                                                                    final CogwheelChainCandidate candidate) {
        if (existingChain.getChainType() != editContext.chainType())
            return null;
        if (editContext.segment().type() != CogwheelChainSegment.SegmentType.BETWEEN_NODES)
            return null;

        final ResolvedSegment resolvedSegment = resolveSegment(existingChain, editContext);
        if (resolvedSegment == null)
            return null;
        if (containsNodeAt(resolvedSegment.existingWorldNodes(), proposedPos))
            return null;

        final PlacingCogwheelNode proposedNode = new PlacingCogwheelNode(
                proposedPos, candidate.axis(), candidate.isLarge(), candidate.hasSmallCogwheelOffset());
        if (!hasValidPath(resolvedSegment.startNode(), proposedNode)
                || !hasValidPath(proposedNode, resolvedSegment.endNode()))
            return null;

        final List<PlacingCogwheelNode> rebuiltNodes = new ArrayList<>(resolvedSegment.existingWorldNodes());
        rebuiltNodes.add(resolvedSegment.insertionIndex(), proposedNode);

        final PlacingCogwheelChain rebuiltChain = new PlacingCogwheelChain(rebuiltNodes);
        if (rebuiltChain.maxBounds() > PlacingCogwheelChain.MAX_CHAIN_BOUNDS)
            return null;

        final int oldCost = resolvedSegment.existingChain().getChainsRequiredInLoop();
        final int newCost = rebuiltChain.getChainsRequiredInLoop();
        return new CogwheelChainPartialEditInsertionPlan(
                resolvedSegment.startNode(),
                resolvedSegment.endNode(),
                proposedNode,
                resolvedSegment.insertionIndex(),
                rebuiltChain,
                oldCost,
                newCost
        );
    }

    public static PlacingCogwheelNode toWorldNode(final PathedCogwheelNode pathedNode, final BlockPos controllerPos) {
        return new PlacingCogwheelNode(
                controllerPos.offset(pathedNode.localPos()),
                pathedNode.rotationAxis(),
                pathedNode.isLarge(),
                pathedNode.hasSmallCogwheelOffset()
        );
    }

    public static PlacingCogwheelChain toWorldChain(final CogwheelChain existingChain, final BlockPos controllerPos) {
        return new PlacingCogwheelChain(toWorldNodes(existingChain.getChainPathCogwheelNodes(), controllerPos));
    }

    public static @Nullable CogwheelChainSegment resolveBetweenNodesSegment(final CogwheelChain existingChain,
                                                                            final int startNodeIndex) {
        if (startNodeIndex < 0) {
            return null;
        }

        int betweenNodesIndex = 0;
        for (final CogwheelChainSegment segment : existingChain.getSegments()) {
            if (segment.type() != CogwheelChainSegment.SegmentType.BETWEEN_NODES) {
                continue;
            }
            if (betweenNodesIndex == startNodeIndex) {
                return segment;
            }
            betweenNodesIndex++;
        }
        return null;
    }

    private static @Nullable ResolvedSegment resolveSegment(final CogwheelChain existingChain,
                                                            final CogwheelChainPartialEditContext editContext) {
        final List<PathedCogwheelNode> existingNodes = existingChain.getChainPathCogwheelNodes();
        if (existingNodes.size() < 2) {
            return null;
        }

        final int startNodeIndex = editContext.startNodeIndex();
        if (startNodeIndex < 0 || startNodeIndex >= existingNodes.size()) {
            return null;
        }

        final int endNodeIndex = (startNodeIndex + 1) % existingNodes.size();
        if (editContext.endNodeIndex() != endNodeIndex) {
            return null;
        }

        final CogwheelChainSegment currentSegment = resolveBetweenNodesSegment(existingChain, startNodeIndex);
        if (currentSegment == null || !currentSegment.equals(editContext.segment())) {
            return null;
        }

        final List<PlacingCogwheelNode> existingWorldNodes = toWorldNodes(existingNodes, editContext.controllerPos());
        final PlacingCogwheelNode startNode = existingWorldNodes.get(startNodeIndex);
        final PlacingCogwheelNode endNode = existingWorldNodes.get(endNodeIndex);
        if (!startNode.equals(editContext.startNode()) || !endNode.equals(editContext.endNode())) {
            return null;
        }

        return new ResolvedSegment(
                new PlacingCogwheelChain(existingWorldNodes),
                existingWorldNodes,
                startNode,
                endNode,
                startNodeIndex + 1
        );
    }

    private static List<PlacingCogwheelNode> toWorldNodes(final List<PathedCogwheelNode> existingNodes,
                                                          final BlockPos controllerPos) {
        final List<PlacingCogwheelNode> worldNodes = new ArrayList<>(existingNodes.size());
        for (final PathedCogwheelNode existingNode : existingNodes) {
            worldNodes.add(toWorldNode(existingNode, controllerPos));
        }
        return worldNodes;
    }

    private static boolean containsNodeAt(final List<PlacingCogwheelNode> existingWorldNodes, final BlockPos proposedPos) {
        for (final PlacingCogwheelNode existingNode : existingWorldNodes) {
            if (existingNode.pos().equals(proposedPos)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasValidPath(final PlacingCogwheelNode from, final PlacingCogwheelNode to) {
        return !CogwheelChainPathfinder.getValidPathSteps(from, to).isEmpty();
    }

    private record ResolvedSegment(PlacingCogwheelChain existingChain,
                                   List<PlacingCogwheelNode> existingWorldNodes,
                                   PlacingCogwheelNode startNode,
                                   PlacingCogwheelNode endNode,
                                   int insertionIndex) {
    }
}
