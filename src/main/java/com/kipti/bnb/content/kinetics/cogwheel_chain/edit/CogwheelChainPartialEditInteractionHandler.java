package com.kipti.bnb.content.kinetics.cogwheel_chain.edit;

import com.kipti.bnb.content.kinetics.cogwheel_chain.attachment.CogwheelChainAttachment;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainCandidate;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PathedCogwheelNode;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelNode;
import com.kipti.bnb.content.kinetics.cogwheel_chain.placement.CogwheelChainPlacementInteraction;
import com.kipti.bnb.content.kinetics.cogwheel_chain.segment.CogwheelChainSegment;
import com.kipti.bnb.content.kinetics.cogwheel_chain.shape.CogwheelChainInteractionHandler;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
import com.kipti.bnb.content.kinetics.cogwheel_chain.world.CogwheelChainWorld;
import com.kipti.bnb.network.packets.from_client.PartialEditCogwheelChainPacket;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Client-side state and entry handling for segment-targeted partial chain edits.
 * <p>
 * Flow:
 * <ol>
 *   <li>Player holds cogwheel, right-clicks a chain segment between two cogs → enters edit mode</li>
 *   <li>Display handler shows preview of proposed insertion as the player looks around</li>
 *   <li>Player right-clicks on a valid block surface → cogwheel is placed AND inserted into the chain</li>
 *   <li>Shift+right-click cancels at any point</li>
 * </ol>
 */
public class CogwheelChainPartialEditInteractionHandler {

    private static @Nullable CogwheelChainPartialEditContext currentEditContext = null;
    private static @Nullable ProposedPlacement proposedPlacement = null;

    /**
     * @return {@code true} if the interaction was handled by this handler
     */
    public static boolean onRightClick() {
        final LocalPlayer player = Minecraft.getInstance().player;
        final ClientLevel level = Minecraft.getInstance().level;
        if (player == null || level == null)
            return false;

        if (CogwheelChainPlacementInteraction.currentBuildingChain != null)
            return false;

        if (currentEditContext != null) {
            if (player.isShiftKeyDown()) {
                clearEditState();
                return true;
            }
            return tryConfirmPlacement(player);
        }

        if (player.isShiftKeyDown())
            return false;

        return tryEnterEditMode(player, level);
    }

    private static boolean tryEnterEditMode(final LocalPlayer player, final ClientLevel level) {
        final CogwheelChainPartialEditContext editContext = resolveSelectedEditContext(player, level);
        if (editContext == null)
            return false;

        currentEditContext = editContext;
        proposedPlacement = null;
        player.displayClientMessage(
                Component.translatable("tooltip.bits_n_bobs.chain_drive_partial_edit_hint"),
                true
        );
        return true;
    }

    private static boolean tryConfirmPlacement(final LocalPlayer player) {
        final ProposedPlacement placement = proposedPlacement;
        if (placement == null)
            return true;

        final CogwheelChainPartialEditContext editContext = currentEditContext;
        if (editContext == null)
            return true;

        final InteractionHand hand = resolveHeldCogwheelHand(player, editContext.chainType());
        if (hand == null)
            return true;

        CatnipServices.NETWORK.sendToServer(new PartialEditCogwheelChainPacket(
                editContext.controllerPos(),
                placement.pos(),
                placement.hitDirection(),
                hand.ordinal(),
                editContext.chainPosition(),
                editContext.startNodeIndex(),
                editContext.endNodeIndex(),
                editContext.startNode(),
                editContext.endNode(),
                editContext.chainType(),
                BuiltInRegistries.ITEM.getHolder(BuiltInRegistries.ITEM.getKey(editContext.chainItemType())).orElseThrow()
        ));
        clearEditState();
        return true;
    }

    private static @Nullable InteractionHand resolveHeldCogwheelHand(final LocalPlayer player,
                                                                     final CogwheelChainType chainType) {
        if (CogwheelChainPlacementInteraction.isCompatibleCogwheelItem(player.getMainHandItem(), chainType))
            return InteractionHand.MAIN_HAND;
        if (CogwheelChainPlacementInteraction.isCompatibleCogwheelItem(player.getOffhandItem(), chainType))
            return InteractionHand.OFF_HAND;
        return null;
    }

