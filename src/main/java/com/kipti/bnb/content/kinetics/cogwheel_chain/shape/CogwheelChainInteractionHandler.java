package com.kipti.bnb.content.kinetics.cogwheel_chain.shape;

import java.util.List;
import java.util.Map.Entry;

import com.kipti.bnb.content.kinetics.cogwheel_chain.block.CogwheelChainBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.RaycastHelper;
import net.createmod.catnip.data.WorldAttached;
import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
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
@SuppressWarnings("null")
public class CogwheelChainInteractionHandler {

    private static final WorldAttached<LevelChainShapeStore> loadedChains =
            new WorldAttached<>($ -> new LevelChainShapeStore());

    public static BlockPos selectedController;
    public static float selectedChainPosition;
    public static Vec3 selectedBakedPosition;
    public static CogwheelChainShape selectedShape;

    private static void clearSelection() {
        selectedController = null;
        selectedShape = null;
        selectedBakedPosition = null;
    }

    private static boolean hasValidSelection(final Level level) {
        if (selectedController == null || selectedShape == null) {
            return false;
        }

        if (!level.isLoaded(selectedController)) {
            return false;
        }

        if (!(level.getBlockEntity(selectedController) instanceof final CogwheelChainBlockEntity chainBE)) {
            return false;
        }

        return chainBE.isController() && chainBE.getChain() != null;
    }

    public static void put(final net.minecraft.world.level.Level level,
                           final BlockPos controllerPos,
                           final List<CogwheelChainShape> shapes) {
        loadedChains.get(level).put(controllerPos, shapes);
    }

    public static void invalidate(final net.minecraft.world.level.Level level, final BlockPos controllerPos) {
        loadedChains.get(level).invalidate(controllerPos);
    }

    public static void clientTick() {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            clearSelection();
            return;
        }

        final Level level = mc.level;
        final var player = mc.player;

        if (!isActive(player.getMainHandItem())) {
            clearSelection();
            return;
        }

        final LevelChainShapeStore levelStore = loadedChains.get(level);
        levelStore.validate(level);

        final Vec3 origin = player.getEyePosition();
        final double range = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;
        final Vec3 target = RaycastHelper.getTraceTarget(player, range, origin);
        final HitResult hitResult = mc.hitResult;

        double bestDistance = hitResult != null ? hitResult.getLocation()
                .distanceToSqr(origin) : Double.MAX_VALUE;

        BlockPos bestController = null;
        CogwheelChainShape bestShape = null;
        float bestChainPosition = 0.0f;
        Vec3 bestVec = null;

        for (final Entry<BlockPos, List<CogwheelChainShape>> entry : levelStore.entries()) {
            final List<CogwheelChainShape> chainShapes = entry.getValue();
            if (chainShapes.isEmpty()) continue;

            final BlockPos controllerPos = entry.getKey();
            final Vec3 controllerBase = Vec3.atLowerCornerOf(controllerPos);
            final CogwheelChainShape shape = chainShapes.get(0);

            final Vec3 localFrom = origin.subtract(controllerBase);
            final Vec3 localTo = target.subtract(controllerBase);
            final Vec3 intersect = shape.intersect(localFrom, localTo);
            if (intersect == null) continue;

            final Vec3 worldHit = intersect.add(controllerBase);
            final double distance = worldHit.distanceToSqr(origin);
            if (distance >= bestDistance) continue;

            bestDistance = distance;
            bestController = controllerPos;
            bestShape = shape;
            bestChainPosition = shape.getChainPosition(intersect);
            bestVec = shape.getVec(controllerPos, bestChainPosition);
        }

        selectedController = bestController;
        selectedShape = bestShape;
        selectedChainPosition = bestChainPosition;
        selectedBakedPosition = bestVec;
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
        if (mc.level == null || !hasValidSelection(mc.level)) {
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
        if (mc.level == null || !hasValidSelection(mc.level)) {
            clearSelection();
            return;
        }
        event.setCanceled(true);
    }

}

