package com.kipti.bnb.content.kinetics.cogwheel_chain.placement;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainCandidate;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainPathfinder;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelNode;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.kipti.bnb.content.kinetics.cogwheel_chain.placement.ChainDriveDisplayRenderer.*;
import static com.kipti.bnb.content.kinetics.cogwheel_chain.placement.CogwheelChainPlacementInteraction.clearPlacingChain;
import static com.kipti.bnb.content.kinetics.cogwheel_chain.placement.CogwheelChainPlacementInteraction.getChainItemInHand;
import static com.kipti.bnb.content.kinetics.cogwheel_chain.placement.CogwheelChainPlacementInteraction.getCurrentBuildingChain;
import static com.kipti.bnb.content.kinetics.cogwheel_chain.placement.CogwheelChainPlacementInteraction.getCurrentChainItemType;
import static com.kipti.bnb.content.kinetics.cogwheel_chain.placement.CogwheelChainPlacementInteraction.getCurrentChainLevel;
import static com.kipti.bnb.content.kinetics.cogwheel_chain.placement.CogwheelChainPlacementInteraction.getCurrentChainType;

/**
 * Client-side display handler for the normal chain-building placement flow.
 * <p>
 * Renders existing chain nodes (shapes + connections) and provides visual feedback
 * for the next target: outliner for valid connections, particles for invalid or no-target cases.
 */
public class CogwheelChainPlacementEffect {

    private static List<PlacingCogwheelNode> lastPlannedNodes = List.of();

    public static void tick(final LocalPlayer player) {
        if (Minecraft.getInstance().isPaused() || Minecraft.getInstance().hitResult == null) return;

        final ClientLevel level = Minecraft.getInstance().level;
        if (level == null || getCurrentChainLevel() == null || getCurrentBuildingChain() == null || getCurrentChainType() == null || getCurrentChainItemType() == null) {
            return;
        }

        if (!getCurrentChainLevel().equals(level.dimension()) || getCurrentBuildingChain().checkMissingNodesInLevel(level, getCurrentChainType())) {
            clearPlacingChain();
            return;
        }

        final ItemStack heldItem = getChainItemInHand(player);
        if (heldItem != null) {
            final BlockPos targetedPos = getTargetedBlockAndDisplay();

            if (!player.hasInfiniteMaterials()) {
                final double additionalDistance = targetedPos != null ?
                        Vec3.atLowerCornerOf(targetedPos.subtract(getCurrentBuildingChain().getLastNode().pos())).length() : 0;
                final int chainsRequired = getCurrentBuildingChain().getChainsRequired(additionalDistance, getCurrentChainType());

                final boolean hasEnough = ChainConveyorBlockEntity.getChainsFromInventory(player, getCurrentChainItemType().getDefaultInstance(), chainsRequired, true);
                BlueprintOverlayRenderer.displayChainRequirements(getCurrentChainItemType(), chainsRequired, hasEnough);
            }
        }
    }

    private static @Nullable BlockPos getTargetedBlockAndDisplay() {
        if (getCurrentBuildingChain() == null || getCurrentChainLevel() == null)
            return null;

        final ClientLevel level = Minecraft.getInstance().level;
        final HitResult genericHit = Minecraft.getInstance().hitResult;
        if (!(genericHit instanceof final BlockHitResult hit)) {
            return null;
        }

        renderExistingChainNodes(level);

        final PlacingCogwheelNode lastNode = getCurrentBuildingChain().getLastNode();
        final BlockPos targetedPos = hit.getBlockPos();
        final BlockState targetedState = level.getBlockState(targetedPos);
        final CogwheelChainCandidate candidate = CogwheelChainCandidate.getForBlock(targetedState);

        if (candidate != null) {
            return displayTargetCandidate(level, lastNode, targetedPos, targetedState, candidate);
        }

        displayProjectedTarget(level, lastNode, hit);
        return null;
    }

    private static void renderExistingChainNodes(final ClientLevel level) {
        for (int i = 0; i < getCurrentBuildingChain().getSize(); i++) {
            ChainDriveDisplayRenderer.renderBlockOutline(level, getCurrentBuildingChain().getNodes().get(i).pos(), VALID_COLOUR);
        }
        renderExistingChainConnections();
    }

