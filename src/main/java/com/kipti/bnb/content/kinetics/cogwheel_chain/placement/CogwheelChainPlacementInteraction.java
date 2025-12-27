package com.kipti.bnb.content.kinetics.cogwheel_chain.placement;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.ChainInteractionFailedException;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelChain;
import com.kipti.bnb.network.packets.from_client.PlaceCogwheelChainPacket;
import com.kipti.bnb.registry.BnbFeatureFlag;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    protected static @Nullable PlacingCogwheelChain currentBuildingChain = null;
    protected static @Nullable ResourceKey<Level> currentChainLevel = null;

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
        final ItemStack itemInHand = player.getMainHandItem().is(Items.CHAIN) ? player.getMainHandItem() :
                player.getOffhandItem().is(Items.CHAIN) ? player.getOffhandItem() : null;

        if (itemInHand == null) {
            return false;
        }

        //If crouching, try clear
        if (player.isShiftKeyDown()) {
            if (currentBuildingChain != null) {
                currentBuildingChain = null;
                currentChainLevel = null;
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

        if (!validBlockTarget) {
            return currentBuildingChain != null;
        }
        //Cancel normal interaction
        event.setSwingHand(true);

        if (currentBuildingChain == null || currentChainLevel == null || !currentChainLevel.equals(level.dimension())) {
            //Start a new chain
            currentBuildingChain = new PlacingCogwheelChain(hitPos, targetedState.getValue(CogWheelBlock.AXIS), PlacingCogwheelChain.isLargeBlockTarget(targetedState), PlacingCogwheelChain.hasSmallCogwheelOffset(targetedState));
            currentChainLevel = level.dimension();

            player.displayClientMessage(Component.translatable("tooltip.bits_n_bobs.chain_drive_placing_hint"), true);
        } else {
            //if this is the last node, then remove the last one
            if (currentBuildingChain.getLastNode().pos().equals(hitPos)) {
                currentBuildingChain.getNodes().removeLast();
                //If no nodes left, clear chain
                if (currentBuildingChain.getNodes().isEmpty()) {
                    currentBuildingChain = null;
                    currentChainLevel = null;
                }
                return true;
            }

            //Try to add to existing chain
            try {
                final boolean added = currentBuildingChain.tryAddNode(hitPos, targetedState);

                if (!added) { //Only happens with invalid target, ignore quietly
                    return true;
                }

                final boolean completed;
                try {
                    completed = currentBuildingChain.canBuildChainIfLooping();
                } catch (final ChainInteractionFailedException exception) {
                    player.displayClientMessage(exception.getComponent(), true);
                    currentBuildingChain = null;
                    currentChainLevel = null;
                    return true;
                }

                if (completed) {
                    //If completed, send to server, clear current chain
                    CatnipServices.NETWORK.sendToServer(new PlaceCogwheelChainPacket(currentBuildingChain, event.getHand().ordinal()));

                    currentBuildingChain = null;
                    currentChainLevel = null;
                }
            } catch (final ChainInteractionFailedException exception) {
                //Send message on fail
                player.displayClientMessage(exception.getComponent(), true);
            }
        }
        return true;
    }

}
