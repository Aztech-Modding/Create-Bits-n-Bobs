package com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.cake.azimuth.behaviour.render.BlockEntityBehaviourRenderer;
import com.kipti.bnb.content.decoration.girder_strut.IBlockEntityRelighter;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.render.ChainQuadBuilder;
import com.kipti.bnb.content.kinetics.cogwheel_chain.render.CogwheelChainRenderGeometryBuilder;
import com.kipti.bnb.content.kinetics.cogwheel_chain.render.CogwheelChainRenderGeometryBuilder.ChainSegment;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
import com.kipti.bnb.foundation.client.ShipyardHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.render.RenderTypes;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Function;

public class CogwheelChainBehaviourRenderer extends BlockEntityBehaviourRenderer<KineticBlockEntity> {

    public static final int MIP_DISTANCE = 48;
    public static final int SEAM_DIST = 16;

    @Override
    public void renderSafe(final SuperBlockEntityBehaviour behaviour, final KineticBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
        super.renderSafe(behaviour, be, partialTicks, ms, buffer, light, overlay);

        if (!(behaviour instanceof final CogwheelChainBehaviour chainBehaviour))
            return;

        final Function<Vector3f, Integer> lighter = IBlockEntityRelighter.createGlobalLighter(be);
        final CogwheelChain chain = chainBehaviour.getControlledChain();

        if (chain != null) {
            final CogwheelChainType type = chain.getChainType();
            final float rotationsPerTick = chainBehaviour.getChainRotationFactor() * be.getSpeed() / (60 * 20);
            final float time = be.getLevel() != null ? AnimationTickHolder.getRenderTime(be.getLevel()) : AnimationTickHolder.getRenderTime();
            final boolean flipInsideOutside = type.getRenderType().usesConsistentInsideOutside() && chain.shouldFlipInsideOutside();

            final float offset = rotationsPerTick == 0 ? 0 : (float) (Math.PI * 2 * rotationsPerTick * time);

            final Vec3 origin = Vec3.atLowerCornerOf(be.getBlockPos());
            final List<ChainSegment> segments = CogwheelChainRenderGeometryBuilder.buildSegments(chain, origin);
            final double totalChainDistance = segments.stream().mapToDouble(ChainSegment::distance).sum();
            if (totalChainDistance <= 1e-4) {
                return;
            }
            final double chainTextureSquish = Math.ceil(totalChainDistance) / totalChainDistance;
            for (final ChainSegment segment : segments) {
                final double stretchOffset = offset + segment.uvStart();

                renderChain(be,
                        ms,
                        buffer,
                        segment.preFrom(),
                        segment.from(),
                        segment.to(),
                        segment.postTo(),
                        segment.fromCogwheelAxis(),
                        segment.toCogwheelAxis(),
                        lighter,
                        (float) stretchOffset,
                        (float) chainTextureSquish,
                        type,
                        flipInsideOutside);
            }
        }
    }

