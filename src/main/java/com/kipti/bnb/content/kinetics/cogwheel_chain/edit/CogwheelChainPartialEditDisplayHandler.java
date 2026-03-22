package com.kipti.bnb.content.kinetics.cogwheel_chain.edit;

import com.kipti.bnb.content.kinetics.cogwheel_chain.edit.CogwheelChainPartialEditInteractionHandler.ProposedPlacement;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainCandidate;
import com.kipti.bnb.content.kinetics.cogwheel_chain.placement.ChainPlacementPathDisplayHelper;
import com.kipti.bnb.content.kinetics.cogwheel_chain.placement.CogwheelChainPlacementInteraction;
import com.kipti.bnb.content.kinetics.cogwheel_chain.world.CogwheelChainWorld;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * pepee poopoo
 */
@EventBusSubscriber(Dist.CLIENT)
public class CogwheelChainPartialEditDisplayHandler {

    private static final float PARTICLE_DENSITY = 0.1f;
    private static final int VALID_COLOUR = 0x95CD41;
    private static final int INVALID_COLOUR = 0xFF5D5;

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event) {
        final Minecraft mc = Minecraft.getInstance();
        tick(mc.player);
    }

    private static void tick(final LocalPlayer player) {
        final CogwheelChainPartialEditContext editContext = CogwheelChainPartialEditInteractionHandler.getCurrentEditContext();
        if (editContext == null) {
            return;
        }

        final ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        if (CogwheelChainPlacementInteraction.getCompatibleCogwheelItemInHand(
                player,
                editContext.chainType()
        ) == null) {
            CogwheelChainPartialEditInteractionHandler.clearEditState();
            return;
        }

        final CogwheelChainWorld chainWorld = CogwheelChainWorld.get(level);
        final CogwheelChain existingChain = chainWorld.getChain(editContext.controllerPos());
        if (existingChain == null) {
            CogwheelChainPartialEditInteractionHandler.clearEditState();
            return;
        }

        final ProposedPlacement placement = resolveProposedPlacement(player, level, editContext);
        CogwheelChainPartialEditInteractionHandler.setProposedPlacement(placement);
        if (placement == null) {
            return;
        }

        final CogwheelChainPartialEditInsertionPlan insertionPlan = CogwheelChainPartialEditInsertionPlanner.planWithCandidate(
                existingChain,
                editContext,
                placement.pos(),
                placement.candidate()
        );
        renderProposedCogwheelOutline(placement.pos(), insertionPlan != null ? VALID_COLOUR : INVALID_COLOUR);
        if (insertionPlan != null) {
            renderValidConnectionSegments(insertionPlan);
            renderCostOverlay(player, editContext, insertionPlan);
            return;
        }

        renderInvalidConnectionSegments(level, editContext, placement.pos());
    }

    private static @Nullable ProposedPlacement resolveProposedPlacement(final LocalPlayer player,
                                                                        final ClientLevel level,
                                                                        final CogwheelChainPartialEditContext editContext) {
        final HitResult genericHit = Minecraft.getInstance().hitResult;
        if (!(genericHit instanceof final BlockHitResult blockHit) || blockHit.getType() == HitResult.Type.MISS)
            return null;

        final ItemStack heldCogwheel = CogwheelChainPlacementInteraction.getCompatibleCogwheelItemInHand(
                player,
                editContext.chainType()
        );
        if (heldCogwheel == null || !(heldCogwheel.getItem() instanceof final BlockItem blockItem))
            return null;

        final BlockPos hitPos = blockHit.getBlockPos();
        final BlockState hitState = level.getBlockState(hitPos);
        final BlockPos placementPos;
        if (hitState.canBeReplaced()) {
            placementPos = hitPos;
        } else {
            placementPos = hitPos.relative(blockHit.getDirection());
            if (!level.getBlockState(placementPos).canBeReplaced())
                return null;
        }

        final Block cogwheelBlock = blockItem.getBlock();
        final CogwheelChainCandidate baseCandidate = CogwheelChainCandidate.getForBlock(cogwheelBlock);
        if (baseCandidate == null)
            return null;

        final Direction.Axis axis = blockHit.getDirection().getAxis();
        final CogwheelChainCandidate candidate = new CogwheelChainCandidate(
                axis, baseCandidate.isLarge(), baseCandidate.hasSmallCogwheelOffset());

        return new ProposedPlacement(placementPos, candidate, blockHit.getDirection());
    }

    private static void renderProposedCogwheelOutline(final BlockPos pos, final int colour) {
        Outliner.getInstance().showAABB(
                        "partial_edit_outline",
                        new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)
                )
                .colored(colour)
                .lineWidth(1 / 16f);
    }

    private static void renderValidConnectionSegments(final CogwheelChainPartialEditInsertionPlan insertionPlan) {
        final int[] displaySides = ChainPlacementPathDisplayHelper.getPathDisplaySides(insertionPlan.rebuiltChain());
        renderConnectionSegment(
                "partial_edit_segment_from",
                ChainPlacementPathDisplayHelper.getDisplayedSegment(
                        insertionPlan.rebuiltChain(),
                        insertionPlan.insertionIndex() - 1,
                        displaySides
                ),
                VALID_COLOUR
        );
        renderConnectionSegment(
                "partial_edit_segment_to",
                ChainPlacementPathDisplayHelper.getDisplayedSegment(
                        insertionPlan.rebuiltChain(),
                        insertionPlan.insertionIndex(),
                        displaySides
                ),
                VALID_COLOUR
        );
    }

    private static void renderConnectionSegment(final String key,
                                                final ChainPlacementPathDisplayHelper.DisplayedSegment segment,
                                                final int colour) {
        Outliner.getInstance().showLine(key, segment.from(), segment.to())
                .colored(colour)
                .lineWidth(2f / 16f);
    }

    private static void renderInvalidConnectionSegments(final ClientLevel level,
                                                        final CogwheelChainPartialEditContext editContext,
                                                        final BlockPos proposedPos) {
        final Vec3 startCenter = editContext.startNode().center();
        final Vec3 proposedCenter = proposedPos.getCenter();
        final Vec3 endCenter = editContext.endNode().center();
        renderConnectionSegment(
                "partial_edit_segment_from",
                new ChainPlacementPathDisplayHelper.DisplayedSegment(startCenter, proposedCenter),
                INVALID_COLOUR
        );
        renderConnectionSegment(
                "partial_edit_segment_to",
                new ChainPlacementPathDisplayHelper.DisplayedSegment(proposedCenter, endCenter),
                INVALID_COLOUR
        );
        renderParticlesBetween(level, startCenter, proposedCenter);
        renderParticlesBetween(level, proposedCenter, endCenter);
    }

    private static void renderCostOverlay(final LocalPlayer player,
                                          final CogwheelChainPartialEditContext editContext,
                                          final CogwheelChainPartialEditInsertionPlan insertionPlan) {
        if (player.hasInfiniteMaterials() || insertionPlan.costDelta() == 0) {
            return;
        }

        final boolean hasEnough = insertionPlan.costDelta() < 0 || ChainConveyorBlockEntity.getChainsFromInventory(
                player,
                editContext.chainItemType().getDefaultInstance(),
                insertionPlan.costDelta(),
                true
        );
        BlueprintOverlayRenderer.displayChainRequirements(
                editContext.chainItemType(),
                insertionPlan.costDelta(),
                hasEnough
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
                    new DustParticleOptions(
                            new Vector3f(1.0f, 0x5D / 255f, 0x5D / 255f),
                            1
                    ),
                    true,
                    lerped.x, lerped.y, lerped.z, 0, 0, 0
            );
        }
    }

}