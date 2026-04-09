package com.kipti.bnb.content.kinetics.cogwheel_chain.graph;

import com.kipti.bnb.content.kinetics.cogwheel_chain.placement.ChainInteractionFailedException;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the result of attempting to build a residual chain after removing a node from an existing chain.
 *
 * @param chain        the rebuilt {@link CogwheelChain} with pathed geometry
 * @param placingChain the world-space {@link PlacingCogwheelChain} used to place the residual chain in the level
 */
public record ResidualChainResult(CogwheelChain chain, PlacingCogwheelChain placingChain) {
    /**
     * Attempts to build a valid chain from the remaining nodes after removing the node at the given world position.
     *
     * @return the rebuilt {@link ResidualChainResult} if valid, or {@code null} if the residual nodes cannot form a valid chain loop
     */
    public static @Nullable ResidualChainResult tryBuildResidualChain(final CogwheelChain existingChain,
                                                                      final BlockPos controllerWorldPos,
                                                                      final BlockPos removedWorldPos) {
        final BlockPos removedLocalPos = removedWorldPos.subtract(controllerWorldPos);
        final List<PathedCogwheelNode> remaining = new ArrayList<>(existingChain.getChainPathCogwheelNodes());
        remaining.removeIf(node -> node.localPos().equals(removedLocalPos));

        if (remaining.size() < 2) return null;

        final PlacingCogwheelChain residualPlacingChain = calculateResidualPlacement(
                controllerWorldPos,
                remaining
        );

        try {
            final List<PathedCogwheelNode> chainGeometry = CogwheelChainPathfinder.buildChainPath(residualPlacingChain);
            if (chainGeometry == null) return null;

            final CogwheelChain residualChain = new CogwheelChain(
                    chainGeometry, existingChain.getChainType(), existingChain.getReturnedItem()
            );

            return new ResidualChainResult(residualChain, residualPlacingChain);
        } catch (final ChainInteractionFailedException e) {
            return null;
        }
    }

    private static @NonNull PlacingCogwheelChain calculateResidualPlacement(final BlockPos controllerWorldPos,
                                                                            final List<PathedCogwheelNode> remaining) {
        final List<PlacingCogwheelNode> worldNodes = new ArrayList<>(remaining.size());
        for (final PathedCogwheelNode node : remaining) {
            worldNodes.add(new PlacingCogwheelNode(
                    controllerWorldPos.offset(node.localPos()),
                    node.rotationAxis(),
                    node.isLarge(),
                    node.hasSmallCogwheelOffset()
            ));
        }

        return new PlacingCogwheelChain(worldNodes);
    }
}
