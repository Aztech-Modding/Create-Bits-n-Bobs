package com.kipti.bnb.content.kinetics.cogwheel_chain.placement;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainPathfinder;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelNode;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicInteger;

import static com.kipti.bnb.content.kinetics.cogwheel_chain.placement.CogwheelChainPlacementInteraction.currentBuildingChain;
import static com.kipti.bnb.content.kinetics.cogwheel_chain.placement.CogwheelChainPlacementInteraction.currentChainLevel;

public class CogwheelChainPlacementEffect {

    private static final float PARTICLE_DENSITY = 0.1f;

    public static void tick(final LocalPlayer player) {
        if (Minecraft.getInstance().isPaused() || Minecraft.getInstance().hitResult == null) return;

        final ClientLevel level = Minecraft.getInstance().level;
        if (level == null || currentChainLevel == null || currentBuildingChain == null) {
            return;
        }
        if (!currentChainLevel.equals(level.dimension()) || !currentBuildingChain.checkMatchingNodesInLevel(level)) {
            currentBuildingChain = null;
            currentChainLevel = null;
            return;
        }


        //Get held chain
        final ItemStack heldItem = isChain(player.getMainHandItem()) ? player.getMainHandItem() :
                isChain(player.getOffhandItem()) ? player.getOffhandItem() : null;
        if (heldItem != null) {
            final BlockPos targetedPos = getTargetedBlockAndDisplay();

            if (!player.hasInfiniteMaterials()) {
                final double additionalDistance = targetedPos != null ?
                        Vec3.atLowerCornerOf(targetedPos.subtract(currentBuildingChain.getLastNode().pos())).length() : 0;
                final int chainsRequired = currentBuildingChain.getChainsRequired(additionalDistance);

                final boolean hasEnough = ChainConveyorBlockEntity.getChainsFromInventory(player, Items.CHAIN.getDefaultInstance(), chainsRequired, true);
                BlueprintOverlayRenderer.displayChainRequirements(Items.CHAIN, chainsRequired, hasEnough);
            }
        }
    }

    private static boolean isChain(final ItemStack offhandItem) {
        return offhandItem.is(Items.CHAIN);
    }

    private static @Nullable BlockPos getTargetedBlockAndDisplay() {
        if (currentBuildingChain == null)
            return null;

        final ClientLevel level = Minecraft.getInstance().level;

        final HitResult genericHit = Minecraft.getInstance().hitResult;
        if (!(genericHit instanceof BlockHitResult hit)) {
            return null;
        }

        //Get last chainNode to calculate a chain preview
        final PlacingCogwheelNode lastNode = currentBuildingChain.getLastNode();
        final Vec3 lastNodePos = Vec3.atCenterOf(lastNode.pos());
        final Direction.Axis axis = lastNode.rotationAxis();

        //Project the current targeted position onto the plane defined by the last chainNode's axis
        final Vec3 targetedOrigin = hit.getLocation();
        final Vec3 toTargeted = targetedOrigin.subtract(lastNodePos);

        final Vec3 axisNormal = Vec3.atLowerCornerOf(Direction.get(Direction.AxisDirection.POSITIVE, axis).getNormal());
        final Vec3 projected = toTargeted.subtract(axisNormal.scale(toTargeted.dot(axisNormal))).add(lastNodePos);

//        Vec3 lastPos = currentBuildingChain.getNodeCenter(0);
//        for (int i = 1; i < currentBuildingChain.getSize(); i++) {
//            final Vec3 currentPos = currentBuildingChain.getNodeCenter(i);
//            renderParticlesBetween(level, lastPos, currentPos);
//            lastPos = currentPos;
//        }
        for (int i = 0; i < currentBuildingChain.getSize(); i++) {
            showBlockOutline(level, currentBuildingChain.getNodes().get(i).pos());
        }

        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < currentBuildingChain.getSize() - 1; i++) {
                final PlacingCogwheelNode nodeA = currentBuildingChain.getNodes().get(i);
                final PlacingCogwheelNode nodeB = currentBuildingChain.getNodes().get(i + 1);
                if (CogwheelChainPathfinder.isValidPathStep(nodeA, side, nodeB, side)) {
                    final Vec3 pathingTangentB = CogwheelChainPathfinder.getPathingTangentOnCog(nodeA, nodeB, side);
                    final Vec3 pathingTangentA = CogwheelChainPathfinder.getPathingTangentOnCog(nodeB, nodeA, -side);
                    Outliner.getInstance().showLine("cogwheel_chain_placement_pathing_" + nodeA.pos() + "_" + nodeB.pos() + "_side_" + side,
                                    nodeA.center().add(pathingTangentA),
                                    nodeB.center().add(pathingTangentB))
                            .colored(0x95CD41)
                            .lineWidth(1 / 16f);
                } else if (CogwheelChainPathfinder.isValidPathStep(nodeA, side, nodeB, -side)) {
                    final Vec3 pathingTangentB = CogwheelChainPathfinder.getPathingTangentOnCog(nodeA, nodeB, -side);
                    final Vec3 pathingTangentA = CogwheelChainPathfinder.getPathingTangentOnCog(nodeB, nodeA, -side);
                    Outliner.getInstance().showLine("cogwheel_chain_placement_pathing_" + nodeA.pos() + "_" + nodeB.pos() + "_side_" + side + "_switching",
                                    nodeA.center().add(pathingTangentA),
                                    nodeB.center().add(pathingTangentB))
                            .colored(0x95CD41)
                            .lineWidth(1 / 16f);
                }
            }
        }

        final Vec3 lastPos = currentBuildingChain.getLastNode().center();
        renderParticlesBetween(level, lastPos, projected);

        final BlockPos targetedPos = hit.getBlockPos();
        final BlockState targetedState = level.getBlockState(targetedPos);
        return PlacingCogwheelChain.isValidBlockTarget(targetedState) ? targetedPos : null;
    }

    private static void showBlockOutline(final ClientLevel level, final BlockPos pos) {
        final AtomicInteger counter = new AtomicInteger(0);
        level.getBlockState(pos).getShape(level, pos).forAllEdges((fx, fy, fz, tx, ty, tz) -> {
            Outliner.getInstance().showLine("cogwheel_chain_placement_" + pos + "_outline_" + counter.getAndIncrement(),
                            new Vec3(fx, fy, fz).add(Vec3.atLowerCornerOf(pos)),
                            new Vec3(tx, ty, tz).add(Vec3.atLowerCornerOf(pos)))
                    .colored(0x95CD41)
                    .lineWidth(1 / 16f);
        });
    }

    private static void renderParticlesBetween(final ClientLevel level, final Vec3 from, final Vec3 to) {
        final Vec3 delta = to.subtract(from);
        final double length = delta.length();
        final Vec3 dir = delta.normalize();
        final double step = 0.25;

        for (double t = 0; t <= length; t += step) {
            if (level.getRandom().nextFloat() > PARTICLE_DENSITY) {
                continue;
            }
            final Vec3 lerped = from.add(dir.scale(t));
            level.addParticle(
                    new DustParticleOptions(new Vector3f(0xab / 256f, 0xe6 / 256f, 0x53 / 256f), 1), true,
                    lerped.x, lerped.y, lerped.z, 0, 0, 0);
        }
    }

}
