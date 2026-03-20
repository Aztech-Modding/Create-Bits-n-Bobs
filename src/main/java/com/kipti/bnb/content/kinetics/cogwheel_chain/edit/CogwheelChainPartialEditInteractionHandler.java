package com.kipti.bnb.content.kinetics.cogwheel_chain.edit;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.*;
import com.kipti.bnb.content.kinetics.cogwheel_chain.placement.CogwheelChainPlacementInteraction;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
import com.kipti.bnb.content.kinetics.cogwheel_chain.world.CogwheelChainWorld;
import com.kipti.bnb.network.packets.from_client.PartialEditCogwheelChainPacket;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Client-side handler for incrementally adding a cogwheel to an existing chain.
 * <p>
 * When a player holds a valid chain item and right-clicks on a block near an
 * existing chain, this handler enters partial edit mode. The player can then
 * confirm placement of a new cogwheel, which sends a
 * {@link PartialEditCogwheelChainPacket} to the server to rebuild the chain
 * with the new cogwheel included.
 */
//@EventBusSubscriber(Dist.CLIENT)
public class CogwheelChainPartialEditInteractionHandler {

    private static @Nullable BlockPos editingControllerPos = null;
    private static @Nullable BlockPos proposedCogwheelPos = null;
    private static @Nullable CogwheelChainType editingChainType = null;
    private static @Nullable Item editingChainItemType = null;

    //TODO: fix cognitive decline from claude
//    @SubscribeEvent(priority = EventPriority.HIGH)
//    public static void onClickInput(final InputEvent.InteractionKeyMappingTriggered event) {
//        final Minecraft mc = Minecraft.getInstance();
//        if (mc.screen != null)
//            return;
//
//        final KeyMapping key = event.getKeyMapping();
//        if (key == mc.options.keyUse && onRightClick()) {
//            event.setCanceled(true);
//        }
//    }

    /**
     * @return {@code true} if the interaction was handled by this handler
     */
    private static boolean onRightClick() {
        final LocalPlayer player = Minecraft.getInstance().player;
        final ClientLevel level = Minecraft.getInstance().level;
        if (!isValidEditState(player, level))
            return false;

        if (player.isShiftKeyDown()) {
            clearEditState();
            return false;
        }

        final ItemStack chainItemInHand = CogwheelChainPlacementInteraction.getChainItemInHand(player);
        final CogwheelChainType heldChainType = getHeldChainType(chainItemInHand);
        if (heldChainType == null)
            return false;

        final BlockHitResult blockHit = getTargetedBlock();
        if (blockHit == null)
            return false;

        final BlockPos hitPos = blockHit.getBlockPos();
        final BlockState targetedState = level.getBlockState(hitPos);
        if (!isValidCogwheelTarget(targetedState, heldChainType))
            return false;

        return handleChainEdit(level, hitPos, targetedState, heldChainType, chainItemInHand, player);
    }

    private static boolean isValidEditState(final LocalPlayer player, final ClientLevel level) {
        return player != null && level != null
                && CogwheelChainPlacementInteraction.currentBuildingChain == null;
    }

    private static @Nullable CogwheelChainType getHeldChainType(final ItemStack chainItemInHand) {
        if (chainItemInHand == null)
            return null;
        return CogwheelChainType.COGWHEEL_TYPE_BY_ITEM.get(chainItemInHand.getItem());
    }

