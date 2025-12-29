package com.kipti.bnb.content.kinetics.cogwheel_chain.placement;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.ChainInteractionFailedException;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
import com.kipti.bnb.network.packets.from_client.PlaceCogwheelChainPacket;
import com.kipti.bnb.registry.BnbFeatureFlag;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
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

        //If it is a chain targeting a cogwheel
        final ItemStack chainItemInHand = getChainItemInHand(player);

        if (chainItemInHand == null) {
            return false;
        }

        final CogwheelChainType heldChainType = CogwheelChainType.COGWHEEL_TYPE_BY_ITEM.get(chainItemInHand.getItem());
        if (heldChainType == null) { //Shouldn't be null, but what the hell
            return false;
        }

        //If crouching, or a mismatched type, try clear
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

        final boolean validBlockTarget = PlacingCogwheelChain.isValidBlockTarget(targetedState);

        if (validBlockTarget && !BnbFeatureFlag.COGWHEEL_CHAIN_DRIVES.get()) {
            player.displayClientMessage(new ChainInteractionFailedException("config_forbids").getComponent(), true);
            return true;
        }
        if (validBlockTarget && !heldChainType.getCogwheelPredicate().test(targetedState.getBlock())) {
            player.displayClientMessage(new ChainInteractionFailedException("invalid_cogwheel_type." + heldChainType.getTranslationKey()).getComponent(), true);
            return true;
        }

        if (!validBlockTarget) {
            event.setSwingHand(false);
            return currentBuildingChain != null;
        }

        if (currentBuildingChain == null || currentChainLevel == null || !currentChainLevel.equals(level.dimension())) {
            //Start a new chain
            currentBuildingChain = new PlacingCogwheelChain(hitPos, targetedState.getValue(CogWheelBlock.AXIS), PlacingCogwheelChain.isLargeBlockTarget(targetedState), PlacingCogwheelChain.hasSmallCogwheelOffset(targetedState));
            currentChainLevel = level.dimension();
            currentChainType = heldChainType;
            currentChainItemType = chainItemInHand.getItem();

            player.displayClientMessage(Component.translatable("tooltip.bits_n_bobs.chain_drive_placing_hint"), true);
        } else {
            //if this is the last node, then remove the last one
            if (currentBuildingChain.getLastNode().pos().equals(hitPos)) {
                currentBuildingChain.getNodes().removeLast();

                //If no nodes left, clear chain
                if (currentBuildingChain.getNodes().isEmpty()) {
                    clearPlacingChain();
                }
                return true;
            }

            //Try to add to existing chain
            try {
                final boolean added = currentBuildingChain.tryAddNode(hitPos, targetedState, currentChainType);

                if (!added) { //Only happens with invalid target, ignore quietly
                    return true;
                }

                final boolean completed;
                try {
                    completed = currentBuildingChain.canBuildChainIfLooping();
                } catch (final ChainInteractionFailedException exception) {
                    player.displayClientMessage(exception.getComponent(), true);
                    clearPlacingChain();
                    return true;
                }

                if (completed) {
                    //If completed, send to server, clear current chain
                    CatnipServices.NETWORK.sendToServer(new PlaceCogwheelChainPacket(
                            currentBuildingChain,
                            currentChainType,
                            event.getHand().ordinal(),
                            BuiltInRegistries.ITEM.getHolder(BuiltInRegistries.ITEM.getKey(chainItemInHand.getItem())).orElseThrow()
                    ));
                    clearPlacingChain();
                }
            } catch (final ChainInteractionFailedException exception) {
                //Send message on fail
                player.displayClientMessage(exception.getComponent(), true);
            }
        }
        return true;
    }

    public static @Nullable ItemStack getChainItemInHand(final LocalPlayer player) {
        return isChainDriveItem(player.getMainHandItem()) ? player.getMainHandItem() :
                isChainDriveItem(player.getOffhandItem()) ? player.getOffhandItem() : null;
    }

    public static void clearPlacingChain() {
        currentBuildingChain = null;
        currentChainLevel = null;
        currentChainType = null;
    }

    public static boolean isChainDriveItem(final ItemStack stack) {
        return CogwheelChainType.COGWHEEL_TYPE_BY_ITEM.get(stack.getItem()) != null;
    }

}
