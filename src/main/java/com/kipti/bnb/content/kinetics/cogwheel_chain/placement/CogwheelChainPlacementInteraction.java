package com.kipti.bnb.content.kinetics.cogwheel_chain.placement;

import com.kipti.bnb.content.kinetics.cogwheel_chain.edit.CogwheelChainPartialEditInteractionHandler;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainCandidate;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.shape.CogwheelChainInteractionHandler;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
import com.kipti.bnb.network.packets.from_client.PlaceCogwheelChainPacket;
import com.kipti.bnb.network.packets.from_client.WrenchCogwheelChainPacket;
import com.kipti.bnb.registry.core.BnbFeatureFlag;
import com.simibubi.create.AllItems;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Client-side interaction entry point for cogwheel-chain placement, removal, and partial edit entry.
 */
@EventBusSubscriber(Dist.CLIENT)
public class CogwheelChainPlacementInteraction {

    public static @Nullable PlacingCogwheelChain currentBuildingChain = null;
    public static @Nullable ResourceKey<Level> currentChainLevel = null;
    public static @Nullable CogwheelChainType currentChainType = null;
    public static @Nullable Item currentChainItemType = null;

    @SubscribeEvent
    public static void onClickInput(final InputEvent.InteractionKeyMappingTriggered event) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null)
            return;

        final KeyMapping key = event.getKeyMapping();

        if (key == mc.options.keyUse && onRightClick(event)) {
            event.setCanceled(true);
        }
    }

    private static boolean onRightClick(final InputEvent.InteractionKeyMappingTriggered event) {
        final LocalPlayer player = Minecraft.getInstance().player;
        final ClientLevel level = Minecraft.getInstance().level;
        if (player == null || level == null)
            return false;

        if (tryHandleSelectedChainInteraction(player)) {
            return true;
        }

        final ItemStack chainItemInHand = getChainItemInHand(player);

        if (chainItemInHand == null) {
            return false;
        }

        final CogwheelChainType heldChainType = CogwheelChainType.COGWHEEL_TYPE_BY_ITEM.get(chainItemInHand.getItem());
        if (heldChainType == null) {
            return false;
        }

        if (player.isShiftKeyDown() || (currentChainType != null && currentChainType != heldChainType)) {
            if (currentBuildingChain != null) {
                clearPlacingChain();
            }
            return true;
        }

        final HitResult hitResult = Minecraft.getInstance().hitResult;

        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            return currentBuildingChain != null;
        }

        final BlockHitResult bhr = (BlockHitResult) hitResult;
        final BlockPos hitPos = bhr.getBlockPos();
        final BlockState targetedState = level.getBlockState(hitPos);

        final CogwheelChainCandidate targetedCandidate = CogwheelChainCandidate.getForBlock(targetedState);

        if (targetedCandidate == null) {
            return currentBuildingChain != null;
        }

        if (!BnbFeatureFlag.COGWHEEL_CHAIN_DRIVES.get()) {
            player.displayClientMessage(new ChainInteractionFailedException("config_forbids").getComponent(), true);
            return true;
        }

        if (!heldChainType.getCogwheelPredicate().test(targetedState.getBlock())) {
            player.displayClientMessage(new ChainInteractionFailedException("invalid_cogwheel_type." + heldChainType.getTranslationKey()).getComponent(), true);
            return true;
        }

        rightClickForChain(event, level, hitPos, targetedState, targetedCandidate, heldChainType, chainItemInHand, player);
        return true;
    }

    private static boolean tryHandleSelectedChainInteraction(final LocalPlayer player) {
        if (tryDestroyChainWithWrench(player)) {
            return true;
        }

        if (CogwheelChainPartialEditInteractionHandler.onRightClick()) {
            return true;
        }

        return CogwheelChainInteractionHandler.onUse();
    }

    private static void rightClickForChain(final InputEvent.InteractionKeyMappingTriggered event,
                                           final ClientLevel level,
                                           final BlockPos hitPos,
                                           final BlockState targetedState,
                                           final CogwheelChainCandidate targetedCandidate,
                                           final CogwheelChainType heldChainType,
                                           final ItemStack chainItemInHand,
                                           final LocalPlayer player) {
        if (currentBuildingChain == null || currentChainLevel == null || !currentChainLevel.equals(level.dimension())) {
            CogwheelChainPartialEditInteractionHandler.clearEditState();
            currentBuildingChain = new PlacingCogwheelChain(hitPos, targetedCandidate.axis(), targetedCandidate.isLarge(), targetedCandidate.hasSmallCogwheelOffset());
            currentChainLevel = level.dimension();
            currentChainType = heldChainType;
            currentChainItemType = chainItemInHand.getItem();

            player.displayClientMessage(Component.translatable("tooltip.bits_n_bobs.chain_drive_placing_hint"), true);
        } else {
            if (currentBuildingChain.getLastNode().pos().equals(hitPos)) {
                currentBuildingChain.getNodes().removeLast();

                if (currentBuildingChain.getNodes().isEmpty()) {
                    clearPlacingChain();
                }
                return;
            }

            try {
                final boolean added = currentBuildingChain.tryAddNode(hitPos, targetedState, currentChainType);

                if (!added) {
                    return;
                }

                final boolean completed;
                try {
                    completed = currentBuildingChain.canBuildChainIfLooping();
                } catch (final ChainInteractionFailedException exception) {
                    player.displayClientMessage(exception.getComponent(), true);
                    clearPlacingChain();
                    return;
                }

                if (completed) {
                    CatnipServices.NETWORK.sendToServer(new PlaceCogwheelChainPacket(
                            currentBuildingChain,
                            currentChainType,
                            event.getHand().ordinal(),
                            BuiltInRegistries.ITEM.getHolder(BuiltInRegistries.ITEM.getKey(chainItemInHand.getItem())).orElseThrow()
                    ));
                    clearPlacingChain();
                }
            } catch (final ChainInteractionFailedException exception) {
                player.displayClientMessage(exception.getComponent(), true);
            }
        }
    }

    private static boolean tryDestroyChainWithWrench(final LocalPlayer player) {
        final ItemStack main = player.getMainHandItem();
        final ItemStack off = player.getOffhandItem();

        if (!player.isShiftKeyDown())
            return false;

        if (!AllItems.WRENCH.isIn(main) && !AllItems.WRENCH.isIn(off))
            return false;

        if (CogwheelChainInteractionHandler.selectedController == null)
            return false;

        final BlockPos controllerPos = CogwheelChainInteractionHandler.selectedController;
        final float chainPosition = CogwheelChainInteractionHandler.selectedChainPosition;

        final ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !level.isLoaded(controllerPos))
            return false;

        CatnipServices.NETWORK.sendToServer(new WrenchCogwheelChainPacket(controllerPos, chainPosition));
        CogwheelChainPartialEditInteractionHandler.clearEditState();
        return true;
    }

    public static @Nullable ItemStack getChainItemInHand(final LocalPlayer player) {
        return isChainDriveItem(player.getMainHandItem()) ? player.getMainHandItem() :
                isChainDriveItem(player.getOffhandItem()) ? player.getOffhandItem() : null;
    }

    public static @Nullable ItemStack getCompatibleCogwheelItemInHand(final LocalPlayer player, final CogwheelChainType chainType) {
        return isCompatibleCogwheelItem(player.getMainHandItem(), chainType) ? player.getMainHandItem() :
                isCompatibleCogwheelItem(player.getOffhandItem(), chainType) ? player.getOffhandItem() : null;
    }

    public static void clearPlacingChain() {
        currentBuildingChain = null;
        currentChainLevel = null;
        currentChainType = null;
        currentChainItemType = null;
    }

    public static boolean isChainDriveItem(final ItemStack stack) {
        return CogwheelChainType.COGWHEEL_TYPE_BY_ITEM.get(stack.getItem()) != null;
    }

    public static boolean isCompatibleCogwheelItem(final ItemStack stack) {
        if (!(stack.getItem() instanceof final BlockItem blockItem))
            return false;
        return CogwheelChainCandidate.getForBlock(blockItem.getBlock()) != null;
    }

    public static boolean isCompatibleCogwheelItem(final ItemStack stack, final CogwheelChainType chainType) {
        if (!(stack.getItem() instanceof final BlockItem blockItem))
            return false;
        return chainType.getCogwheelPredicate().test(blockItem.getBlock())
                && CogwheelChainCandidate.getForBlock(blockItem.getBlock()) != null;
    }



}