    private static void renderChainSlowerButWithoutGaps(final PoseStack ms,
                                                        final MultiBufferSource buffer,
                                                        final float offset,
                                                        final float textureSquish,
                                                        final Vec3 preFrom,
                                                        final Vec3 from,
                                                        final Vec3 to,
                                                        final Vec3 postTo,
                                                        final Vec3 fromCogwheelAxis,
                                                        final Vec3 toCogwheelAxis,
                                                        final int lightAtSource,
                                                        final int lightAtDest,
                                                        final CogwheelChainType type,
                                                        final boolean flipInsideOutside) {
        final CogwheelChainType.ChainRenderInfo chainRenderInfo = type.getRenderType();

        // Calculate corners in world space for the segment ends
        List<Vec3> destinationPoints = CogwheelChainRenderGeometryBuilder.getEndPointsForChainJoint(from, to, postTo, chainRenderInfo, toCogwheelAxis);
        final List<Vec3> sourcePoints = CogwheelChainRenderGeometryBuilder.getEndPointsForChainJoint(preFrom, from, to, chainRenderInfo, fromCogwheelAxis);

        //This is my shame, i couldnt find a deterministic way to order the points consistently between joints so here we are,
        //Matching it in a post process step
        destinationPoints = CogwheelChainRenderGeometryBuilder.getPointsInClosestOrder(destinationPoints, sourcePoints);

        final float length = (float) from.distanceTo(to);
        final float minV = offset * textureSquish;
        final float maxV = length * textureSquish + minV;

        ms.pushPose();

        final VertexConsumer vc = buffer.getBuffer(RenderTypes.chain(type.getRenderTexture()));
        final Matrix4f poseMatrix = ms.last().pose();
        final PoseStack.Pose pose = ms.last();

        // Per-vertex light interpolation data: project vertex position onto the from→to
        // direction to blend between lightAtSource and lightAtDest. This eliminates abrupt
        // brightness jumps at segment boundaries (previously only lightAtSource was used).
        final Vec3 segDir = to.subtract(from);
        final double segLenSq = segDir.lengthSqr();

        // Emit all faces via shared geometry builder, adapting vertex output to VertexConsumer
        final ChainQuadBuilder.VertexEmitter emitter = (x, y, z, u, v, nx, ny, nz) -> {
            final float rx = x - (float) from.x;
            final float ry = y - (float) from.y;
            final float rz = z - (float) from.z;
            final float t = segLenSq > 1e-8
                    ? Mth.clamp((float) (new Vec3(x, y, z).subtract(from).dot(segDir) / segLenSq), 0f, 1f)
                    : 0f;
            final int vertexLight = lerpPackedLight(lightAtSource, lightAtDest, t);
            vc.addVertex(poseMatrix, rx, ry, rz)
                    .setColor(1.0f, 1.0f, 1.0f, 1.0f)
                    .setUv(u, v)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(vertexLight)
                    .setNormal(pose, 0.0F, 1.0F, 0.0F);
        };

        ChainQuadBuilder.buildSegmentFaces(destinationPoints, sourcePoints, chainRenderInfo, minV, maxV, flipInsideOutside, emitter);

        ms.popPose();
    }

    /**
     * Linearly interpolates between two packed light values (block | sky << 16).
     */
    private static int lerpPackedLight(final int light1, final int light2, final float t) {
        final int block = (int) Mth.lerp(t, light1 & 0xFFFF, light2 & 0xFFFF);
        final int sky = (int) Mth.lerp(t, (light1 >> 16) & 0xFFFF, (light2 >> 16) & 0xFFFF);
        return block | (sky << 16);
    }

    private static void renderChainFastButWithGaps(final PoseStack ms,
                                                   final MultiBufferSource buffer,
                                                   final float offset,
                                                   final float textureSquish,
                                                   final float length,
                                                   final int light1,
                                                   final int light2,
                                                   final boolean far,
                                                   final CogwheelChainType type) {
        final CogwheelChainType.ChainRenderInfo info = type.getRenderType();
        final float w = info.getWidth() / 16f;
        final float h = info.getHeight() / 16f;

        ms.pushPose();
        ms.translate(0.5D, 0.0D, 0.5D);

        final VertexConsumer vc = buffer.getBuffer(RenderTypes.chain(type.getRenderTexture()));
        final PoseStack.Pose pose = ms.last();
        final Matrix4f matrix4f = pose.pose();

        if (info.getVertexShape() == CogwheelChainType.VertexShape.CROSS) {
            final float radius = far ? 1f / 16f : w / 2f;
            final float minV = far ? 0 : offset * textureSquish;
            final float maxV = far ? h : length * textureSquish + minV;
            final float crossMinU = far ? w : 0;
            final float crossMaxU = far ? w + 1f / 16f : w;
            final float crossUO = far ? 0 : w;

            // Two perpendicular cross planes, each double-sided
            renderQuad(matrix4f, pose, vc, 0, length, 0, radius, 0, -radius, crossMinU, crossMaxU, minV, maxV, light1, light2);
            renderQuad(matrix4f, pose, vc, 0, length, 0, -radius, 0, radius, crossMinU, crossMaxU, minV, maxV, light1, light2);
            renderQuad(matrix4f, pose, vc, 0, length, radius, 0, -radius, 0, crossMinU + crossUO, crossMaxU + crossUO, minV, maxV, light1, light2);
            renderQuad(matrix4f, pose, vc, 0, length, -radius, 0, radius, 0, crossMinU + crossUO, crossMaxU + crossUO, minV, maxV, light1, light2);
        } else {
            // SQUARE shape: rectangular beam with 4 single-sided faces
            final float halfW = far ? 1f / 16f : w / 2f;
            final float halfH = far ? 1f / 16f : h / 2f;
            final float minV = far ? 0 : offset * textureSquish;
            final float maxV = far ? h : length * textureSquish + minV;

            // Top face
            renderQuad(matrix4f, pose, vc, 0, length, -halfW, halfH, halfW, halfH, h, h + w, minV, maxV, light1, light2);
            // Left face
            renderQuad(matrix4f, pose, vc, 0, length, -halfW, -halfH, -halfW, halfH, 0, h, minV, maxV, light1, light2);
            // Bottom face
            renderQuad(matrix4f, pose, vc, 0, length, halfW, -halfH, -halfW, -halfH, h + w + h, h + w + h + w, minV, maxV, light1, light2);
            // Right face
            renderQuad(matrix4f, pose, vc, 0, length, halfW, halfH, halfW, -halfH, h + w, h + w + h, minV, maxV, light1, light2);
        }

        ms.popPose();
    }

