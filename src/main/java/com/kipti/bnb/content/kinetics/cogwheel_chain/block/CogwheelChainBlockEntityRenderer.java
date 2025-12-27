package com.kipti.bnb.content.kinetics.cogwheel_chain.block;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.RenderedChainPathNode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.RenderTypes;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Function;

public class CogwheelChainBlockEntityRenderer extends KineticBlockEntityRenderer<CogwheelChainBlockEntity> {

    public static final ResourceLocation CHAIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/block/chain.png");
    public static final int MIP_DISTANCE = 48;
    public static final int SEAM_DIST = 16;

    public CogwheelChainBlockEntityRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final CogwheelChainBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        final Function<Vector3f, Integer> lighter = be.createGlobalLighter();
        final CogwheelChain chain = be.getChain();
        if (be.isController() && chain != null) {
            final float rotationsPerTick = be.getChainRotationFactor() * be.getSpeed() / (60 * 20);
            final float time = be.getLevel() != null ? AnimationTickHolder.getRenderTime(be.getLevel()) : AnimationTickHolder.getRenderTime();

            final float offset = rotationsPerTick == 0 ? 0 : (float) (Math.PI * 2 * rotationsPerTick * time);

            final double totalChainDistance = calculateTotalChainDistance(be);
            final double chainTextureSquish = Math.ceil(totalChainDistance) / totalChainDistance;

            double accumulatedUV = 0f;

            final Vec3 origin = Vec3.atLowerCornerOf(be.getBlockPos());
            final int size = chain.getChainPathNodes().size();
            for (int i = 0; i < size; i++) {
                final RenderedChainPathNode node0 = chain.getChainPathNodes().get((size + i - 1) % size);
                final RenderedChainPathNode node1 = chain.getChainPathNodes().get(i);
                final RenderedChainPathNode node2 = chain.getChainPathNodes().get((i + 1) % size);
                final RenderedChainPathNode node3 = chain.getChainPathNodes().get((i + 2) % size);

                final double stretchOffset = offset + accumulatedUV;

                final double distance = node1.getPosition().add(origin).distanceTo(node2.getPosition().add(origin));
                accumulatedUV += distance;

                renderChain(be, ms, buffer, node3.getPosition().add(origin), node2.getPosition().add(origin), node1.getPosition().add(origin), node0.getPosition().add(origin), lighter, (float) stretchOffset, (float) chainTextureSquish);
            }
        }
    }

    private double calculateTotalChainDistance(final CogwheelChainBlockEntity be) {
        double totalDistance = 0f;
        final Vec3 origin = Vec3.atLowerCornerOf(be.getBlockPos());
        for (int i = 0; i < be.getChain().getChainPathNodes().size(); i++) {
            final RenderedChainPathNode nodeA = be.getChain().getChainPathNodes().get(i);
            final RenderedChainPathNode nodeB = be.getChain().getChainPathNodes().get((i + 1) % be.getChain().getChainPathNodes().size());

            totalDistance += nodeA.getPosition().add(origin).distanceTo(nodeB.getPosition().add(origin));
        }
        return totalDistance;
    }

    private void renderChain(final CogwheelChainBlockEntity be,
                             final PoseStack ms,
                             final MultiBufferSource buffer,
                             final Vec3 preFrom,
                             final Vec3 from,
                             final Vec3 to,
                             final Vec3 postTo,
                             final Function<Vector3f, Integer> lighter,
                             final float offset,
                             final float textureSquish) {
        final Vec3 diff = to.subtract(from);
        final double yaw = Mth.RAD_TO_DEG * Mth.atan2(diff.x, diff.z);
        final double pitch = Mth.RAD_TO_DEG * Mth.atan2(diff.y, diff.multiply(1, 0, 1)
                .length());

        final BlockPos tilePos = be.getBlockPos();

        final Vec3 startOffset = from.subtract(Vec3.atCenterOf(tilePos));

        ms.pushPose();
        final PoseTransformStack chain = TransformStack.of(ms);
        chain.center();
        chain.translate(startOffset);

        final int light1 = lighter.apply(new Vector3f((float) from.x, (float) from.y, (float) from.z));
        final int light2 = lighter.apply(new Vector3f((float) to.x, (float) to.y, (float) to.z));

        final boolean far = Minecraft.getInstance().level == be.getLevel() && !Minecraft.getInstance()
                .getBlockEntityRenderDispatcher().camera.getPosition()
                .closerThan(from.lerp(to, 0.5), MIP_DISTANCE);
        final boolean close = Minecraft.getInstance().level == be.getLevel() && Minecraft.getInstance()
                .getBlockEntityRenderDispatcher().camera.getPosition()
                .closerThan(from.lerp(to, 0.5), SEAM_DIST);

        if (close)
            renderChainSlowerButWithoutGaps(ms, buffer, offset, textureSquish, preFrom, from, to, postTo, light1, light2);
        else {
            chain.rotateYDegrees((float) yaw);
            chain.rotateXDegrees(90 - (float) pitch);
            chain.rotateYDegrees(45);
            final float overextend = 0.05f;
            chain.translate(0, 8 / 16f - overextend / 2f, 0);
            chain.uncenter();
            renderChainFastButWithGaps(ms, buffer, offset - overextend / 2f, textureSquish, (float) from.distanceTo(to) + overextend, light1, light2, far);
        }

        ms.popPose();
    }

    private static void renderChainSlowerButWithoutGaps(final PoseStack ms, final MultiBufferSource buffer, final float offset, final float textureSquish, final Vec3 preFrom, final Vec3 from, final Vec3 to, final Vec3 postTo, final int light1, final int light2) {
        final List<Vec3> endPoints = getEndPointsForChainJoint(from, to, postTo);
        final List<Vec3> fromPoint = getEndPointsForChainJoint(preFrom, from, to);
        final float length = (float) from.distanceTo(to);
        final float minV = offset * textureSquish;
        final float maxV = length * textureSquish + minV;
        final float minU = 0;
        final float maxU = 3 / 16f;
        ms.pushPose();

        final VertexConsumer vc = buffer.getBuffer(RenderTypes.chain(CHAIN_LOCATION));
        final Matrix4f matrix4f = ms.last().pose();
        for (int i = 0; i < 4; i += 1) {
            final float uO = (i % 2 == 1) ? 0 : 3 / 16f;
            addVertex(matrix4f, ms.last(), vc, endPoints.get((i + 2) % 4).subtract(from), minU + uO, minV, light1);
            addVertex(matrix4f, ms.last(), vc, fromPoint.get((i + 2) % 4).subtract(from), minU + uO, maxV, light1);
            addVertex(matrix4f, ms.last(), vc, fromPoint.get(i).subtract(from), maxU + uO, maxV, light1);
            addVertex(matrix4f, ms.last(), vc, endPoints.get(i).subtract(from), maxU + uO, minV, light1);
        }

        ms.popPose();
    }

    private static List<Vec3> getEndPointsForChainJoint(final Vec3 before, final Vec3 point, final Vec3 after) {
        final float radius = 1.5f / 16f;
        final Vec3 dirToBefore = point.subtract(before).normalize();
        final Vec3 dirToAfter = after.subtract(point).normalize();

        final Vec3 averagedDir = dirToBefore.add(dirToAfter).normalize();

        final Quaternionf quat = new Quaternionf().rotationTo(0, 1, 0, (float) averagedDir.x, (float) averagedDir.y, (float) averagedDir.z);

        final Vector3d localAxis1Joml = quat.transform(1f, 0f, 0f, new Vector3d());
        final Vec3 localAxis1 = new Vec3(localAxis1Joml.x, localAxis1Joml.y, localAxis1Joml.z).normalize();
        final Vector3d localAxis2Joml = quat.transform(0f, 0f, 1f, new Vector3d());
        final Vec3 localAxis2 = new Vec3(localAxis2Joml.x, localAxis2Joml.y, localAxis2Joml.z).normalize();

        return List.of(
                point.add(localAxis1.add(localAxis2).normalize().scale(radius)),
                point.add(localAxis1.subtract(localAxis2).normalize().scale(radius)),
                point.add(localAxis2.scale(-1).subtract(localAxis1).normalize().scale(radius)),
                point.add(localAxis2.subtract(localAxis1).normalize().scale(radius))
        );
    }

    private static void renderChainFastButWithGaps(final PoseStack ms, final MultiBufferSource buffer, final float offset, final float textureSquish, final float length, final int light1,
                                                   final int light2, final boolean far) {
        final float radius = far ? 1f / 16f : 1.5f / 16f;
        final float minV = far ? 0 : offset * textureSquish;
        final float maxV = far ? 1 / 16f : length * textureSquish + minV;
        final float minU = far ? 3 / 16f : 0;
        final float maxU = far ? 4 / 16f : 3 / 16f;

        ms.pushPose();
        ms.translate(0.5D, 0.0D, 0.5D);

        final VertexConsumer vc = buffer.getBuffer(RenderTypes.chain(CHAIN_LOCATION));
        renderPart(ms, vc, length, 0.0F, radius, radius, 0.0F, -radius, 0.0F, 0.0F, -radius, minU, maxU, minV, maxV,
                light1, light2, far);

        ms.popPose();
    }

    private static void renderPart(final PoseStack pPoseStack, final VertexConsumer pConsumer, final float pMaxY, final float pX0, final float pZ0,
                                   final float pX1, final float pZ1, final float pX2, final float pZ2, final float pX3, final float pZ3, final float pMinU, final float pMaxU, final float pMinV,
                                   final float pMaxV, final int light1, final int light2, final boolean far) {
        final PoseStack.Pose posestack$pose = pPoseStack.last();
        final Matrix4f matrix4f = posestack$pose.pose();

        final float uO = far ? 0f : 3 / 16f;
        renderQuad(matrix4f, posestack$pose, pConsumer, 0, pMaxY, pX0, pZ0, pX3, pZ3, pMinU, pMaxU, pMinV, pMaxV, light1,
                light2);
        renderQuad(matrix4f, posestack$pose, pConsumer, 0, pMaxY, pX3, pZ3, pX0, pZ0, pMinU, pMaxU, pMinV, pMaxV, light1,
                light2);
        renderQuad(matrix4f, posestack$pose, pConsumer, 0, pMaxY, pX1, pZ1, pX2, pZ2, pMinU + uO, pMaxU + uO, pMinV, pMaxV,
                light1, light2);
        renderQuad(matrix4f, posestack$pose, pConsumer, 0, pMaxY, pX2, pZ2, pX1, pZ1, pMinU + uO, pMaxU + uO, pMinV, pMaxV,
                light1, light2);
    }

    private static void renderQuad(final Matrix4f pPose, final PoseStack.Pose pNormal, final VertexConsumer pConsumer, final float pMinY, final float pMaxY,
                                   final float pMinX, final float pMinZ, final float pMaxX, final float pMaxZ, final float pMinU, final float pMaxU, final float pMinV, final float pMaxV,
                                   final int light1, final int light2) {
        addVertex(pPose, pNormal, pConsumer, pMaxY, pMinX, pMinZ, pMaxU, pMinV, light2);
        addVertex(pPose, pNormal, pConsumer, pMinY, pMinX, pMinZ, pMaxU, pMaxV, light1);
        addVertex(pPose, pNormal, pConsumer, pMinY, pMaxX, pMaxZ, pMinU, pMaxV, light1);
        addVertex(pPose, pNormal, pConsumer, pMaxY, pMaxX, pMaxZ, pMinU, pMinV, light2);
    }

    private static void addVertex(final Matrix4f pPose, final PoseStack.Pose pNormal, final VertexConsumer pConsumer, final Vec3 pPos,
                                  final float pU, final float pV, final int light) {
        addVertex(pPose, pNormal, pConsumer, (float) pPos.y, (float) pPos.x, (float) pPos.z, pU, pV, light);
    }

    private static void addVertex(final Matrix4f pPose, final PoseStack.Pose pNormal, final VertexConsumer pConsumer, final float pY, final float pX,
                                  final float pZ, final float pU, final float pV, final int light) {
        pConsumer.addVertex(pPose, pX, pY, pZ)
                .setColor(1.0f, 1.0f, 1.0f, 1.0f)
                .setUv(pU, pV)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pNormal, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(final CogwheelChainBlockEntity blockEntity) {
        return true;
    }

}
