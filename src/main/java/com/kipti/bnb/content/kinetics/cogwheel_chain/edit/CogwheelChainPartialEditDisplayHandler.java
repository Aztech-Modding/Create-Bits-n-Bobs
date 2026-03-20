package com.kipti.bnb.content.kinetics.cogwheel_chain.edit;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainCandidate;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainPathfinder;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PathedCogwheelNode;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelNode;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
import com.kipti.bnb.content.kinetics.cogwheel_chain.world.CogwheelChainWorld;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Renders placement preview outlines and cost overlays during a partial chain edit.
 * <p>
 * When the player has entered partial edit mode via
 * {@link CogwheelChainPartialEditInteractionHandler}, this handler renders:
 * <ul>
 *   <li>Block outlines on the proposed cogwheel position</li>
 *   <li>Connection lines between the proposed cogwheel and its chain neighbours</li>
 *   <li>Cost difference overlay via {@link BlueprintOverlayRenderer}</li>
 * </ul>
 */
@EventBusSubscriber(Dist.CLIENT)
public class CogwheelChainPartialEditDisplayHandler {

    private static final float PARTICLE_DENSITY = 0.1f;
    private static final int OUTLINE_COLOUR = 0xEBA832;

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused() || mc.player == null || mc.level == null)
            return;

        tick(mc.player);
    }

    private static void tick(final LocalPlayer player) {
        final BlockPos controllerPos = CogwheelChainPartialEditInteractionHandler.getEditingControllerPos();
        final BlockPos proposedPos = CogwheelChainPartialEditInteractionHandler.getProposedCogwheelPos();
        final CogwheelChainType chainType = CogwheelChainPartialEditInteractionHandler.getEditingChainType();

        if (controllerPos == null || proposedPos == null || chainType == null)
            return;

        final ClientLevel level = Minecraft.getInstance().level;
        if (level == null)
            return;

        final CogwheelChainWorld chainWorld = CogwheelChainWorld.get(level);
        final CogwheelChain existingChain = chainWorld.getChain(controllerPos);
        if (existingChain == null) {
            CogwheelChainPartialEditInteractionHandler.clearEditState();
            return;
        }

        renderProposedCogwheelOutline(level, proposedPos);
        renderConnectionSegments(existingChain, controllerPos, proposedPos);
        renderCostOverlay(player, existingChain, controllerPos, proposedPos);
    }

    private static void renderProposedCogwheelOutline(final ClientLevel level, final BlockPos pos) {
        final AtomicInteger counter = new AtomicInteger(0);
        level.getBlockState(pos).getShape(level, pos).forAllEdges((fx, fy, fz, tx, ty, tz) -> {
            Outliner.getInstance().showLine(
                            "partial_edit_outline_" + pos + "_" + counter.getAndIncrement(),
                            new Vec3(fx, fy, fz).add(Vec3.atLowerCornerOf(pos)),
                            new Vec3(tx, ty, tz).add(Vec3.atLowerCornerOf(pos)))
                    .colored(OUTLINE_COLOUR)
                    .lineWidth(1 / 16f);
        });
    }

    private static void renderConnectionSegments(final CogwheelChain chain,
                                                  final BlockPos controllerPos,
                                                  final BlockPos proposedPos) {
        final List<PathedCogwheelNode> existingNodes = chain.getChainPathCogwheelNodes();
        final InsertionNeighbours neighbours = findInsertionNeighbours(existingNodes, controllerPos, proposedPos);
        if (neighbours == null)
            return;

        final Vec3 proposedCenter = proposedPos.getCenter();
        final Vec3 neighbourACenter = neighbours.nodeA().center();
        final Vec3 neighbourBCenter = neighbours.nodeB().center();

        Outliner.getInstance().showLine(
                        "partial_edit_segment_from",
                        neighbourACenter,
                        proposedCenter)
                .colored(OUTLINE_COLOUR)
                .lineWidth(2f / 16f);

        Outliner.getInstance().showLine(
                        "partial_edit_segment_to",
                        proposedCenter,
                        neighbourBCenter)
                .colored(OUTLINE_COLOUR)
                .lineWidth(2f / 16f);

        final ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            renderParticlesBetween(level, neighbourACenter, proposedCenter);
            renderParticlesBetween(level, proposedCenter, neighbourBCenter);
        }
    }

    private static void renderCostOverlay(final LocalPlayer player,
                                           final CogwheelChain existingChain,
                                           final BlockPos controllerPos,
                                           final BlockPos proposedPos) {
        final CogwheelChainType chainType = CogwheelChainPartialEditInteractionHandler.getEditingChainType();
        final Item chainItem = CogwheelChainPartialEditInteractionHandler.getEditingChainItemType();
        if (chainType == null || chainItem == null)
            return;

        if (player.hasInfiniteMaterials())
            return;

        final int newCost = estimateNewCost(existingChain, controllerPos, proposedPos);
        final int oldCost = existingChain.getChainsRequired();
        final int costDifference = Math.max(newCost - oldCost, 0);

        final boolean hasEnough = ChainConveyorBlockEntity.getChainsFromInventory(
                player, chainItem.getDefaultInstance(), costDifference, true
        );

        BlueprintOverlayRenderer.displayChainRequirements(chainItem, costDifference, hasEnough);
    }

    private static int estimateNewCost(final CogwheelChain existingChain,
                                        final BlockPos controllerPos,
                                        final BlockPos proposedPos) {
        final List<PathedCogwheelNode> existingNodes = existingChain.getChainPathCogwheelNodes();
        double totalLength = 0;

        final List<PlacingCogwheelNode> worldNodes = new ArrayList<>();
        for (final PathedCogwheelNode pathed : existingNodes) {
            final BlockPos worldPos = controllerPos.offset(pathed.localPos());
            worldNodes.add(new PlacingCogwheelNode(worldPos, pathed.rotationAxis(), pathed.isLarge(), pathed.hasSmallCogwheelOffset()));
        }

        final InsertionNeighbours neighbours = findInsertionNeighbours(existingNodes, controllerPos, proposedPos);
        if (neighbours == null)
            return existingChain.getChainsRequired();

        for (int i = 0; i < worldNodes.size(); i++) {
            final PlacingCogwheelNode nodeA = worldNodes.get(i);
            final PlacingCogwheelNode nodeB = worldNodes.get((i + 1) % worldNodes.size());

            if (i == neighbours.insertIndex()) {
                totalLength += nodeA.center().distanceTo(proposedPos.getCenter());
                totalLength += proposedPos.getCenter().distanceTo(nodeB.center());
            } else {
                totalLength += nodeA.center().distanceTo(nodeB.center());
            }
        }

        return PlacingCogwheelChain.getChainsRequiredForLength(totalLength);
    }

    private static InsertionNeighbours findInsertionNeighbours(final List<PathedCogwheelNode> existingNodes,
                                                                final BlockPos controllerPos,
                                                                final BlockPos proposedPos) {
        final PlacingCogwheelNode proposedNode = createProposedNode(proposedPos);
        if (proposedNode == null)
            return null;

        final int bestIndex = findBestInsertionIndex(existingNodes, controllerPos, proposedPos, proposedNode);
        if (bestIndex == -1)
            return null;

        final PlacingCogwheelNode nodeA = toWorldNode(existingNodes.get(bestIndex), controllerPos);
        final PlacingCogwheelNode nodeB = toWorldNode(existingNodes.get((bestIndex + 1) % existingNodes.size()), controllerPos);

        return new InsertionNeighbours(nodeA, nodeB, bestIndex);
    }

    private static @Nullable PlacingCogwheelNode createProposedNode(final BlockPos proposedPos) {
        final ClientLevel level = Minecraft.getInstance().level;
        if (level == null)
            return null;

        final BlockState proposedState = level.getBlockState(proposedPos);
        final CogwheelChainCandidate candidate = CogwheelChainCandidate.getForBlock(proposedState);
        if (candidate == null)
            return null;

        return new PlacingCogwheelNode(
                proposedPos,
                candidate.axis(),
                candidate.isLarge(),
                candidate.hasSmallCogwheelOffset()
        );
    }

    private static int findBestInsertionIndex(final List<PathedCogwheelNode> existingNodes,
                                               final BlockPos controllerPos,
                                               final BlockPos proposedPos,
                                               final PlacingCogwheelNode proposedNode) {
        double bestDistance = Double.MAX_VALUE;
        int bestIndex = -1;

        for (int i = 0; i < existingNodes.size(); i++) {
            final PathedCogwheelNode pathedA = existingNodes.get(i);
            final PathedCogwheelNode pathedB = existingNodes.get((i + 1) % existingNodes.size());

            final PlacingCogwheelNode nodeA = toWorldNode(pathedA, controllerPos);
            final PlacingCogwheelNode nodeB = toWorldNode(pathedB, controllerPos);

            final boolean connectsFromA = !CogwheelChainPathfinder.getValidPathSteps(nodeA, proposedNode).isEmpty();
            final boolean connectsToB = !CogwheelChainPathfinder.getValidPathSteps(proposedNode, nodeB).isEmpty();

            if (!connectsFromA || !connectsToB)
                continue;

            final double distance = nodeA.center().distanceTo(proposedPos.getCenter())
                    + proposedPos.getCenter().distanceTo(nodeB.center());
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
            }
        }

        return bestIndex;
    }

    private static PlacingCogwheelNode toWorldNode(final PathedCogwheelNode pathed, final BlockPos controllerPos) {
        return new PlacingCogwheelNode(
                controllerPos.offset(pathed.localPos()),
                pathed.rotationAxis(),
                pathed.isLarge(),
                pathed.hasSmallCogwheelOffset()
        );
    }

    private static void renderParticlesBetween(final ClientLevel level, final Vec3 from, final Vec3 to) {
        final Vec3 delta = to.subtract(from);
        final double length = delta.length();
        if (length < 1.0E-3 || length > 256)
            return;

        final Vec3 dir = delta.normalize();
        final double step = 0.25;

        for (double t = 0; t <= length; t += step) {
            if (level.getRandom().nextFloat() > PARTICLE_DENSITY)
                continue;

            final Vec3 lerped = from.add(dir.scale(t));
            level.addParticle(
                    new DustParticleOptions(new Vector3f(0xEB / 256f, 0xA8 / 256f, 0x32 / 256f), 1), true,
                    lerped.x, lerped.y, lerped.z, 0, 0, 0
            );
        }
    }

    /**
     * The two chain nodes between which the proposed cogwheel would be inserted.
     */
    private record InsertionNeighbours(PlacingCogwheelNode nodeA, PlacingCogwheelNode nodeB, int insertIndex) {
    }
}