    private static void renderQuad(final Matrix4f pPose, final PoseStack.Pose pNormal, final VertexConsumer pConsumer, final float pMinY, final float pMaxY,
                                   final float pMinX, final float pMinZ, final float pMaxX, final float pMaxZ, final float pMinU, final float pMaxU, final float pMinV, final float pMaxV,
                                   final int light1, final int light2) {
        addVertex(pPose, pNormal, pConsumer, pMaxY, pMinX, pMinZ, pMaxU, pMinV, light2);
        addVertex(pPose, pNormal, pConsumer, pMinY, pMinX, pMinZ, pMaxU, pMaxV, light1);
        addVertex(pPose, pNormal, pConsumer, pMinY, pMaxX, pMaxZ, pMinU, pMaxV, light1);
        addVertex(pPose, pNormal, pConsumer, pMaxY, pMaxX, pMaxZ, pMinU, pMinV, light2);
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

    private void renderChain(final KineticBlockEntity be,
                             final PoseStack ms,
                             final MultiBufferSource buffer,
                             final Vec3 preFrom,
                             final Vec3 from,
                             final Vec3 to,
                             final Vec3 postTo,
                             final Vec3 fromCogwheelAxis,
                             final Vec3 toCogwheelAxis,
                             final Function<Vector3f, Integer> lighter,
                             final float offset,
                             final float textureSquish,
                             final CogwheelChainType type,
                             final boolean flipInsideOutside) {
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

        final boolean inShipyardLod = ShipyardHelper.isProbablyInShipyard(BlockPos.containing(from));

        final boolean far = !inShipyardLod && (Minecraft.getInstance().level == be.getLevel() && !Minecraft.getInstance()
                .getBlockEntityRenderDispatcher().camera.getPosition()
                .closerThan(from.lerp(to, 0.5), MIP_DISTANCE));
        final boolean close = inShipyardLod || (Minecraft.getInstance().level == be.getLevel() && Minecraft.getInstance()
                .getBlockEntityRenderDispatcher().camera.getPosition()
                .closerThan(from.lerp(to, 0.5), SEAM_DIST));

        if (close)
            renderChainSlowerButWithoutGaps(ms, buffer, offset, textureSquish, preFrom, from, to, postTo, fromCogwheelAxis, toCogwheelAxis, light1, light2, type, flipInsideOutside);
        else {
            chain.rotateYDegrees((float) yaw);
            chain.rotateXDegrees(90 - (float) pitch);
            chain.rotateYDegrees(45);
            final float overextend = 0.05f;
            chain.translate(0, 8 / 16f - overextend / 2f, 0);
            chain.uncenter();
            renderChainFastButWithGaps(ms, buffer, offset - overextend / 2f, textureSquish, (float) from.distanceTo(to) + overextend, light1, light2, far, type);
        }

        ms.popPose();
    }
}
