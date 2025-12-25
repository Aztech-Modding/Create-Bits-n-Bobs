package com.kipti.bnb.content.cogwheel_chain.graph;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.cogwheel_chain.block.CogwheelChainBlock;
import com.kipti.bnb.content.cogwheel_chain.block.CogwheelChainBlockEntity;
import com.kipti.bnb.registry.BnbBlocks;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CogwheelChain {

    private final List<PathedCogwheelNode> cogwheelNodes;
    private List<RenderedChainPathNode> renderedNodes;

    public CogwheelChain(final CompoundTag tag) {
        renderedNodes = new ArrayList<>();
        cogwheelNodes = new ArrayList<>();
        read(tag);
    }

    public CogwheelChain(final List<PathedCogwheelNode> path) {
        this.cogwheelNodes = path;
        this.renderedNodes = CogwheelChainGeometryBuilder.buildFullChainFromPathNodes(path);
    }

    public @Nullable PathedCogwheelNode getNodeFromControllerOffset(final Vec3i controllerOffset) {
        final Vec3i offsetFromStart = controllerOffset.multiply(-1);

        for (final PathedCogwheelNode cogwheelNode : cogwheelNodes) {
            if (cogwheelNode.localPos().equals(offsetFromStart)) {
                return cogwheelNode;
            }
        }
        return null;
    }

    public boolean checkIntegrity(final Level level, final BlockPos origin) {
        for (final PathedCogwheelNode node : this.cogwheelNodes) {
            final BlockState state = level.getBlockState(node.localPos().offset(origin));
            if (!isValidChainCogwheel(state)) {
                return false;
            }
            final Direction.Axis axis = state.getValue(CogWheelBlock.AXIS);
            final boolean isLarge = state.getBlock() instanceof final CogwheelChainBlock iCogWheel && iCogWheel.isLargeChainCog();
            if (axis != node.rotationAxis() || isLarge != node.isLarge()) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidChainCogwheel(final BlockState state) {
        return BnbBlocks.LARGE_SPROCKET_COGWHEEL_CHAIN.is(state.getBlock()) || BnbBlocks.SMALL_SPROCKET_COGWHEEL_CHAIN.is(state.getBlock()) ||
                BnbBlocks.LARGE_FLANGED_COGWHEEL_CHAIN.is(state.getBlock()) || BnbBlocks.SMALL_FLANGED_COGWHEEL_CHAIN.is(state.getBlock());
    }

    public int getChainsRequired() {
        double length = 0;
        for (int i = 0; i <= cogwheelNodes.size(); i++) {
            final PathedCogwheelNode startNode = cogwheelNodes.get(i % cogwheelNodes.size());
            final PathedCogwheelNode endNode = cogwheelNodes.get((i + 1) % cogwheelNodes.size());
            length += startNode.dist(endNode);
        }
        return PlacingCogwheelChain.getChainsRequiredForLength(length);
    }

    public void write(final CompoundTag tag) {
        tag.putInt("cogwheel_pos_count", cogwheelNodes.size());
        for (int i = 0; i < cogwheelNodes.size(); i++) {
            final CompoundTag posTag = new CompoundTag();
            cogwheelNodes.get(i).write(posTag);
            tag.put("cogwheel_pos_" + i, posTag);
        }
    }

    public void read(final CompoundTag tag) {
        cogwheelNodes.clear();
        final int cogWheelPosCount = tag.getInt("cogwheel_pos_count");
        for (int i = 0; i < cogWheelPosCount; i++) {
            final CompoundTag posTag = tag.getCompound("cogwheel_pos_" + i);
            final PathedCogwheelNode pos = PathedCogwheelNode.read(posTag);
            cogwheelNodes.add(pos);
        }
        renderedNodes.clear();
        renderedNodes = CogwheelChainGeometryBuilder.buildFullChainFromPathNodes(cogwheelNodes);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final CogwheelChain that = (CogwheelChain) o;
        return Objects.equals(renderedNodes, that.renderedNodes);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(renderedNodes);
    }

    public void placeInLevel(final Level level, final PlacingCogwheelChain source) {
        boolean isController = true;
        final BlockPos controllerPos = source.getFirstNode().pos();
        final int chainsUsed = source.getChainsRequiredInLoop();
        for (final PlacingCogwheelNode node : source.getVisitedNodes()) {
            placeChainCogwheelInLevel(level, node, isController, chainsUsed, controllerPos);
            isController = false;
        }

    }

    private void placeChainCogwheelInLevel(final Level level, final PlacingCogwheelNode node, final boolean isController, final int chainsUsed, final BlockPos controllerPos) {
        final BlockState existingState = level.getBlockState(node.pos());

        @Nullable final BlockState newState = CogwheelChainBlock.getChainState(existingState, node.isLarge(), node.rotationAxis());

        if (newState == null) {
            CreateBitsnBobs.LOGGER.error("Failed to place cogwheel chain at {}, existing block {}, because the chain state could not be resolved", node.pos(), existingState);
            return;
        }
        level.setBlockAndUpdate(node.pos(), newState);

        final BlockEntity be = level.getBlockEntity(node.pos());
        if (be instanceof final CogwheelChainBlockEntity chainBE) {
            if (isController) {
                chainBE.setAsController(this);
                chainBE.setChainsUsed(chainsUsed);
            } else {
                chainBE.setController(controllerPos.subtract(node.pos()));
            }
        } else {
            throw new IllegalStateException("Expected CogwheelChainBlockEntity at " + node.pos());
        }
    }

    public void destroy(final Level level, final BlockPos worldPosition) {
        for (final PathedCogwheelNode cogwheel : cogwheelNodes) {
            final BlockPos pos = worldPosition.offset(cogwheel.localPos());
            removeChainCogwheelFromLevelIfPresent(level, pos);
        }
    }

    public static void removeChainCogwheelFromLevelIfPresent(final Level level, final BlockPos pos) {
        final BlockEntity be = level.getBlockEntity(pos);
        final BlockState state = level.getBlockState(pos);
        if (be instanceof CogwheelChainBlockEntity && (state.getBlock() instanceof final CogwheelChainBlock cogwheelChainBlock)) {
            level.setBlockAndUpdate(pos, cogwheelChainBlock.getSourceBlockState()
                    .setValue(CogwheelChainBlock.AXIS, state.getValue(CogwheelChainBlock.AXIS)));
        }
    }

    /**
     * All nodes in the chain, there are typically multiple, as the path wraps around cogwheels
     */
    public List<RenderedChainPathNode> getChainPathNodes() {
        return renderedNodes;
    }

    /**
     * Each cogwheel in the chain
     */
    public List<PathedCogwheelNode> getChainPathCogwheelNodes() {
        return cogwheelNodes;
    }
}
