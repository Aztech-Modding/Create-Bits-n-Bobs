package com.kipti.bnb.content.kinetics.cogwheel_chain.block;

import java.util.List;
import java.util.Map.Entry;

import com.google.common.cache.Cache;
import com.kipti.bnb.CreateBitsnBobs;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.RaycastHelper;
import com.simibubi.create.foundation.utility.TickBasedCache;
import net.createmod.catnip.data.WorldAttached;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

/**
 * Client-side selection and outline handling for cogwheel chains.
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = CreateBitsnBobs.MOD_ID)
public class CogwheelChainInteractionHandler {

    public static final WorldAttached<Cache<BlockPos, List<CogwheelChainShape>>> loadedChains =
            new WorldAttached<>($ -> new TickBasedCache<>(60, true));

    public static BlockPos selectedController;
    public static float selectedChainPosition;
    public static Vec3 selectedBakedPosition;
    public static CogwheelChainShape selectedShape;

    public static void put(final net.minecraft.world.level.Level level,
                           final BlockPos controllerPos,
                           final List<CogwheelChainShape> shapes) {
        loadedChains.get(level)
                .put(controllerPos, shapes);
    }

    public static void invalidate(final net.minecraft.world.level.Level level, final BlockPos controllerPos) {
        loadedChains.get(level)
                .invalidate(controllerPos);
    }

    public static void clientTick() {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            selectedController = null;
            selectedShape = null;
            selectedBakedPosition = null;
            return;
        }

        if (!isActive(mc.player.getMainHandItem())) {
            selectedController = null;
            selectedShape = null;
            selectedBakedPosition = null;
            return;
        }

        final Vec3 origin = mc.player.getEyePosition();
        final double range = mc.player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;
        final Vec3 target = RaycastHelper.getTraceTarget(mc.player, range, origin);
        final HitResult hitResult = mc.hitResult;

        double bestDistance = hitResult != null ? hitResult.getLocation()
                .distanceToSqr(origin) : Double.MAX_VALUE;

        BlockPos bestController = null;
        CogwheelChainShape bestShape = null;
        float bestChainPosition = 0.0f;
        Vec3 bestVec = null;

        for (final Entry<BlockPos, List<CogwheelChainShape>> entry : loadedChains.get(mc.level)
                .asMap()
                .entrySet()) {
            final BlockPos controllerPos = entry.getKey();
            final Vec3 controllerBase = Vec3.atLowerCornerOf(controllerPos);

            for (final CogwheelChainShape shape : entry.getValue()) {
                final Vec3 localFrom = origin.subtract(controllerBase);
                final Vec3 localTo = target.subtract(controllerBase);
                final Vec3 intersect = shape.intersect(localFrom, localTo);
                if (intersect == null) {
                    continue;
                }

                final Vec3 worldHit = intersect.add(controllerBase);
                final double distance = worldHit.distanceToSqr(origin);
                if (distance > bestDistance) {
                    continue;
                }

                bestDistance = distance;
                bestController = controllerPos;
                bestShape = shape;
                bestChainPosition = shape.getChainPosition(intersect);
                bestVec = shape.getVec(controllerPos, bestChainPosition);
            }
        }

        selectedController = bestController;
        selectedShape = bestShape;
        selectedChainPosition = bestChainPosition;
        selectedBakedPosition = bestVec;

        if (selectedController != null && selectedBakedPosition != null) {
            Outliner.getInstance()
                    .chaseAABB("CogwheelChainPointSelection", new AABB(selectedBakedPosition, selectedBakedPosition))
                    .colored(Color.WHITE)
                    .lineWidth(1 / 8f)
                    .disableLineNormals();
        }
    }

    private static boolean isActive(final ItemStack mainHand) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }
        if (!mc.player.isShiftKeyDown()) {
            return false;
        }
        return AllItems.WRENCH.isIn(mainHand);
    }

    public static void drawCustomBlockSelection(final PoseStack ms, final MultiBufferSource buffer, final Vec3 camera) {
        if (selectedController == null || selectedShape == null) {
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
    public static void hideVanillaBlockSelection(final RenderHighlightEvent.Block event) {
        if (selectedController == null || selectedShape == null) {
            return;
        }
        drawCustomBlockSelection(event.getPoseStack(), event.getMultiBufferSource(), event.getCamera().getPosition());
        event.setCanceled(true);
    }
}