    /**
     * Target is a candidate cogwheel: check connection validity.
     * Valid → outliner with connections only (no block shape).
     * Invalid → red particles from last node to target.
     */
    private static @Nullable BlockPos displayTargetCandidate(final ClientLevel level,
                                                              final PlacingCogwheelNode lastNode,
                                                              final BlockPos targetedPos,
                                                              final BlockState targetedState,
                                                              final CogwheelChainCandidate candidate) {
        if (!getCurrentChainType().getCogwheelPredicate().test(targetedState.getBlock())) {
            ChainDriveDisplayRenderer.renderParticlesBetween(level, lastNode.center(), targetedPos.getCenter(), INVALID_COLOUR);
            return null;
        }

        final PlacingCogwheelNode targetNode = new PlacingCogwheelNode(
                targetedPos, candidate.axis(), candidate.isLarge(), candidate.hasSmallCogwheelOffset()
        );
        final @Nullable PlacingCogwheelNode previousNode = getCurrentBuildingChain().getSize() >= 2
                ? getCurrentBuildingChain().getNodes().get(getCurrentBuildingChain().getSize() - 2) : null;

        if (isConnectionValid(lastNode, targetNode, previousNode)) {
            renderTargetConnection(lastNode, targetNode);
            return targetedPos;
        }

        ChainDriveDisplayRenderer.renderParticlesBetween(level, lastNode.center(), targetNode.center(), INVALID_COLOUR);
        return null;
    }

    /**
     * No candidate targeted: project hit onto the last node's axis plane and show particles.
     * Green if within range, red if beyond.
     */
    private static void displayProjectedTarget(final ClientLevel level,
                                                final PlacingCogwheelNode lastNode,
                                                final BlockHitResult hit) {
        final Vec3 lastNodePos = Vec3.atCenterOf(lastNode.pos());
        final Direction.Axis axis = lastNode.rotationAxis();

        final Vec3 targetedOrigin = hit.getLocation();
        final Vec3 toTargeted = targetedOrigin.subtract(lastNodePos);
        final Vec3 axisNormal = Vec3.atLowerCornerOf(Direction.get(Direction.AxisDirection.POSITIVE, axis).getNormal());
        final Vec3 projected = toTargeted.subtract(axisNormal.scale(toTargeted.dot(axisNormal))).add(lastNodePos);

        final double distance = projected.distanceTo(lastNodePos);
        final int colour = distance <= MAX_PLACEMENT_RANGE ? VALID_COLOUR : INVALID_COLOUR;
        ChainDriveDisplayRenderer.renderParticlesBetween(level, lastNode.center(), projected, colour);
    }

    private static boolean isConnectionValid(final PlacingCogwheelNode from,
                                              final PlacingCogwheelNode to,
                                              final @Nullable PlacingCogwheelNode previous) {
        try {
            PlacingCogwheelChain.validateConnection(from, to, previous, getCurrentChainType());
            return true;
        } catch (final ChainInteractionFailedException ignored) {
            return false;
        }
    }

    private static void renderTargetConnection(final PlacingCogwheelNode lastNode,
                                                final PlacingCogwheelNode targetNode) {
        final int[] sides = ChainPlacementPathDisplayHelper.getPathDisplaySides(getCurrentBuildingChain());
        final int lastSide = sides.length > 0 ? sides[sides.length - 1] : 0;

        int targetSide = 0;
        for (int side = -1; side <= 1; side += 2) {
            if (ChainPlacementPathDisplayHelper.doesNodePermitSide(lastNode, targetNode, null, side)) {
                targetSide += side;
            }
        }

        final ChainPlacementPathDisplayHelper.DisplayedSegment segment =
                ChainPlacementPathDisplayHelper.getDisplayedSegment(lastNode, targetNode, lastSide, targetSide);
        ChainDriveDisplayRenderer.renderConnectionSegment("cogwheel_chain_target_connection", segment, VALID_COLOUR);
    }

    private static void renderExistingChainConnections() {
        if (getCurrentBuildingChain() == null) return;
        if (!lastPlannedNodes.equals(getCurrentBuildingChain().getNodes())) {
            lastPlannedNodes = new ArrayList<>(getCurrentBuildingChain().getNodes());
        }

        final int[] sides = ChainPlacementPathDisplayHelper.getPathDisplaySides(getCurrentBuildingChain());

        for (int i = 0; i < getCurrentBuildingChain().getSize() - 1; i++) {
            final PlacingCogwheelNode nodeA = getCurrentBuildingChain().getNodes().get(i);
            final PlacingCogwheelNode nodeB = getCurrentBuildingChain().getNodes().get(i + 1);
            renderExistingSegment(nodeA, nodeB, sides[i], sides[i + 1]);
        }
    }

    private static void renderExistingSegment(final PlacingCogwheelNode nodeA, final PlacingCogwheelNode nodeB,
                                               final int fromSide, final int toSide) {
        final Vec3 fromOffset = fromSide == 0 ? Vec3.ZERO : CogwheelChainPathfinder.getPathingTangentOnCog(nodeB, nodeA, -fromSide);
        final Vec3 toOffset = toSide == 0 ? Vec3.ZERO : CogwheelChainPathfinder.getPathingTangentOnCog(nodeA, nodeB, toSide);
        ChainDriveDisplayRenderer.renderConnectionLine(
                "cogwheel_chain_placement_pathing_" + nodeA.pos() + "_" + nodeB.pos() + "_from_" + fromSide + "_to_" + toSide,
                nodeA.center().add(fromOffset),
                nodeB.center().add(toOffset),
                VALID_COLOUR
        );
    }

}

