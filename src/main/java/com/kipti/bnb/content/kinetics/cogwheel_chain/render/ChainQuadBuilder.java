package com.kipti.bnb.content.kinetics.cogwheel_chain.render;

import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Shared chain quad geometry builder used by both the traditional renderer ({@code CogwheelChainBehaviourRenderer})
 * and the Flywheel instanced visual ({@code CogwheelChainBehaviourVisual}).
 * <p>
 * Computes per-face quad corners, UVs, and subdivision, then emits vertices via a {@link VertexEmitter} callback
 * so each rendering backend can handle vertex output in its own way.
 */
public final class ChainQuadBuilder {

    public static final int SUBDIVISION_COUNT = 4;

    /**
     * Callback for emitting a single vertex. Implementations adapt this to their specific vertex format
     * (e.g. {@code VertexConsumer} for the traditional renderer, or a vertex list for the Flywheel mesh).
     */
    @FunctionalInterface
    public interface VertexEmitter {
        void emit(float x, float y, float z, float u, float v, float nx, float ny, float nz);
    }

    private ChainQuadBuilder() {
    }

    /**
     * Build and emit all 4 faces for a single chain segment, handling both CROSS and SQUARE vertex shapes,
     * UV mapping, and subdivision to avoid affine texture warping.
     *
     * @param destinationPoints 4 corner points at the destination end of the segment (already reordered)
     * @param sourcePoints      4 corner points at the source end of the segment
     * @param chainRenderInfo   render shape/dimension info for the chain type
     * @param minV              V texture coordinate at the source end
     * @param maxV              V texture coordinate at the destination end
     * @param flipInsideOutside whether to flip UV face indices for consistent inside/outside
     * @param emitter           callback to emit each vertex
     */
    public static void buildSegmentFaces(final List<Vec3> destinationPoints,
                                         final List<Vec3> sourcePoints,
                                         final CogwheelChainType.ChainRenderInfo chainRenderInfo,
                                         final float minV,
                                         final float maxV,
                                         final boolean flipInsideOutside,
                                         final VertexEmitter emitter) {
        // CROSS shapes only need 2 faces (one per cross plane). Faces 0&2 and 1&3 are the
        // same plane with reversed winding; emitting all 4 with backface culling off causes
        // each pixel to be rendered 4x, producing severe self-z-fighting.
        final int faceCount = chainRenderInfo.getVertexShape() == CogwheelChainType.VertexShape.CROSS ? 2 : 4;
        for (int faceIndex = 0; faceIndex < faceCount; faceIndex++) {
            if (chainRenderInfo.getVertexShape() == CogwheelChainType.VertexShape.CROSS) {
                buildCrossShapeFace(destinationPoints, sourcePoints, faceIndex, minV, maxV, emitter);
            } else {
                buildDefaultShapeFace(destinationPoints, sourcePoints, chainRenderInfo, faceIndex, minV, maxV, flipInsideOutside, emitter);
            }
        }
    }

    private static void buildCrossShapeFace(final List<Vec3> destinationPoints,
                                            final List<Vec3> sourcePoints,
                                            final int faceIndex,
                                            final float minV,
                                            final float maxV,
                                            final VertexEmitter emitter) {
        final float uOffset = (faceIndex % 2 == 1) ? 0 : 3 / 16f;
        final float uWidth = 3 / 16f;

        final float uLeft = uOffset;
        final float uRight = uWidth + uOffset;

        // Indices preserved from original 'CROSS' logic: (i+2), (i+2), i, i
        final Vec3 posTL = destinationPoints.get((faceIndex + 2) % 4);
        final Vec3 posBL = sourcePoints.get((faceIndex + 2) % 4);
        final Vec3 posBR = sourcePoints.get(faceIndex);
        final Vec3 posTR = destinationPoints.get(faceIndex);

        buildSubdividedQuad(posTL, posBL, posBR, posTR, uLeft, uRight, minV, maxV, emitter);
    }

    private static void buildDefaultShapeFace(final List<Vec3> destinationPoints,
                                              final List<Vec3> sourcePoints,
                                              final CogwheelChainType.ChainRenderInfo renderInfo,
                                              final int faceIndex,
                                              final float minV,
                                              final float maxV,
                                              final boolean flipTopBottom,
                                              final VertexEmitter emitter) {
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

        buildSubdividedQuad(posTL, posBL, posBR, posTR, minU, maxU, minV, maxV, emitter);
    }

    /**
     * Subdivides a quad into {@link #SUBDIVISION_COUNT} strips to prevent affine texture mapping artifacts
     * when the quad is twisted (non-planar).
     */
    private static void buildSubdividedQuad(final Vec3 posTL,
                                            final Vec3 posBL,
                                            final Vec3 posBR,
                                            final Vec3 posTR,
                                            final float uLeft,
                                            final float uRight,
                                            final float minV,
                                            final float maxV,
                                            final VertexEmitter emitter) {
        for (int s = 0; s < SUBDIVISION_COUNT; s++) {
            final float t1 = (float) s / SUBDIVISION_COUNT;
            final float t2 = (float) (s + 1) / SUBDIVISION_COUNT;

            final float vStart = Mth.lerp(t1, minV, maxV);
            final float vEnd = Mth.lerp(t2, minV, maxV);

            final Vec3 p1 = posTL.lerp(posBL, t1);
            final Vec3 p2 = posTL.lerp(posBL, t2);
            final Vec3 p3 = posTR.lerp(posBR, t2);
            final Vec3 p4 = posTR.lerp(posBR, t1);

            // Compute face normal from the sub-quad edges
            final Vec3 edgeA = p2.subtract(p1);
            final Vec3 edgeB = p4.subtract(p1);
            Vec3 normal = edgeA.cross(edgeB);
            if (normal.lengthSqr() < 1e-7) {
                normal = new Vec3(0, 1, 0);
            } else {
                normal = normal.normalize();
            }

            final float nx = (float) normal.x;
            final float ny = (float) normal.y;
            final float nz = (float) normal.z;

            emitter.emit((float) p1.x, (float) p1.y, (float) p1.z, uLeft, vStart, nx, ny, nz);
            emitter.emit((float) p2.x, (float) p2.y, (float) p2.z, uLeft, vEnd, nx, ny, nz);
            emitter.emit((float) p3.x, (float) p3.y, (float) p3.z, uRight, vEnd, nx, ny, nz);
            emitter.emit((float) p4.x, (float) p4.y, (float) p4.z, uRight, vStart, nx, ny, nz);
        }
    }
}