    private static @Nullable CogwheelChainPartialEditContext resolveSelectedEditContext(final LocalPlayer player,
                                                                                        final ClientLevel level) {
        final BlockPos controllerPos = CogwheelChainInteractionHandler.selectedController;
        if (controllerPos == null || !level.isLoaded(controllerPos))
            return null;

        final CogwheelChainWorld chainWorld = CogwheelChainWorld.get(level);
        final CogwheelChain chain = chainWorld.getChain(controllerPos);
        if (chain == null)
            return null;

        if (CogwheelChainPlacementInteraction.getCompatibleCogwheelItemInHand(player, chain.getChainType()) == null)
            return null;

        final float chainPosition = CogwheelChainInteractionHandler.selectedChainPosition;
        final CogwheelChainSegment selectedSegment = resolveSelectedSegment(level, controllerPos, chainPosition);
        if (selectedSegment == null || selectedSegment.type() != CogwheelChainSegment.SegmentType.BETWEEN_NODES)
            return null;

        final SelectedSegmentNodes segmentNodes = resolveSelectedSegmentNodes(chain, controllerPos, selectedSegment);
        if (segmentNodes == null)
            return null;

        return new CogwheelChainPartialEditContext(
                controllerPos,
                chainPosition,
                selectedSegment,
                segmentNodes.startNodeIndex(),
                segmentNodes.endNodeIndex(),
                segmentNodes.startNode(),
                segmentNodes.endNode(),
                chain.getChainType(),
                chain.getReturnedItem()
        );
    }

    private static @Nullable CogwheelChainSegment resolveSelectedSegment(final ClientLevel level,
                                                                         final BlockPos controllerPos,
                                                                         final float chainPosition) {
        final CogwheelChainAttachment attachment = new CogwheelChainAttachment(controllerPos, chainPosition);
        return attachment.getCurrentCogwheelChainSegment(level);
    }

    private static @Nullable SelectedSegmentNodes resolveSelectedSegmentNodes(final CogwheelChain chain,
                                                                              final BlockPos controllerPos,
                                                                              final CogwheelChainSegment selectedSegment) {
        final List<PathedCogwheelNode> existingNodes = chain.getChainPathCogwheelNodes();
        if (existingNodes.size() < 2)
            return null;

        int betweenNodesIndex = 0;
        for (final CogwheelChainSegment segment : chain.getSegments()) {
            if (segment.type() != CogwheelChainSegment.SegmentType.BETWEEN_NODES)
                continue;

            if (!segment.equals(selectedSegment)) {
                betweenNodesIndex++;
                continue;
            }

            final int startNodeIndex = betweenNodesIndex;
            final int endNodeIndex = (betweenNodesIndex + 1) % existingNodes.size();
            return new SelectedSegmentNodes(
                    startNodeIndex,
                    endNodeIndex,
                    CogwheelChainPartialEditInsertionPlanner.toWorldNode(
                            existingNodes.get(startNodeIndex),
                            controllerPos
                    ),
                    CogwheelChainPartialEditInsertionPlanner.toWorldNode(existingNodes.get(endNodeIndex), controllerPos)
            );
        }

        return null;
    }

    public static void clearEditState() {
        currentEditContext = null;
        proposedPlacement = null;
    }

    public static boolean hasActiveEditContext() {
        return currentEditContext != null;
    }

    public static @Nullable CogwheelChainPartialEditContext getCurrentEditContext() {
        return currentEditContext;
    }

    public static @Nullable BlockPos getEditingControllerPos() {
        if (currentEditContext == null)
            return null;
        return currentEditContext.controllerPos();
    }

    public static @Nullable ProposedPlacement getProposedPlacement() {
        return proposedPlacement;
    }

    public static void setProposedPlacement(final @Nullable ProposedPlacement placement) {
        proposedPlacement = placement;
    }

    public static @Nullable CogwheelChainType getEditingChainType() {
        if (currentEditContext == null)
            return null;
        return currentEditContext.chainType();
    }

    public static @Nullable Item getEditingChainItemType() {
        if (currentEditContext == null)
            return null;
        return currentEditContext.chainItemType();
    }

    public record ProposedPlacement(BlockPos pos, CogwheelChainCandidate candidate, Direction hitDirection,
                                    BlockState placementState) {

        public ProposedPlacement {
            pos = pos.immutable();
        }
    }

    private record SelectedSegmentNodes(int startNodeIndex, int endNodeIndex, PlacingCogwheelNode startNode,
                                        PlacingCogwheelNode endNode) {
    }
}
