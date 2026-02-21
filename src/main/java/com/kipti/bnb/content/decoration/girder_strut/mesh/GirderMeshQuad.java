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

    private GirderMeshQuad(GirderVertex[] vertices, TextureAtlasSprite sprite, Direction nominalFace, int tintIndex, boolean shade) {
        this.vertices = vertices;
        this.sprite = sprite;
        this.nominalFace = nominalFace;
        this.tintIndex = tintIndex;
        this.shade = shade;
    }

    public static GirderMeshQuad from(BakedQuad quad) {
        int[] data = quad.getVertices();
        int stride = BakedQuadHelper.VERTEX_STRIDE;
        GirderVertex[] vertices = new GirderVertex[4];
        for (int i = 0; i < 4; i++) {
            Vector3f pos = toVector3f(BakedQuadHelper.getXYZ(data, i));
            Vector3f normal = toVector3f(BakedQuadHelper.getNormalXYZ(data, i));
            float u = BakedQuadHelper.getU(data, i);
            float v = BakedQuadHelper.getV(data, i);
            int baseIndex = stride * i;
            int color = data.length > baseIndex + BakedQuadHelper.COLOR_OFFSET ? data[baseIndex + BakedQuadHelper.COLOR_OFFSET] : GirderGeometry.DEFAULT_COLOR;
            int light = data.length > baseIndex + BakedQuadHelper.LIGHT_OFFSET ? data[baseIndex + BakedQuadHelper.LIGHT_OFFSET] : GirderGeometry.DEFAULT_LIGHT;
            vertices[i] = new GirderVertex(pos, normal, u, v, color, light);
        }
        return new GirderMeshQuad(vertices, quad.getSprite(), quad.getDirection(), quad.getTintIndex(), quad.isShade());
    }

    public GirderMeshQuad translate(float dx, float dy, float dz) {
        GirderVertex[] translated = new GirderVertex[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            GirderVertex vertex = vertices[i];
            Vector3f pos = new Vector3f(vertex.position()).add(dx, dy, dz);
            translated[i] = new GirderVertex(pos, new Vector3f(vertex.normal()), vertex.u(), vertex.v(), vertex.color(), vertex.light());
        }
        return new GirderMeshQuad(translated, sprite, nominalFace, tintIndex, shade);
    }

    public GirderMeshQuad clipZ(float maxZ) {
        float minZ = Float.POSITIVE_INFINITY;
        float maxOriginalZ = Float.NEGATIVE_INFINITY;
        for (GirderVertex vertex : vertices) {
            float z = vertex.position().z;
            minZ = Math.min(minZ, z);
            maxOriginalZ = Math.max(maxOriginalZ, z);
        }
        if (maxZ >= maxOriginalZ - GirderGeometry.EPSILON) {
            return this;
        }
        if (maxZ <= minZ + GirderGeometry.EPSILON) {
            float translation = maxZ - maxOriginalZ;
            GirderVertex[] shifted = new GirderVertex[vertices.length];
            for (int i = 0; i < vertices.length; i++) {
                GirderVertex vertex = vertices[i];
                Vector3f pos = new Vector3f(vertex.position()).add(0f, 0f, translation);
                shifted[i] = new GirderVertex(pos, new Vector3f(vertex.normal()), vertex.u(), vertex.v(), vertex.color(), vertex.light());
            }
            return new GirderMeshQuad(shifted, sprite, nominalFace, tintIndex, shade);
        }
        List<GirderVertex> clipped = new ArrayList<>();

        for (int i = 0; i < vertices.length; i++) {
            GirderVertex current = vertices[i];
            GirderVertex next = vertices[(i + 1) % vertices.length];

            boolean currentInside = current.position().z <= maxZ + GirderGeometry.EPSILON;
            boolean nextInside = next.position().z <= maxZ + GirderGeometry.EPSILON;

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

    private float clampT(GirderVertex current, GirderVertex next, float maxZ) {
        float delta = next.position().z - current.position().z;
        if (Math.abs(delta) < GirderGeometry.EPSILON) {
            return 0f;
        }
        return (maxZ - current.position().z) / delta;
    }

    public void transformAndEmit(
            Matrix4f pose,
            Matrix3f normalMatrix,
            Vector3f planePoint,
            Vector3f planeNormal,
            GirderCapAccumulator capAccumulator,
            List<BakedQuad> consumer
    ) {
        List<GirderVertex> transformed = new ArrayList<>(vertices.length);
        for (GirderVertex vertex : vertices) {
            Vector3f position = new Vector3f(vertex.position());
            pose.transformPosition(position);
            Vector3f normal = new Vector3f(vertex.normal());
            normalMatrix.transform(normal);
            if (normal.lengthSquared() > GirderGeometry.EPSILON) {
                normal.normalize();
            }
            transformed.add(new GirderVertex(position, normal, vertex.u(), vertex.v(), vertex.color(), vertex.light()));
        }

        ClipResult clipResult = clipAgainstPlane(transformed, planePoint, planeNormal);
        List<GirderVertex> clipped = clipResult.polygon();
        if (clipped.size() >= 3) {
            GirderGeometry.emitPolygon(clipped, sprite, nominalFace, tintIndex, shade, consumer);
        }

        if (clipResult.clipped() && planeNormal.lengthSquared() > GirderGeometry.EPSILON) {
            capAccumulator.addSegments(sprite, tintIndex, shade, clipResult.segments());
        }
    }

    private ClipResult clipAgainstPlane(List<GirderVertex> input, Vector3f planePoint, Vector3f planeNormal) {
        if (planeNormal.lengthSquared() <= GirderGeometry.EPSILON) {
            return new ClipResult(input, List.of(), false);
        }

        List<GirderVertex> result = new ArrayList<>();
        List<Segment> segments = new ArrayList<>();
        boolean clipped = false;
        boolean hasInsideVertex = false;

        int size = input.size();
        GirderVertex previousVertex = input.get(size - 1);
        float previousDistance = GirderGeometry.signedDistance(previousVertex.position(), planeNormal, planePoint);
        boolean previousInside = previousDistance >= -GirderGeometry.EPSILON;
        if (previousInside) {
            hasInsideVertex = true;
        }

        GirderVertex pendingSegmentStart = null;

        for (GirderVertex currentVertex : input) {
            float currentDistance = GirderGeometry.signedDistance(currentVertex.position(), planeNormal, planePoint);
            boolean currentInside = currentDistance >= -GirderGeometry.EPSILON;

            if (currentInside) {
                hasInsideVertex = true;
            }

            List<GirderVertex> edgePoints = new ArrayList<>();
            if (Math.abs(previousDistance) <= GirderGeometry.EPSILON) {
                edgePoints.add(previousVertex);
            }

            if (currentInside != previousInside) {
                float t = previousDistance / (previousDistance - currentDistance);
                GirderVertex intersection = GirderGeometry.interpolate(previousVertex, currentVertex, t);
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

            for (GirderVertex edgePoint : edgePoints) {
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

    private static Vector3f toVector3f(net.minecraft.world.phys.Vec3 vec) {
        return new Vector3f((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public void transformAndEmitToConsumer(Matrix4f pose, Matrix3f normalMatrix, Vector3f planePoint, Vector3f planeNormal, GirderCapAccumulator capAccumulator, List<Consumer<BufferBuilder>> bufferConsumer, Function<Vector3f, Integer> lightFunction) {
        List<GirderVertex> transformed = new ArrayList<>(vertices.length);
        for (GirderVertex vertex : vertices) {
            Vector3f position = new Vector3f(vertex.position());
            pose.transformPosition(position);
            Vector3f normal = new Vector3f(vertex.normal());
            normalMatrix.transform(normal);
            if (normal.lengthSquared() > GirderGeometry.EPSILON) {
                normal.normalize();
            }
            transformed.add(new GirderVertex(position, normal, vertex.u(), vertex.v(), vertex.color(), vertex.light()));
        }

        ClipResult clipResult = clipAgainstPlane(transformed, planePoint, planeNormal);
        List<GirderVertex> clipped = clipResult.polygon();
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

