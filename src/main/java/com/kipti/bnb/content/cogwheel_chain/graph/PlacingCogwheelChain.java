package com.kipti.bnb.content.cogwheel_chain.graph;

import com.kipti.bnb.registry.BnbBlocks;
import com.kipti.bnb.registry.BnbConfigs;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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

    public static CompoundTag writeToNbt(PlacingCogwheelChain chain) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("size", chain.getSize());
        for (int i = 0; i < chain.getSize(); i++) {
            PlacingCogwheelNode node = chain.getVisitedNodes().get(i);
            CompoundTag nodeTag = new CompoundTag();
            nodeTag.putLong("pos", node.pos().asLong());
            nodeTag.putString("axis", node.rotationAxis().name());
            nodeTag.putBoolean("isLarge", node.isLarge());
            nodeTag.putBoolean("hasOffsetForSmallCogwheel", node.hasOffsetForSmallCogwheel());
            tag.put("node_" + i, nodeTag);
        }
        return tag;
    }

    public static PlacingCogwheelChain readFromNbt(CompoundTag tag) {
        int size = tag.getInt("size");
        List<PlacingCogwheelNode> nodes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            CompoundTag nodeTag = tag.getCompound("node_" + i);
            BlockPos pos = BlockPos.of(nodeTag.getLong("pos"));
            Direction.Axis axis = Direction.Axis.byName(nodeTag.getString("axis"));
            boolean isLarge = nodeTag.getBoolean("isLarge");
            boolean hasOffsetForSmallCogwheel = nodeTag.getBoolean("hasOffsetForSmallCogwheel");
            nodes.add(new PlacingCogwheelNode(pos, axis, isLarge, hasOffsetForSmallCogwheel));
        }
        return new PlacingCogwheelChain(nodes);
    }

    public static void writeToBuffer(PlacingCogwheelChain chain, FriendlyByteBuf buf) {
        buf.writeInt(chain.getSize());
        for (PlacingCogwheelNode node : chain.getVisitedNodes()) {
            buf.writeBlockPos(node.pos());
            buf.writeEnum(node.rotationAxis());
            buf.writeBoolean(node.isLarge());
            buf.writeBoolean(node.hasOffsetForSmallCogwheel());
        }
    }

    public static PlacingCogwheelChain readFromBuffer(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<PlacingCogwheelNode> nodes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            BlockPos pos = buf.readBlockPos();
            Direction.Axis axis = buf.readEnum(Direction.Axis.class);
            boolean isLarge = buf.readBoolean();
            boolean hasOffsetForSmallCogwheel = buf.readBoolean();
            nodes.add(new PlacingCogwheelNode(pos, axis, isLarge, hasOffsetForSmallCogwheel));
        }
        return new PlacingCogwheelChain(nodes);
    }

    public static final Integer MAX_CHAIN_BOUNDS = 32;//TODO config

    private List<PlacingCogwheelNode> visitedNodes;

    public PlacingCogwheelChain(final BlockPos startPos, final Direction.Axis startAxis, final boolean isLarge, final boolean hasSmallCogwheelOffset) {
        this.visitedNodes = new ArrayList<>(List.of(new PlacingCogwheelNode(startPos, startAxis, isLarge, hasSmallCogwheelOffset)));
    }

    public PlacingCogwheelChain(final List<PlacingCogwheelNode> nodes) {
        visitedNodes = new ArrayList<>(nodes);
    }

    public int getChainsRequiredInLoop() {
        return getChainsRequired(Vec3.atLowerCornerOf(visitedNodes.get(visitedNodes.size()-1).pos().subtract(visitedNodes.get(0).pos())).length());
    }

    /**
     * Get the number of chains required to build this chain, given an extra length
     */
    public int getChainsRequired(double length) {
        final float factor = BnbConfigs.server().COGWHEEL_CHAIN_DRIVE_COST_FACTOR.getF();
        if (factor == 0) {
            return 0;
        }

        for (int i = 0; i < visitedNodes.size() - 1; i++) {
            final Vec3i offset = visitedNodes.get(i + 1).pos().subtract(visitedNodes.get(i).pos());
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

    public static boolean isValidBlockTarget(final BlockState state) {
        return AllBlocks.COGWHEEL.has(state) ||  AllBlocks.LARGE_COGWHEEL.has(state) ||
                BnbBlocks.SMALL_EMPTY_FLANGED_COGWHEEL.has(state) || BnbBlocks.LARGE_EMPTY_FLANGED_COGWHEEL.has(state);
    }

    public static boolean isLargeBlockTarget(final BlockState state) {
        return AllBlocks.LARGE_COGWHEEL.has(state) || BnbBlocks.LARGE_EMPTY_FLANGED_COGWHEEL.has(state);
    }

    public static boolean hasSmallCogwheelOffset(final BlockState state) {
        return AllBlocks.COGWHEEL.has(state);
    }


    public boolean tryAddNode(final BlockPos newPos, final BlockState newBlockState) throws ChainInteractionFailedException {
        final PlacingCogwheelNode lastNode = getLastNode();

        if (!isValidBlockTarget(newBlockState)) {
            return false;
        }

        //For each node, check if this is already in the list
        for (int i = 1; i < visitedNodes.size(); i++) {
            if (visitedNodes.get(i).pos().equals(newPos)) {
                throw new ChainInteractionFailedException("cannot_revisit_node");
            }
        }
        final Direction.Axis axis = newBlockState.getValue(CogWheelBlock.AXIS);
        final boolean isLarge = isLargeBlockTarget(newBlockState);
        final boolean hasSmallCogwheelOffset = hasSmallCogwheelOffset(newBlockState);

        final PlacingCogwheelNode newNode = new PlacingCogwheelNode(newPos, axis, isLarge, hasSmallCogwheelOffset);

        final boolean isWithinBounds = !exceedsMaxBounds(newNode);
        if (!isWithinBounds) {
            throw new ChainInteractionFailedException("out_of_bounds");
        }

        final int differenceOnAxis = Math.abs(newPos.get(axis) - lastNode.pos().get(axis));
        final @Nullable PlacingCogwheelNode lastLastNode = getSize() >= 2 ? visitedNodes.get(visitedNodes.size() - 2) : null;

        final boolean isFlat = differenceOnAxis == 0;
        final boolean isSameAxis = axis == lastNode.rotationAxis();
        final double totalRadius = (isLarge ? 1 : 0.5) + (lastNode.isLarge() ? 1 : 0.5);
        final boolean isAdjacent = isFlat && newPos.distSqr(lastNode.pos()) <= totalRadius * totalRadius;
        final boolean isValidFlat = isSameAxis && isFlat && !isAdjacent;
        final boolean isValidAxisChange = isValidLargeCogAxisConnection(lastNode, newPos, axis, isLarge);

        final boolean isValidCandidate = isValidFlat || isValidAxisChange;

        if (!isValidCandidate) {
            if (isAdjacent) {
                throw new ChainInteractionFailedException("cogwheels_cannot_touch");
            }

            if (!isSameAxis) {
                throw new ChainInteractionFailedException("not_valid_axis_change");
            }
            //Else it wasn't accepted because it wasn't flat
            throw new ChainInteractionFailedException("not_flat_connection");
        }

        //Final validity check, look by pathfinding if this cogwheel can connect to the last one

        //Check there is a side which it can connect backwards by, and that that connection can go back
        final List<Integer> backwardsConnections = CogwheelChainPathfinder.getValidPathSteps(lastNode, newNode);
        if (backwardsConnections.isEmpty()) {
            throw new ChainInteractionFailedException("no_cogwheel_connection");
        }

        if (lastLastNode != null) {
            boolean hasPathBack = false;
            for (final Integer side : backwardsConnections) {
                hasPathBack = hasPathBack ||
                        CogwheelChainPathfinder.isValidPathStep(lastLastNode, 1, lastNode, side) ||
                        CogwheelChainPathfinder.isValidPathStep(lastLastNode, -1, lastNode, side);
            }
            if (!hasPathBack) {
                throw new ChainInteractionFailedException("no_path_to_cogwheel");
            }
        }

        visitedNodes.add(newNode);
        return true;
    }

    private boolean isValidLargeCogAxisConnection(final PlacingCogwheelNode lastNode, final BlockPos newPos, final Direction.Axis axis, final boolean isLarge) {
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
        if (o == null || getClass() != o.getClass()) return false;
        final PlacingCogwheelChain that = (PlacingCogwheelChain) o;
        return Objects.equals(visitedNodes, that.visitedNodes);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(visitedNodes);
    }

    public boolean canBuildChainIfLooping() throws ChainInteractionFailedException {
        if (getSize() < 2) return false;
        final PlacingCogwheelNode firstNode = visitedNodes.get(0);
        final PlacingCogwheelNode lastNode = getLastNode();
        if (!firstNode.pos().equals(lastNode.pos())) return false;

        // Remove last chainNode to avoid duplication
        visitedNodes.remove(visitedNodes.size()-1);
        if (CogwheelChainPathfinder.buildChainPath(this) == null) {
            throw new ChainInteractionFailedException("pathfinding_failed");
        }
        return true;
    }

    public List<PlacingCogwheelNode> getNodes() {
        return visitedNodes;
    }

    public PlacingCogwheelNode getNodeLooped(final int i) {
        return visitedNodes.get((visitedNodes.size() + (i % visitedNodes.size())) % visitedNodes.size());
    }

    public PlacingCogwheelNode getFirstNode() {
        return visitedNodes.get(0);
    }

    public PlacingCogwheelNode getLastNode() {
        return visitedNodes.get(visitedNodes.size()-1);
    }

    public Vec3 getNodeCenter(final int i) {
        return visitedNodes.get(i).pos().getCenter();
    }

    public int getSize() {
        return visitedNodes.size();
    }

    public int maxBounds() {
        return getMaxBoundsOfNodes(visitedNodes);
    }

    public boolean exceedsMaxBounds(final PlacingCogwheelNode candidate) {
        final List<PlacingCogwheelNode> nodesWithCandidate = new ArrayList<>(visitedNodes);
        nodesWithCandidate.add(candidate);
        final int newMaxBounds = getMaxBoundsOfNodes(nodesWithCandidate);
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

    public boolean checkMatchingNodesInLevel(final Level level) {
        for (final PlacingCogwheelNode node : visitedNodes) {
            final BlockState state = level.getBlockState(node.pos());
            if (!isValidBlockTarget(state)) {
                return false;
            }
            final Direction.Axis axis = state.getValue(CogWheelBlock.AXIS);
            final boolean isLarge = isLargeBlockTarget(state);
            final boolean hasSmallCogwheelOffset = hasSmallCogwheelOffset(state);
            if (axis != node.rotationAxis() || isLarge != node.isLarge() || hasSmallCogwheelOffset != node.hasOffsetForSmallCogwheel()) {
                return false;
            }
        }
        return true;
    }

    public PlacingCogwheelChain toLocalSpaceChain() {
        final BlockPos origin = getFirstNode().pos();
        final List<PlacingCogwheelNode> localNodes = new ArrayList<>();
        for (final PlacingCogwheelNode node : visitedNodes) {
            final BlockPos localPos = node.pos().subtract(origin);
            localNodes.add(new PlacingCogwheelNode(localPos, node.rotationAxis(), node.isLarge(), node.hasOffsetForSmallCogwheel()));
        }
        return new PlacingCogwheelChain(localNodes);
    }

    public List<PlacingCogwheelNode> getVisitedNodes() {
        return visitedNodes;
    }

    public void setVisitedNodes(final List<PlacingCogwheelNode> visitedNodes) {
        this.visitedNodes = visitedNodes;
    }

    public void removeLastNode() {
        if (!visitedNodes.isEmpty()) {
            visitedNodes.remove(visitedNodes.size() - 1);
        }
    }
}
