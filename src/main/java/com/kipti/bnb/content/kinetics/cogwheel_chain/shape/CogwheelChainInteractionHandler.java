package com.kipti.bnb.content.kinetics.cogwheel_chain.shape;

import com.kipti.bnb.content.kinetics.cogwheel_chain.world.CogwheelChainWorld;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.RaycastHelper;
import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
        if (!isActive(player.getMainHandItem())) {
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
    }

    private static boolean isActive(final ItemStack mainHand) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }
        return AllItems.WRENCH.isIn(mainHand);
    }

    public static void drawCustomBlockSelection(final PoseStack ms, final MultiBufferSource buffer, final Vec3 camera) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || invalidSelection(mc.level)) {
            clearSelection();
            return;
        }

        final VertexConsumer vb = buffer.getBuffer(RenderType.lines());
        ms.pushPose();
        ms.translate(selectedController.getX() - camera.x, selectedController.getY() - camera.y, selectedController.getZ() - camera.z);
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