    private static @Nullable BlockHitResult getTargetedBlock() {
        final HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK)
            return null;
        return (BlockHitResult) hitResult;
    }

    private static boolean isValidCogwheelTarget(final BlockState state, final CogwheelChainType chainType) {
        return CogwheelChainCandidate.isValidCandidate(state)
                && chainType.getCogwheelPredicate().test(state.getBlock());
    }

    private static boolean handleChainEdit(final ClientLevel level,
                                           final BlockPos hitPos,
                                           final BlockState targetedState,
                                           final CogwheelChainType heldChainType,
                                           final ItemStack chainItemInHand,
                                           final LocalPlayer player) {
        if (isAlreadyPartOfAnyChain(level, hitPos))
            return false;

        final BlockPos nearbyController = findNearbyChainController(level, hitPos, heldChainType, targetedState);
        if (nearbyController == null)
            return false;

        if (editingControllerPos != null && editingControllerPos.equals(nearbyController)
                && proposedCogwheelPos != null && proposedCogwheelPos.equals(hitPos)) {
            confirmPlacement(player, chainItemInHand);
            return true;
        }

        editingControllerPos = nearbyController;
        proposedCogwheelPos = hitPos;
        editingChainType = heldChainType;
        editingChainItemType = chainItemInHand.getItem();

        player.displayClientMessage(
                Component.translatable("tooltip.bits_n_bobs.chain_drive_partial_edit_hint"), true
        );
        return true;
    }

    private static boolean isAlreadyPartOfAnyChain(final ClientLevel level, final BlockPos pos) {
        final CogwheelChainWorld chainWorld = CogwheelChainWorld.get(level);
        return !chainWorld.getChainDrivesAtPosition(pos).isEmpty();
    }

    private static @Nullable BlockPos findNearbyChainController(final ClientLevel level,
                                                                final BlockPos newCogwheelPos,
                                                                final CogwheelChainType chainType,
                                                                final BlockState newBlockState) {
        final CogwheelChainCandidate candidate = CogwheelChainCandidate.getForBlock(newBlockState);
        if (candidate == null)
            return null;

        final PlacingCogwheelNode proposedNode = new PlacingCogwheelNode(
                newCogwheelPos,
                candidate.axis(),
                candidate.isLarge(),
                candidate.hasSmallCogwheelOffset()
        );

        final CogwheelChainWorld chainWorld = CogwheelChainWorld.get(level);

        BlockPos bestController = null;
        double bestDistance = Double.MAX_VALUE;

        for (final BlockPos controllerPos : chainWorld.getControllerPositions()) {
            if (!isMatchingChainController(chainWorld, controllerPos, chainType, proposedNode))
                continue;

            final double distance = controllerPos.getCenter().distanceTo(newCogwheelPos.getCenter());
            if (distance < bestDistance) {
                bestDistance = distance;
                bestController = controllerPos;
            }
        }

        return bestController;
    }

    private static boolean isMatchingChainController(final CogwheelChainWorld chainWorld,
                                                     final BlockPos controllerPos,
                                                     final CogwheelChainType chainType,
                                                     final PlacingCogwheelNode proposedNode) {
        final CogwheelChain chain = chainWorld.getChain(controllerPos);
        if (chain == null)
            return false;

        if (chain.getChainType() != chainType)
            return false;

        return canInsertIntoChain(chain, controllerPos, proposedNode);
    }

    private static boolean canInsertIntoChain(final CogwheelChain chain,
                                              final BlockPos controllerPos,
                                              final PlacingCogwheelNode proposedNode) {
        final List<PathedCogwheelNode> existingNodes = chain.getChainPathCogwheelNodes();

        for (int i = 0; i < existingNodes.size(); i++) {
            final PathedCogwheelNode pathedA = existingNodes.get(i);
            final PathedCogwheelNode pathedB = existingNodes.get((i + 1) % existingNodes.size());

            final PlacingCogwheelNode nodeA = toWorldNode(pathedA, controllerPos);
            final PlacingCogwheelNode nodeB = toWorldNode(pathedB, controllerPos);

            final boolean connectsFromA = !CogwheelChainPathfinder.getValidPathSteps(nodeA, proposedNode).isEmpty();
            final boolean connectsToB = !CogwheelChainPathfinder.getValidPathSteps(proposedNode, nodeB).isEmpty();

            if (connectsFromA && connectsToB)
                return true;
        }

        return false;
    }

    private static PlacingCogwheelNode toWorldNode(final PathedCogwheelNode pathed, final BlockPos controllerPos) {
        return new PlacingCogwheelNode(
                controllerPos.offset(pathed.localPos()),
                pathed.rotationAxis(),
                pathed.isLarge(),
                pathed.hasSmallCogwheelOffset()
        );
    }

    private static void confirmPlacement(final LocalPlayer player, final ItemStack chainItemInHand) {
        if (editingControllerPos == null || proposedCogwheelPos == null
                || editingChainType == null || editingChainItemType == null)
            return;

        CatnipServices.NETWORK.sendToServer(new PartialEditCogwheelChainPacket(
                editingControllerPos,
                proposedCogwheelPos,
                editingChainType,
                BuiltInRegistries.ITEM.getHolder(BuiltInRegistries.ITEM.getKey(chainItemInHand.getItem())).orElseThrow()
        ));

        clearEditState();
    }

    public static void clearEditState() {
        editingControllerPos = null;
        proposedCogwheelPos = null;
        editingChainType = null;
        editingChainItemType = null;
    }

    public static @Nullable BlockPos getEditingControllerPos() {
        return editingControllerPos;
    }

    public static @Nullable BlockPos getProposedCogwheelPos() {
        return proposedCogwheelPos;
    }

    public static @Nullable CogwheelChainType getEditingChainType() {
        return editingChainType;
    }

    public static @Nullable Item getEditingChainItemType() {
        return editingChainItemType;
    }
}
