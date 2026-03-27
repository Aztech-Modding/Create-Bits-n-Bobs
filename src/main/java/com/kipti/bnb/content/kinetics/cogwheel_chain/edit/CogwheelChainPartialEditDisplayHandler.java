package com.kipti.bnb.content.kinetics.cogwheel_chain.edit;

import com.kipti.bnb.content.kinetics.cogwheel_chain.edit.CogwheelChainPartialEditInteractionHandler.ProposedPlacement;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainCandidate;
import com.kipti.bnb.content.kinetics.cogwheel_chain.placement.ChainDriveDisplayRenderer;
import com.kipti.bnb.content.kinetics.cogwheel_chain.placement.ChainInteractionFailedException;
import com.kipti.bnb.content.kinetics.cogwheel_chain.placement.ChainPlacementPathDisplayHelper;
import com.kipti.bnb.content.kinetics.cogwheel_chain.placement.CogwheelChainPlacementInteraction;
import com.kipti.bnb.content.kinetics.cogwheel_chain.world.CogwheelChainWorld;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.Nullable;

import static com.kipti.bnb.content.kinetics.cogwheel_chain.placement.ChainDriveDisplayRenderer.*;

/**
 * Client-side display handler for cogwheel chain partial edit previews.
 * <p>
 * Renders either outliner (shape + connections) for valid insertions,
 * or red particles for invalid ones. Only one mode is ever active.
 */
@EventBusSubscriber(Dist.CLIENT)
public class CogwheelChainPartialEditDisplayHandler {

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

        CogwheelChainPartialEditInsertionPlan insertionPlan = null;
        ChainInteractionFailedException validationFailure = null;
        try {
            insertionPlan = CogwheelChainPartialEditInsertionPlanner.planWithCandidate(
                    existingChain, editContext, placement.pos(), placement.candidate()
            );
        } catch (final ChainInteractionFailedException e) {
            validationFailure = e;
        }

        if (insertionPlan != null) {
            renderValidPlacement(level, placement, insertionPlan);
            renderCostOverlay(player, editContext, insertionPlan);
            return;
        }

        if (validationFailure != null) {
            player.displayClientMessage(validationFailure.getComponent(), true);
        }
        renderInvalidPlacement(level, editContext, placement);
    }

    private static void renderValidPlacement(final ClientLevel level,
                                              final ProposedPlacement placement,
                                              final CogwheelChainPartialEditInsertionPlan insertionPlan) {
        ChainDriveDisplayRenderer.renderBlockOutline(level, placement.pos(), placement.placementState(), VALID_COLOUR, "partial_edit_preview");

        final int[] displaySides = ChainPlacementPathDisplayHelper.getPathDisplaySides(insertionPlan.rebuiltChain());
        ChainDriveDisplayRenderer.renderConnectionSegment(
                "partial_edit_segment_from",
                ChainPlacementPathDisplayHelper.getDisplayedSegment(
                        insertionPlan.rebuiltChain(),
                        insertionPlan.insertionIndex() - 1,
                        displaySides
                ),
                VALID_COLOUR
        );
        ChainDriveDisplayRenderer.renderConnectionSegment(
                "partial_edit_segment_to",
                ChainPlacementPathDisplayHelper.getDisplayedSegment(
                        insertionPlan.rebuiltChain(),
                        insertionPlan.insertionIndex(),
                        displaySides
                ),
                VALID_COLOUR
        );
    }

    private static void renderInvalidPlacement(final ClientLevel level,
                                                final CogwheelChainPartialEditContext editContext,
                                                final ProposedPlacement placement) {
        ChainDriveDisplayRenderer.renderBlockOutline(level, placement.pos(), placement.placementState(), INVALID_COLOUR, "partial_edit_preview");

        final Vec3 startCenter = editContext.startNode().center();
        final Vec3 proposedCenter = placement.pos().getCenter();
        final Vec3 endCenter = editContext.endNode().center();
        ChainDriveDisplayRenderer.renderParticlesBetween(level, startCenter, proposedCenter, INVALID_COLOUR);
        ChainDriveDisplayRenderer.renderParticlesBetween(level, proposedCenter, endCenter, INVALID_COLOUR);
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

        final BlockPlaceContext placeContext = new BlockPlaceContext(
                new UseOnContext(level, player, InteractionHand.MAIN_HAND, heldCogwheel,
                        new BlockHitResult(Vec3.atCenterOf(placementPos), blockHit.getDirection(), placementPos, false)));
        final BlockState placementState = cogwheelBlock.getStateForPlacement(placeContext);
        if (placementState == null) return null;

        return new ProposedPlacement(placementPos, candidate, blockHit.getDirection(), placementState);
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

}
