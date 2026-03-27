package com.kipti.bnb.content.kinetics.cogwheel_chain.graph;

import com.kipti.bnb.content.kinetics.cogwheel_chain.placement.ChainInteractionFailedException;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
import com.kipti.bnb.registry.core.BnbConfigs;
import com.mojang.serialization.Codec;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Used during the construction of a cogwheel chain
 */
public class PlacingCogwheelChain {

    public static final Codec<PlacingCogwheelChain> CODEC = PlacingCogwheelNode.CODEC.listOf().xmap(
            PlacingCogwheelChain::new,
            chain -> chain.visitedNodes
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PlacingCogwheelChain> STREAM_CODEC = StreamCodec.composite(
            CatnipStreamCodecBuilders.list(PlacingCogwheelNode.STREAM_CODEC),
            chain -> chain.visitedNodes,
            PlacingCogwheelChain::new
    );
    public static final Integer MAX_CHAIN_BOUNDS = 32;//TODO config

    private List<PlacingCogwheelNode> visitedNodes;

    public PlacingCogwheelChain(final BlockPos startPos,
                                final Direction.Axis startAxis,
                                final boolean isLarge,
                                final boolean hasSmallCogwheelOffset) {
        this.visitedNodes = new ArrayList<>(List.of(new PlacingCogwheelNode(
                startPos,
                startAxis,
                isLarge,
                hasSmallCogwheelOffset
        )));
    }

    public PlacingCogwheelChain(final List<PlacingCogwheelNode> nodes) {
        this.visitedNodes = new ArrayList<>(nodes);
    }

    public int getChainsRequiredInLoop() {
        return this.getChainsRequired(Vec3.atLowerCornerOf(this.visitedNodes.getLast().pos().subtract(this.visitedNodes.getFirst().pos())).length());
    }

    /**
     * Get the number of chains required to build this chain, given an extra length
     */
    public int getChainsRequired(double length) {
        final float factor = BnbConfigs.server().COGWHEEL_CHAIN_DRIVE_COST_FACTOR.getF();
        if (factor == 0) {
            return 0;
        }

        for (int i = 0; i < this.visitedNodes.size() - 1; i++) {
            final Vec3i offset = this.visitedNodes.get(i + 1).pos().subtract(this.visitedNodes.get(i).pos());
            length += Vec3.atLowerCornerOf(offset).length();
        }
        return getChainsRequiredForLength(length);
    }

    public static int getChainsRequiredForLength(final double length) {
        final float factor = BnbConfigs.server().COGWHEEL_CHAIN_DRIVE_COST_FACTOR.getF();
        if (factor == 0) {
            return 0;
        }
        return (int) Math.max(Math.round(factor * length / 5), 1);
    }

    public boolean tryAddNode(final BlockPos newPos,
                              final BlockState newBlockState,
                              final CogwheelChainType type) throws ChainInteractionFailedException {
        final PlacingCogwheelNode lastNode = this.getLastNode();

        final CogwheelChainCandidate candidate = CogwheelChainCandidate.getForBlock(newBlockState);
        if (candidate == null) {
            return false;
        }

        if (!type.getCogwheelPredicate().test(newBlockState.getBlock())) {
            throw new ChainInteractionFailedException("invalid_cogwheel_type." + type.getTranslationKey());
        }

        for (int i = 1; i < this.visitedNodes.size(); i++) {
            if (this.visitedNodes.get(i).pos().equals(newPos)) {
                throw new ChainInteractionFailedException("cannot_revisit_node");
            }
        }

        final PlacingCogwheelNode newNode = new PlacingCogwheelNode(
                newPos, candidate.axis(), candidate.isLarge(), candidate.hasSmallCogwheelOffset()
        );

        if (this.exceedsMaxBounds(newNode)) {
            throw new ChainInteractionFailedException("out_of_bounds");
        }

        final @Nullable PlacingCogwheelNode previous = this.getSize() >= 2
                ? this.visitedNodes.get(this.visitedNodes.size() - 2) : null;

        validateConnection(lastNode, newNode, previous, type);

        this.visitedNodes.add(newNode);
        return true;
    }

    /**
     * Validates the geometric and pathfinding connection between two cogwheel nodes.
     * Throws {@link ChainInteractionFailedException} if the connection is invalid.
     *
     * @param from      the node to connect from
     * @param to        the node to connect to
     * @param previous  the node before {@code from} in the chain, or null if none
     * @param chainType the chain type governing axis-change rules
     */
    public static void validateConnection(final PlacingCogwheelNode from,
                                          final PlacingCogwheelNode to,
                                          final @Nullable PlacingCogwheelNode previous,
                                          final CogwheelChainType chainType) throws ChainInteractionFailedException {
        final Direction.Axis axis = to.rotationAxis();
        final boolean isSameAxis = axis == from.rotationAxis();
        final Vec3i difference = to.pos().subtract(from.pos());
        final boolean isFlat = difference.get(to.rotationAxis()) == 0;
        final double totalRadius = (to.isLarge() ? 1 : 0.5) + (from.isLarge() ? 1 : 0.5);
        final boolean isAdjacent = to.pos().distSqr(from.pos()) <= totalRadius * totalRadius;
        final boolean isValidFlat = isFlat && isSameAxis && !isAdjacent;
        final boolean isAxisChangePermitted = chainType.permitsAxisChanges();
        final boolean isValidAxisChange = isAxisChangePermitted && isValidLargeCogAxisConnection(
                from, to.pos(), axis, to.isLarge()
        ); //TODO: forgot to undelete non flat being valid

        final boolean isValidCandidate = isValidFlat || isValidAxisChange;

        if (!isValidCandidate) {
            if (!isSameAxis && !isAxisChangePermitted) {
                throw new ChainInteractionFailedException("axis_change_forbidden_by_type");
            }

            if (isAdjacent) {
                throw new ChainInteractionFailedException("cogwheels_cannot_touch");
            }

            if (!isSameAxis) {
                throw new ChainInteractionFailedException("not_valid_axis_change");
            }

            throw new ChainInteractionFailedException("not_flat_connection");
        }

        final List<Integer> backwardsConnections = CogwheelChainPathfinder.getValidPathSteps(from, to);
        if (backwardsConnections.isEmpty()) {
            throw new ChainInteractionFailedException("no_cogwheel_connection");
        }

        if (previous != null) {
            boolean hasPathBack = false;
            for (final Integer side : backwardsConnections) {
                hasPathBack = hasPathBack ||
                        CogwheelChainPathfinder.isValidPathStep(previous, 1, from, side) ||
                        CogwheelChainPathfinder.isValidPathStep(previous, -1, from, side);
            }
            if (!hasPathBack) {
                throw new ChainInteractionFailedException("no_path_to_cogwheel");
            }
        }
    }

    static boolean isValidLargeCogAxisConnection(final PlacingCogwheelNode lastNode,
                                                 final BlockPos newPos,
                                                 final Direction.Axis axis,
                                                 final boolean isLarge) {
        if (!lastNode.isLarge() || !isLarge) {
            return false;
        }

        // Check that they are one block apart on the two axes perpendicular to the rotation axes
        final Vec3i diff = newPos.subtract(lastNode.pos());

        final int safeAxisOrdinal = 0x7 & ~(1 << axis.ordinal()) & ~(1 << lastNode.rotationAxis().ordinal());
        final int[] component = {diff.getX(), diff.getY(), diff.getZ()};
        for (int i = 0; i < 3; i++) {
            if (0b1 << i == safeAxisOrdinal) {
                if (Math.abs(component[i]) < 1) {
                    return false;
                }
            } else {
                if (Math.abs(component[i]) != 1) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public boolean equals(final Object o) {
        if (o == null || this.getClass() != o.getClass()) return false;
        final PlacingCogwheelChain that = (PlacingCogwheelChain) o;
        return Objects.equals(this.visitedNodes, that.visitedNodes);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.visitedNodes);
    }

    public boolean canBuildChainIfLooping() throws ChainInteractionFailedException {
        if (this.getSize() < 2) return false;
        final PlacingCogwheelNode firstNode = this.visitedNodes.getFirst();
        final PlacingCogwheelNode lastNode = this.getLastNode();
        if (!firstNode.pos().equals(lastNode.pos())) return false;

        // Remove last chainNode to avoid duplication
        this.visitedNodes.removeLast();
        if (CogwheelChainPathfinder.buildChainPath(this) == null) {
            throw new ChainInteractionFailedException("pathfinding_failed");
        }
        return true;
    }

    public List<PlacingCogwheelNode> getNodes() {
        return this.visitedNodes;
    }

    public PlacingCogwheelNode getNodeLooped(final int i) {
        return this.visitedNodes.get((this.visitedNodes.size() + (i % this.visitedNodes.size())) % this.visitedNodes.size());
    }

    public PlacingCogwheelNode getFirstNode() {
        return this.visitedNodes.getFirst();
    }

    public PlacingCogwheelNode getLastNode() {
        return this.visitedNodes.getLast();
    }

    public Vec3 getNodeCenter(final int i) {
        return this.visitedNodes.get(i).pos().getCenter();
    }

    public int getSize() {
        return this.visitedNodes.size();
    }

    public int maxBounds() {
        return this.getMaxBoundsOfNodes(this.visitedNodes);
    }

    public boolean exceedsMaxBounds(final PlacingCogwheelNode candidate) {
        final List<PlacingCogwheelNode> nodesWithCandidate = new ArrayList<>(this.visitedNodes);
        nodesWithCandidate.add(candidate);
        final int newMaxBounds = this.getMaxBoundsOfNodes(nodesWithCandidate);
        return newMaxBounds > MAX_CHAIN_BOUNDS;
    }

    private int getMaxBoundsOfNodes(final List<PlacingCogwheelNode> nodes) {
        Vec3i min = new Vec3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        Vec3i max = new Vec3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

        for (final PlacingCogwheelNode node : nodes) {
            final BlockPos pos = node.pos();
            min = new Vec3i(
                    Math.min(min.getX(), pos.getX()),
                    Math.min(min.getY(), pos.getY()),
                    Math.min(min.getZ(), pos.getZ())
            );
            max = new Vec3i(
                    Math.max(max.getX(), pos.getX()),
                    Math.max(max.getY(), pos.getY()),
                    Math.max(max.getZ(), pos.getZ())
            );
        }

        return Math.max(Math.max(max.getX() - min.getX(), max.getY() - min.getY()), max.getZ() - min.getZ());
    }

    public boolean checkMissingNodesInLevel(final Level level, final CogwheelChainType type) {
        for (final PlacingCogwheelNode node : this.visitedNodes) {
            final BlockState state = level.getBlockState(node.pos());
            final CogwheelChainCandidate candidate = CogwheelChainCandidate.getForBlock(state);

            if (candidate == null || !candidate.isConsistentWithNode(node) || !type.getCogwheelPredicate().test(state.getBlock())) {
                return true;
            }
        }
        return false;
    }

    public PlacingCogwheelChain toLocalSpaceChain() {
        final BlockPos origin = this.getFirstNode().pos();
        final List<PlacingCogwheelNode> localNodes = new ArrayList<>();
        for (final PlacingCogwheelNode node : this.visitedNodes) {
            final BlockPos localPos = node.pos().subtract(origin);
            localNodes.add(new PlacingCogwheelNode(
                    localPos,
                    node.rotationAxis(),
                    node.isLarge(),
                    node.hasSmallCogwheelOffset()
            ));
        }
        return new PlacingCogwheelChain(localNodes);
    }

    public List<PlacingCogwheelNode> getVisitedNodes() {
        return this.visitedNodes;
    }

    public void setVisitedNodes(final List<PlacingCogwheelNode> visitedNodes) {
        this.visitedNodes = visitedNodes;
    }

}

