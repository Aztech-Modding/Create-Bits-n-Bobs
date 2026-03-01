package com.kipti.bnb.content.decoration.girder_strut.mesh;

import com.kipti.bnb.content.decoration.girder_strut.cap.GirderCapAccumulator;
import com.kipti.bnb.content.decoration.girder_strut.geometry.GirderGeometry;
import com.kipti.bnb.content.decoration.girder_strut.geometry.GirderVertex;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.simibubi.create.foundation.model.BakedQuadHelper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class GirderMeshQuad {

    private final GirderVertex[] vertices;
    private final TextureAtlasSprite sprite;
    private final Direction nominalFace;
    private final int tintIndex;
    private final boolean shade;

    private GirderMeshQuad(final GirderVertex[] vertices, final TextureAtlasSprite sprite, final Direction nominalFace, final int tintIndex, final boolean shade) {
        this.vertices = vertices;
        this.sprite = sprite;
        this.nominalFace = nominalFace;
        this.tintIndex = tintIndex;
        this.shade = shade;
    }

    public static GirderMeshQuad from(final BakedQuad quad) {
        final int[] data = quad.getVertices();
        final int stride = BakedQuadHelper.VERTEX_STRIDE;
        final GirderVertex[] vertices = new GirderVertex[4];
        for (int i = 0; i < 4; i++) {
            final Vector3f pos = toVector3f(BakedQuadHelper.getXYZ(data, i));
            final Vector3f normal = toVector3f(BakedQuadHelper.getNormalXYZ(data, i));
            final float u = BakedQuadHelper.getU(data, i);
            final float v = BakedQuadHelper.getV(data, i);
            final int baseIndex = stride * i;
            final int color = data.length > baseIndex + BakedQuadHelper.COLOR_OFFSET ? data[baseIndex + BakedQuadHelper.COLOR_OFFSET] : GirderGeometry.DEFAULT_COLOR;
            final int light = data.length > baseIndex + BakedQuadHelper.LIGHT_OFFSET ? data[baseIndex + BakedQuadHelper.LIGHT_OFFSET] : GirderGeometry.DEFAULT_LIGHT;
            vertices[i] = new GirderVertex(pos, normal, u, v, color, light);
        }
        return new GirderMeshQuad(vertices, quad.getSprite(), quad.getDirection(), quad.getTintIndex(), quad.isShade());
    }

    public GirderMeshQuad translate(final float dx, final float dy, final float dz) {
        final GirderVertex[] translated = new GirderVertex[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            final GirderVertex vertex = vertices[i];
            final Vector3f pos = new Vector3f(vertex.position()).add(dx, dy, dz);
            translated[i] = new GirderVertex(pos, new Vector3f(vertex.normal()), vertex.u(), vertex.v(), vertex.color(), vertex.light());
        }
        return new GirderMeshQuad(translated, sprite, nominalFace, tintIndex, shade);
    }

    public GirderMeshQuad clipZ(final float maxZ) {
        float minZ = Float.POSITIVE_INFINITY;
        float maxOriginalZ = Float.NEGATIVE_INFINITY;
        for (final GirderVertex vertex : vertices) {
            final float z = vertex.position().z;
            minZ = Math.min(minZ, z);
            maxOriginalZ = Math.max(maxOriginalZ, z);
        }
        if (maxZ >= maxOriginalZ - GirderGeometry.EPSILON) {
            return this;
        }
        if (maxZ <= minZ + GirderGeometry.EPSILON) {
            final float translation = maxZ - maxOriginalZ;
            final GirderVertex[] shifted = new GirderVertex[vertices.length];
            for (int i = 0; i < vertices.length; i++) {
                final GirderVertex vertex = vertices[i];
                final Vector3f pos = new Vector3f(vertex.position()).add(0f, 0f, translation);
                shifted[i] = new GirderVertex(pos, new Vector3f(vertex.normal()), vertex.u(), vertex.v(), vertex.color(), vertex.light());
            }
            return new GirderMeshQuad(shifted, sprite, nominalFace, tintIndex, shade);
        }
        final List<GirderVertex> clipped = new ArrayList<>();

        for (int i = 0; i < vertices.length; i++) {
            final GirderVertex current = vertices[i];
            final GirderVertex next = vertices[(i + 1) % vertices.length];

            final boolean currentInside = current.position().z <= maxZ + GirderGeometry.EPSILON;
            final boolean nextInside = next.position().z <= maxZ + GirderGeometry.EPSILON;

            if (currentInside && nextInside) {
                clipped.add(next);
            } else if (currentInside && !nextInside) {
                clipped.add(GirderGeometry.interpolate(current, next, clampT(current, next, maxZ)));
            } else if (!currentInside && nextInside) {
                clipped.add(GirderGeometry.interpolate(current, next, clampT(current, next, maxZ)));
                clipped.add(next);
            }
        }

        if (clipped.size() < 3) {
            return null;
        }

        return new GirderMeshQuad(clipped.toArray(new GirderVertex[0]), sprite, nominalFace, tintIndex, shade);
    }

    private float clampT(final GirderVertex current, final GirderVertex next, final float maxZ) {
        final float delta = next.position().z - current.position().z;
        if (Math.abs(delta) < GirderGeometry.EPSILON) {
            return 0f;
        }
        return (maxZ - current.position().z) / delta;
    }

    public void transformAndEmit(
            final Matrix4f pose,
            final Matrix3f normalMatrix,
            final Vector3f planePoint,
            final Vector3f planeNormal,
            final GirderCapAccumulator capAccumulator,
            final List<BakedQuad> consumer
    ) {
        final List<GirderVertex> transformed = new ArrayList<>(vertices.length);
        for (final GirderVertex vertex : vertices) {
            final Vector3f position = new Vector3f(vertex.position());
            pose.transformPosition(position);
            final Vector3f normal = new Vector3f(vertex.normal());
            normalMatrix.transform(normal);
            if (normal.lengthSquared() > GirderGeometry.EPSILON) {
                normal.normalize();
            }
            transformed.add(new GirderVertex(position, normal, vertex.u(), vertex.v(), vertex.color(), vertex.light()));
        }

        final ClipResult clipResult = clipAgainstPlane(transformed, planePoint, planeNormal);
        final List<GirderVertex> clipped = clipResult.polygon();
        if (clipped.size() >= 3) {
            GirderGeometry.emitPolygon(clipped, sprite, nominalFace, tintIndex, shade, consumer);
        }

        if (clipResult.clipped() && planeNormal.lengthSquared() > GirderGeometry.EPSILON) {
            capAccumulator.addSegments(sprite, tintIndex, shade, clipResult.segments());
        }
    }

    private ClipResult clipAgainstPlane(final List<GirderVertex> input, final Vector3f planePoint, final Vector3f planeNormal) {
        if (planeNormal.lengthSquared() <= GirderGeometry.EPSILON) {
            return new ClipResult(input, List.of(), false);
        }

        final List<GirderVertex> result = new ArrayList<>();
        final List<Segment> segments = new ArrayList<>();
        boolean clipped = false;
        boolean hasInsideVertex = false;

        final int size = input.size();
        GirderVertex previousVertex = input.get(size - 1);
        float previousDistance = GirderGeometry.signedDistance(previousVertex.position(), planeNormal, planePoint);
        boolean previousInside = previousDistance >= -GirderGeometry.EPSILON;
        if (previousInside) {
            hasInsideVertex = true;
        }

        GirderVertex pendingSegmentStart = null;

        for (final GirderVertex currentVertex : input) {
            final float currentDistance = GirderGeometry.signedDistance(currentVertex.position(), planeNormal, planePoint);
            final boolean currentInside = currentDistance >= -GirderGeometry.EPSILON;

            if (currentInside) {
                hasInsideVertex = true;
            }

            final List<GirderVertex> edgePoints = new ArrayList<>();
            if (Math.abs(previousDistance) <= GirderGeometry.EPSILON) {
                edgePoints.add(previousVertex);
            }

            if (currentInside != previousInside) {
                final float t = previousDistance / (previousDistance - currentDistance);
                final GirderVertex intersection = GirderGeometry.interpolate(previousVertex, currentVertex, t);
                result.add(intersection);
                edgePoints.add(intersection);
                clipped = true;
            }

            if (currentInside) {
                result.add(currentVertex);
            }

            if (Math.abs(currentDistance) <= GirderGeometry.EPSILON) {
                edgePoints.add(currentVertex);
            }

            if (!currentInside) {
                clipped = true;
            }

            for (final GirderVertex edgePoint : edgePoints) {
                if (pendingSegmentStart == null) {
                    pendingSegmentStart = edgePoint;
                } else if (!GirderGeometry.positionsEqual(pendingSegmentStart.position(), edgePoint.position())) {
                    segments.add(new Segment(pendingSegmentStart, edgePoint));
                    pendingSegmentStart = null;
                }
            }

            previousVertex = currentVertex;
            previousDistance = currentDistance;
            previousInside = currentInside;
        }

        return new ClipResult(result, segments, clipped);
    }

    private static Vector3f toVector3f(final net.minecraft.world.phys.Vec3 vec) {
        return new Vector3f((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public void transformAndEmitToConsumer(final Matrix4f pose, final Matrix3f normalMatrix, final Vector3f planePoint, final Vector3f planeNormal, final GirderCapAccumulator capAccumulator, final List<Consumer<BufferBuilder>> bufferConsumer, final Function<Vector3f, Integer> lightFunction) {
        final List<GirderVertex> transformed = new ArrayList<>(vertices.length);
        for (final GirderVertex vertex : vertices) {
            final Vector3f position = new Vector3f(vertex.position());
            pose.transformPosition(position);
            final Vector3f normal = new Vector3f(vertex.normal());
            normalMatrix.transform(normal);
            if (normal.lengthSquared() > GirderGeometry.EPSILON) {
                normal.normalize();
            }
            transformed.add(new GirderVertex(position, normal, vertex.u(), vertex.v(), vertex.color(), vertex.light()));
        }

        final ClipResult clipResult = clipAgainstPlane(transformed, planePoint, planeNormal);
        final List<GirderVertex> clipped = clipResult.polygon();
        if (clipped.size() >= 3) {
            GirderGeometry.emitPolygonToConsumer(clipped, bufferConsumer, lightFunction);
        }

        if (clipResult.clipped() && planeNormal.lengthSquared() > GirderGeometry.EPSILON) {
            capAccumulator.addSegments(sprite, tintIndex, shade, clipResult.segments());
        }
    }

    private record ClipResult(List<GirderVertex> polygon, List<Segment> segments, boolean clipped) {
    }

    public record Segment(GirderVertex start, GirderVertex end) {
    }
}

