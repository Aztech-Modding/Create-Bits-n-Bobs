package com.kipti.bnb.content.kinetics.cogwheel_chain.shape;

import com.kipti.bnb.content.kinetics.cogwheel_chain.attachment.CogwheelChainAttachment;
import com.kipti.bnb.content.kinetics.cogwheel_chain.edit.CogwheelChainPartialEditInteractionHandler;
import com.kipti.bnb.content.kinetics.cogwheel_chain.placement.CogwheelChainPlacementInteraction;
import com.kipti.bnb.content.kinetics.cogwheel_chain.riding.CogwheelChainRidingHelper;
import com.kipti.bnb.content.kinetics.cogwheel_chain.world.CogwheelChainWorld;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.foundation.utility.RaycastHelper;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Client-side selection and outline handling for cogwheel chains.
 */
@EventBusSubscriber(Dist.CLIENT)
public class CogwheelChainInteractionHandler {

    public static BlockPos selectedController;
    public static float selectedChainPosition;
    public static Vec3 selectedBakedPosition;
    public static CogwheelChainShape selectedShape;

    private static void clearSelection() {
        selectedController = null;
        selectedChainPosition = 0;
        selectedShape = null;
        selectedBakedPosition = null;
    }

    private static boolean invalidSelection(final Level level) {
        if (selectedController == null || selectedShape == null) {
            return true;
        }

        if (!level.isLoaded(selectedController)) {
            return true;
        }

        return !CogwheelChainWorld.get(level).containsChain(selectedController);
    }

    public static void clientTick() {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            clearSelection();
            return;
        }

        final LocalPlayer player = mc.player;
        if (!isActive(player)) {
            clearSelection();
            return;
        }

        final Vec3 origin = player.getEyePosition();
        final double range = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;
        final Vec3 target = RaycastHelper.getTraceTarget(player, range, origin);
        final double vanillaDistSq = mc.hitResult != null
                ? mc.hitResult.getLocation().distanceToSqr(origin)
                : Double.MAX_VALUE;

        final CogwheelChainWorld chainWorld = CogwheelChainWorld.get(mc.level);
        chainWorld.validate(mc.level);

        final ChainDriveShapeHelper.ChainShapeHit hit = ChainDriveShapeHelper.findClosestRayHit(
                mc.level, origin, target, vanillaDistSq);
        if (hit == null) {
            clearSelection();
            return;
        }

        selectedController = hit.controllerPos();
        selectedShape = hit.shape();
        selectedChainPosition = hit.chainPosition();
        selectedBakedPosition = hit.bakedPosition();

        if (!player.isShiftKeyDown() && !CogwheelChainRidingHelper.isRiding()) {
            Outliner.getInstance()
                    .chaseAABB("CogwheelChainPointSelection", new AABB(selectedBakedPosition, selectedBakedPosition))
                    .colored(Color.WHITE)
                    .lineWidth(1 / 6f)
                    .disableLineNormals();
        }
    }

    private static boolean isActive(final LocalPlayer player) {
        return AllItems.WRENCH.isIn(player.getMainHandItem())
                || AllItems.WRENCH.isIn(player.getOffhandItem())
                || player.isHolding(AllItemTags.CHAIN_RIDEABLE::matches)
                || CogwheelChainPlacementInteraction.isCompatibleCogwheelItem(player.getMainHandItem())
                || CogwheelChainPlacementInteraction.isCompatibleCogwheelItem(player.getOffhandItem())
                || CogwheelChainPartialEditInteractionHandler.hasActiveEditContext();
    }

    /**
     * Attempts to mount the player onto the currently selected chain.
     * Called from input event handlers when the player right-clicks while holding a CHAIN_RIDEABLE item.
     *
     * @return {@code true} if the player successfully began riding
     */
    public static boolean onUse() {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return false;
        if (selectedController == null) return false;
        if (!mc.player.isHolding(AllItemTags.CHAIN_RIDEABLE::matches)) return false;
        if (mc.player.isShiftKeyDown()) return false;

        final CogwheelChainAttachment attachment = new CogwheelChainAttachment(
                selectedController,
                selectedChainPosition
        );
        if (!attachment.isValid(mc.level)) return false;

        CogwheelChainRidingHelper.embark(attachment);
        return true;
    }

    public static void drawCustomBlockSelection(final PoseStack ms, final MultiBufferSource buffer, final Vec3 camera) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || invalidSelection(mc.level)) {
            clearSelection();
            return;
        }

        final VertexConsumer vb = buffer.getBuffer(RenderType.lines());
        ms.pushPose();
        ms.translate(
                selectedController.getX() - camera.x,
                selectedController.getY() - camera.y,
                selectedController.getZ() - camera.z
        );
        selectedShape.drawOutline(selectedController, ms, vb);
        ms.popPose();
    }

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event) {
        clientTick();
    }

    @SubscribeEvent
    public static void onRenderWorld(final RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
            return;
        final SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
        drawCustomBlockSelection(event.getPoseStack(), buffer, event.getCamera().getPosition());
    }

    @SubscribeEvent
    public static void hideVanillaBlockSelection(final RenderHighlightEvent.Block event) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || invalidSelection(mc.level)) {
            clearSelection();
            return;
        }
        event.setCanceled(true);
    }

}


