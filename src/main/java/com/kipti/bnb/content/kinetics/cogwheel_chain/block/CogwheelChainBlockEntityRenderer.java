package com.kipti.bnb.content.kinetics.cogwheel_chain.block;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.RenderedChainPathNode;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import com.simibubi.create.foundation.render.RenderTypes;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class CogwheelChainBlockEntityRenderer extends KineticBlockEntityRenderer<CogwheelChainBlockEntity> {

    public static final int MIP_DISTANCE = 48;
    public static final int SEAM_DIST = 16;

    public CogwheelChainBlockEntityRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(final CogwheelChainBlockEntity be, final BlockState state) {
        return CachedBuffers.partialFacingVertical(
                Objects.requireNonNull(GenericBlockEntityRenderModels.REGISTRY.get(state.getBlock())),
                state.getBlock().defaultBlockState(),
                Direction.fromAxisAndDirection(getRotationAxisOf(be), Direction.AxisDirection.POSITIVE)
        );

//        return CachedBuffers.block(KINETIC_BLOCK, state);
    }

    @Override
    protected void renderSafe(final CogwheelChainBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        if (be.getBlockState().getBlock() instanceof EncasedCogwheelBlock) {
            final BlockState blockState = be.getBlockState();
            final Block block = blockState.getBlock();
            if (block instanceof final IRotate def) {
                final Direction.Axis axis = getRotationAxisOf(be);
                boolean large = false;
                if (block instanceof final ICogwheelChainBlock chainBlock)
                    large = chainBlock.isLargeCog();

                final float angle = large ? BracketedKineticBlockEntityRenderer.getAngleForLargeCogShaft(be, axis)
                        : getAngleForBe(be, be.getBlockPos(), axis);

                for (final Direction d : Iterate.directionsInAxis(getRotationAxisOf(be))) {
                    if (def.hasShaftTowards(be.getLevel(), be.getBlockPos(), blockState, d)) {
                        final SuperByteBuffer shaft = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), d);
                        kineticRotationTransform(shaft, be, axis, angle, light);
                        shaft.renderInto(ms, buffer.getBuffer(RenderType.solid()));
                    }
                }
            }
        }

        final Function<Vector3f, Integer> lighter = be.createGlobalLighter();
        final CogwheelChain chain = be.getChain();

        if (be.isController() && chain != null) {
            final CogwheelChainType type = chain.getChainType();
            final float rotationsPerTick = be.getChainRotationFactor() * be.getSpeed() / (60 * 20);
            final float time = be.getLevel() != null ? AnimationTickHolder.getRenderTime(be.getLevel()) : AnimationTickHolder.getRenderTime();
            final boolean flipInsideOutside = type.getRenderType().usesConsistentInsideOutside() && chain.shouldFlipInsideOutside();

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

                renderChain(be,
                        ms,
                        buffer,
                        node3.getPosition().add(origin),
                        node2.getPosition().add(origin),
                        node1.getPosition().add(origin),
                        node0.getPosition().add(origin),
                        node2.sourceCogwheelAxis(),
                        node1.sourceCogwheelAxis(),
                        lighter,
                        (float) stretchOffset,
                        (float) chainTextureSquish,
                        type,
                        flipInsideOutside);

                accumulatedUV += distance;

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

        final boolean far = Minecraft.getInstance().level == be.getLevel() && !Minecraft.getInstance()
                .getBlockEntityRenderDispatcher().camera.getPosition()
                .closerThan(from.lerp(to, 0.5), MIP_DISTANCE);
        final boolean close = Minecraft.getInstance().level == be.getLevel() && Minecraft.getInstance()
                .getBlockEntityRenderDispatcher().camera.getPosition()
                .closerThan(from.lerp(to, 0.5), SEAM_DIST);

        if (close || type.getRenderType() != CogwheelChainType.ChainRenderInfo.CHAIN) //For now there is only slow rendering of diff types TODO implement fast
            renderChainSlowerButWithoutGaps(ms, buffer, offset, textureSquish, preFrom, from, to, postTo, fromCogwheelAxis, toCogwheelAxis, light1, light2, type, flipInsideOutside);
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
        List<Vec3> destinationPoints = getEndPointsForChainJoint(from, to, postTo, chainRenderInfo, toCogwheelAxis);
        final List<Vec3> sourcePoints = getEndPointsForChainJoint(preFrom, from, to, chainRenderInfo, fromCogwheelAxis);

        //This is my shame, i couldnt find a deterministic way to order the points consistently between joints so here we are,
        //Matching it in a post process step
        destinationPoints = getPointsInClosestOrder(destinationPoints, sourcePoints);

        final float length = (float) from.distanceTo(to);
        final float minV = offset * textureSquish;
        final float maxV = length * textureSquish + minV;

        ms.pushPose();

        final VertexConsumer vc = buffer.getBuffer(RenderTypes.chain(type.getRenderTexture()));
        final Matrix4f poseMatrix = ms.last().pose();

        // Iterate over the 4 faces of the chain beam
        for (int faceIndex = 0; faceIndex < 4; faceIndex += 1) {
            if (chainRenderInfo.getVertexShape() == CogwheelChainType.VertexShape.CROSS) {
                renderCrossShapeFace(ms, vc, poseMatrix, from, destinationPoints, sourcePoints, faceIndex, minV, maxV, lightAtSource);
            } else {
                renderDefaultShapeFace(ms, vc, poseMatrix, from, destinationPoints, sourcePoints, chainRenderInfo, faceIndex, minV, maxV, lightAtSource, flipInsideOutside);
            }
        }

        ms.popPose();
    }

    /**
     * Ai slop but heres the explanation:
     * - When you have two sets of 4 points each that are supposed to correspond to each other in pairs, there are 24 possible ways to match them up
     * - You could just say, "find the nearest point to point connection and connect that way", but that doesent preserve the correct order we are after.
     * - Therefore, this needs a "ring-preserving" search to ensure we dont loose the orderings (that are necassary for making CROSS rendering shapes work)
     */
    private static List<Vec3> getPointsInClosestOrder(final List<Vec3> destinationPoints, final List<Vec3> sourcePoints) {
        if (destinationPoints.size() != 4 || sourcePoints.size() != 4) {
            return new ArrayList<>(destinationPoints);
        }

        double bestScore = Double.POSITIVE_INFINITY;
        List<Vec3> best = new ArrayList<>(destinationPoints);

        // Only allow cyclic orderings: 4 rotations * (normal/reversed winding) = 8 candidates
        for (int reversed = 0; reversed <= 1; reversed++) {
            for (int shift = 0; shift < 4; shift++) {
                final ArrayList<Vec3> candidate = new ArrayList<>(4);
                double pointScore = 0.0;

                for (int i = 0; i < 4; i++) {
                    final int j = (reversed == 0)
                            ? ((i + shift) & 3)
                            : ((shift - i + 4) & 3);

                    final Vec3 d = destinationPoints.get(j);
                    candidate.add(d);
                    pointScore += sourcePoints.get(i).distanceToSqr(d);
                }

                // Edge-direction term to discourage "crossed" face mapping
                double edgeScore = 0.0;
                for (int i = 0; i < 4; i++) {
                    final Vec3 sEdge = sourcePoints.get((i + 1) & 3).subtract(sourcePoints.get(i)).normalize();
                    final Vec3 dEdge = candidate.get((i + 1) & 3).subtract(candidate.get(i)).normalize();
                    edgeScore += 1.0 - sEdge.dot(dEdge); // 0 is best
                }

                // Small bias against reversing winding unless clearly better
                final double windingPenalty = reversed == 1 ? 1e-4 : 0.0;
                final double score = pointScore + edgeScore * 0.25 + windingPenalty;

                if (score < bestScore) {
                    bestScore = score;
                    best = candidate;
                }
            }
        }

        return best;
    }

    private static void renderCrossShapeFace(final PoseStack ms, final VertexConsumer vc, final Matrix4f poseMatrix, final Vec3 origin,
                                             final List<Vec3> destinationPoints, final List<Vec3> sourcePoints,
                                             final int faceIndex, final float minV, final float maxV, final int light) {
        final float uOffset = (faceIndex % 2 == 1) ? 0 : 3 / 16f;
        final float uWidth = 3 / 16f;
        final float minU = 0f;

        final float uLeft = minU + uOffset;
        final float uRight = uWidth + uOffset;

        // "Top" corresponds to Destination (Matches original logic where endPoints got minV)
        // "Bottom" corresponds to Source
        // Indices preserved from original 'CROSS' logic: (i+2), (i+2), i, i
        final Vec3 posTL = destinationPoints.get((faceIndex + 2) % 4);
        final Vec3 posBL = sourcePoints.get((faceIndex + 2) % 4);
        final Vec3 posBR = sourcePoints.get(faceIndex);
        final Vec3 posTR = destinationPoints.get(faceIndex);

        renderSubdividedQuad(ms, vc, poseMatrix, origin, posTL, posBL, posBR, posTR, uLeft, uRight, minV, maxV, light);
    }

    private static void renderDefaultShapeFace(final PoseStack ms, final VertexConsumer vc, final Matrix4f poseMatrix, final Vec3 origin,
                                               final List<Vec3> destinationPoints, final List<Vec3> sourcePoints,
                                               final CogwheelChainType.ChainRenderInfo renderInfo,
                                               final int faceIndex, final float minV, final float maxV, final int light, final boolean flipTopBottom) {
        final float h = renderInfo.getHeight() / 16f;
        final float w = renderInfo.getWidth() / 16f;

        final float minU;
        final float maxU;

        final int uvFaceIndex = flipTopBottom ? (faceIndex + 2) % 4 : faceIndex;
        if (uvFaceIndex == 0) { // Top
            minU = h;
            maxU = h + w;
        } else if (uvFaceIndex == 1) { // Left
            minU = 0;
            maxU = h;
        } else if (uvFaceIndex == 2) { // Bottom
            minU = h + w + h;
            maxU = h + w + h + w;
        } else { // Right (faceIndex == 3)
            minU = h + w;
            maxU = h + w + h;
        }

        // Indices preserved from original 'DEFAULT' logic: (i+1), (i+1), i, i
        final Vec3 posTL = destinationPoints.get((faceIndex + 1) % 4);
        final Vec3 posBL = sourcePoints.get((faceIndex + 1) % 4);
        final Vec3 posBR = sourcePoints.get(faceIndex);
        final Vec3 posTR = destinationPoints.get(faceIndex);

        renderSubdividedQuad(ms, vc, poseMatrix, origin, posTL, posBL, posBR, posTR, minU, maxU, minV, maxV, light);
    }

    private static void renderSubdividedQuad(final PoseStack ms, final VertexConsumer vc, final Matrix4f poseMatrix, final Vec3 origin,
                                             final Vec3 posTL, final Vec3 posBL, final Vec3 posBR, final Vec3 posTR,
                                             final float uLeft, final float uRight, final float minV, final float maxV, final int light) {
        // Subdividing the quad prevents AFFINE TEXTURE MAPPING artifacts (shearing/kinks)
        // when the quad is twisted (non-planar) by drawing smaller, approximate planar strips.
        final int segmentCount = 4;

        for (int s = 0; s < segmentCount; s++) {
            final float t1 = (float) s / segmentCount;
            final float t2 = (float) (s + 1) / segmentCount;

            // Interpolate V coordinates along the chain length
            final float vStart = Mth.lerp(t1, minV, maxV);
            final float vEnd = Mth.lerp(t2, minV, maxV);

            // Interpolate vertex positions between Top (Destination) and Bottom (Source)
            // t=0 is Top/Dest, t=1 is Bottom/Source
            final Vec3 p1 = posTL.lerp(posBL, t1); // Top-Left interpolated
            final Vec3 p2 = posTL.lerp(posBL, t2); // Bottom-Left interpolated
            final Vec3 p3 = posTR.lerp(posBR, t2); // Bottom-Right interpolated
            final Vec3 p4 = posTR.lerp(posBR, t1); // Top-Right interpolated

            // Render sub-quad relative to 'origin'
            addVertex(poseMatrix, ms.last(), vc, p1.subtract(origin), uLeft, vStart, light);
            addVertex(poseMatrix, ms.last(), vc, p2.subtract(origin), uLeft, vEnd, light);
            addVertex(poseMatrix, ms.last(), vc, p3.subtract(origin), uRight, vEnd, light);
            addVertex(poseMatrix, ms.last(), vc, p4.subtract(origin), uRight, vStart, light);
        }
    }

    private static List<Vec3> getEndPointsForChainJoint(final Vec3 before, final Vec3 point, final Vec3 after, final CogwheelChainType.ChainRenderInfo chainRenderInfo, final Vec3 cogwheelAxis) {
        final float radius = (float) ((chainRenderInfo.getVertexShape() == CogwheelChainType.VertexShape.CROSS ? Math.sqrt(2f) / 2f : 1f) * 1f / 16f);
        final Vec3 dirToBefore = point.subtract(before).normalize();
        final Vec3 dirToAfter = after.subtract(point).normalize();

        Vec3 averagedDir = dirToBefore.add(dirToAfter).normalize();

        final Matrix3f transform;

        if (chainRenderInfo.isDefaultDimensions()) {//TODO: make it just be is "square" dimensions
            transform = new Quaternionf()
                    .rotationTo(0, 1, 0, (float) averagedDir.x, (float) averagedDir.y, (float) averagedDir.z)
                    .get(new Matrix3f());
        } else {
            //Project averagedDir to axis if needed
            if (averagedDir.dot(cogwheelAxis) > 1e-4)
                averagedDir = averagedDir.subtract(cogwheelAxis.multiply(averagedDir)).normalize();

            //Some s****y fallback
            if (averagedDir.lengthSqr() < 1e-4)
                averagedDir = cogwheelAxis.cross(new Vec3(1, 0, 0));
            if (averagedDir.lengthSqr() < 1e-4)
                averagedDir = cogwheelAxis.cross(new Vec3(0, 1, 0));

            final Vec3 perpendicular = cogwheelAxis.cross(averagedDir);
            transform = new Matrix3f(
                    (float) perpendicular.x, (float) perpendicular.y, (float) perpendicular.z,
                    (float) averagedDir.x, (float) averagedDir.y, (float) averagedDir.z,
                    (float) cogwheelAxis.x, (float) cogwheelAxis.y, (float) cogwheelAxis.z
            );
        }


        final Vector3f localAxis1Joml = transform.transform(1f, 0f, 0f, new Vector3f());
        final Vec3 localAxis1Direction = new Vec3(localAxis1Joml.x, localAxis1Joml.y, localAxis1Joml.z).normalize();
        final Vec3 localAxis1 = localAxis1Direction.scale(chainRenderInfo.getHeight() / 2f);
        final Vector3f localAxis2Joml = transform.transform(0f, 0f, 1f, new Vector3f());
        final Vec3 localAxis2 = new Vec3(localAxis2Joml.x, localAxis2Joml.y, localAxis2Joml.z).normalize().scale(chainRenderInfo.getWidth() / 2f);

        return Stream.of(
                        point.add(localAxis1.add(localAxis2).scale(radius)),
                        point.add(localAxis1.subtract(localAxis2).scale(radius)),
                        point.add(localAxis2.scale(-1).subtract(localAxis1).scale(radius)),
                        point.add(localAxis2.subtract(localAxis1).scale(radius))
                )
                .map(e -> chainRenderInfo.getHeight() < 3 ? e.add(localAxis1Direction.scale((3f - chainRenderInfo.getHeight()) / (2 * 3 * 16))) : e)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private static void renderChainFastButWithGaps(final PoseStack ms,
                                                   final MultiBufferSource buffer,
                                                   final float offset,
                                                   final float textureSquish,
                                                   final float length,
                                                   final int light1,
                                                   final int light2,
                                                   final boolean far) {
        final float radius = far ? 1f / 16f : 1.5f / 16f;
        final float minV = far ? 0 : offset * textureSquish;
        final float maxV = far ? 1 / 16f : length * textureSquish + minV;
        final float minU = far ? 3 / 16f : 0;
        final float maxU = far ? 4 / 16f : 3 / 16f;

        ms.pushPose();
        ms.translate(0.5D, 0.0D, 0.5D);

        final VertexConsumer vc = buffer.getBuffer(RenderTypes.chain(CogwheelChainType.DEFAULT_CHAIN_TEXTURE_LOCATION));
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
