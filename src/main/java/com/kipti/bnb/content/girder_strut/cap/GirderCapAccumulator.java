package com.kipti.bnb.content.girder_strut.cap;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.girder_strut.geometry.GirderGeometry;
import com.kipti.bnb.content.girder_strut.geometry.GirderVertex;
import com.kipti.bnb.content.girder_strut.mesh.GirderMeshQuad;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GirderCapAccumulator {

    private static final float POSITION_TOLERANCE = 1.0e-4f;

    private final ResourceLocation stoneLocation;
    private final List<CapSegment> segments = new ArrayList<>();

    public GirderCapAccumulator(ResourceLocation stoneLocation) {
        this.stoneLocation = stoneLocation;
    }

    public void addSegments(TextureAtlasSprite sourceSprite, int tintIndex, boolean shade, List<GirderMeshQuad.Segment> newSegments) {
        for (GirderMeshQuad.Segment segment : newSegments) {
            CapVertex start = new CapVertex(segment.start(), sourceSprite);
            CapVertex end = new CapVertex(segment.end(), sourceSprite);
            if (GirderGeometry.positionsEqual(start.position(), end.position())) {
                continue;
            }
            CapSegment candidate = new CapSegment(start, end, tintIndex, shade);
            segments.add(candidate);
        }
    }

    public void emitCaps(Vector3f planePoint, Vector3f planeNormal, List<BakedQuad> consumer) {
        if (segments.isEmpty()) {
            return;
        }
        Vector3f normal = new Vector3f(planeNormal);
        if (normal.lengthSquared() <= GirderGeometry.EPSILON) {
            return;
        }
        normal.normalize();
        Vector3f point = new Vector3f(planePoint);

        TextureAtlasSprite stoneSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stoneLocation);

        Map<LoopKey, List<CapSegment>> grouped = new HashMap<>();
        for (CapSegment segment : segments) {
            grouped.computeIfAbsent(new LoopKey(segment.tintIndex(), segment.shade()), key -> new ArrayList<>()).add(segment);
        }

        Vector3f uAxis = buildPerpendicular(normal);
        Vector3f vAxis = new Vector3f(normal).cross(uAxis);
        if (vAxis.lengthSquared() > GirderGeometry.EPSILON) {
            vAxis.normalize();
        }

        CreateBitsnBobs.LOGGER.debug("[GirderCap] emitting {} segment groups", grouped.size());

        for (Map.Entry<LoopKey, List<CapSegment>> entry : grouped.entrySet()) {
            LoopKey key = entry.getKey();
            List<CapSegment> groupSegments = entry.getValue();
            CreateBitsnBobs.LOGGER.debug(
                "[GirderCap] group tint={} shade={} segments={}",
                key.tintIndex(),
                key.shade(),
                groupSegments.size()
            );

            Map<VertexKey, Integer> indices = new HashMap<>();
            List<Vector3f> positions = new ArrayList<>();
            List<HalfEdge> halfEdges = new ArrayList<>();
            Map<Integer, List<HalfEdge>> outgoing = new HashMap<>();

            for (CapSegment segment : groupSegments) {
                int startIndex = indexForPosition(indices, positions, segment.start().position());
                int endIndex = indexForPosition(indices, positions, segment.end().position());
                if (startIndex == endIndex) {
                    continue;
                }

                HalfEdge forward = new HalfEdge(startIndex, endIndex, segment.start().copy(), segment.end().copy());
                HalfEdge reverse = new HalfEdge(endIndex, startIndex, segment.end().copy(), segment.start().copy());
                forward.setTwin(reverse);
                reverse.setTwin(forward);

                halfEdges.add(forward);
                halfEdges.add(reverse);

                outgoing.computeIfAbsent(startIndex, keyIgnored -> new ArrayList<>()).add(forward);
                outgoing.computeIfAbsent(endIndex, keyIgnored -> new ArrayList<>()).add(reverse);
            }

            if (positions.size() < 3 || halfEdges.isEmpty()) {
                CreateBitsnBobs.LOGGER.debug("[GirderCap] group skipped - not enough data (vertices={}, halfEdges={})", positions.size(), halfEdges.size());
                continue;
            }

            for (List<HalfEdge> star : outgoing.values()) {
                for (HalfEdge edge : star) {
                    Vector3f start = positions.get(edge.startIndex());
                    Vector3f end = positions.get(edge.endIndex());
                    Vector3f delta = new Vector3f(end).sub(start);
                    float u = delta.dot(uAxis);
                    float v = delta.dot(vAxis);
                    edge.setAngle((float) Math.atan2(v, u));
                }
                star.sort(java.util.Comparator.comparingDouble(HalfEdge::angle));
            }

            List<List<CapVertex>> loops = new ArrayList<>();
            for (HalfEdge edge : halfEdges) {
                if (edge.used()) {
                    continue;
                }
                List<CapVertex> loopVertices = traceLoop(edge, outgoing);
                if (loopVertices.size() >= 3) {
                    loops.add(loopVertices);
                }
            }

            CreateBitsnBobs.LOGGER.debug(
                "[GirderCap] group produced {} loops from {} positions and {} directed edges",
                loops.size(),
                positions.size(),
                halfEdges.size()
            );

            for (List<CapVertex> loop : loops) {
                emitLoop(loop, normal, point, stoneSprite, key.tintIndex(), key.shade(), consumer);
            }
        }

        segments.clear();
    }

    private void emitLoop(
        List<CapVertex> loopVertices,
        Vector3f planeNormal,
        Vector3f planePoint,
        TextureAtlasSprite stoneSprite,
        int tintIndex,
        boolean shade,
        List<BakedQuad> consumer
    ) {
        Vector3f normalizedPlane = new Vector3f(planeNormal);
        if (normalizedPlane.lengthSquared() > GirderGeometry.EPSILON) {
            normalizedPlane.normalize();
        }
        Vector3f faceNormal = new Vector3f(normalizedPlane).negate();

        List<GirderVertex> loop = new ArrayList<>(loopVertices.size());
        for (CapVertex data : loopVertices) {
            Vector3f projectedPosition = projectOntoPlane(data.position(), normalizedPlane, planePoint);
            float remappedU = GirderGeometry.remapU(data.u(), data.sourceSprite(), stoneSprite);
            float remappedV = GirderGeometry.remapV(data.v(), data.sourceSprite(), stoneSprite);
            loop.add(new GirderVertex(
                projectedPosition,
                new Vector3f(faceNormal),
                remappedU,
                remappedV,
                data.color(),
                data.light()
            ));
        }

        List<GirderVertex> cleaned = GirderGeometry.dedupeLoopVertices(loop);
        if (cleaned.size() < 3) {
            CreateBitsnBobs.LOGGER.debug("[GirderCap] loop dropped after dedupe - insufficient vertices {}", cleaned.size());
            return;
        }

        Vector3f polygonNormal = GirderGeometry.computePolygonNormal(cleaned);
        if (polygonNormal.lengthSquared() > GirderGeometry.EPSILON && polygonNormal.dot(faceNormal) < 0f) {
            java.util.Collections.reverse(cleaned);
        }

        Direction face = Direction.getNearest(faceNormal.x, faceNormal.y, faceNormal.z);
        GirderGeometry.emitPolygon(cleaned, stoneSprite, face, tintIndex, shade, consumer);
    }

    private static Vector3f projectOntoPlane(Vector3f position, Vector3f normal, Vector3f point) {
        Vector3f projected = new Vector3f(position);
        float distance = GirderGeometry.signedDistance(projected, normal, point);
        if (Math.abs(distance) > GirderGeometry.EPSILON) {
            projected.sub(new Vector3f(normal).mul(distance));
        }
        return projected;
    }

    private static int indexForPosition(Map<VertexKey, Integer> indices, List<Vector3f> positions, Vector3f position) {
        VertexKey key = VertexKey.from(position);
        Integer existing = indices.get(key);
        if (existing != null) {
            return existing;
        }
        int next = positions.size();
        positions.add(new Vector3f(position));
        indices.put(key, next);
        return next;
    }

    private static List<CapVertex> traceLoop(HalfEdge start, Map<Integer, List<HalfEdge>> outgoing) {
        List<CapVertex> loop = new ArrayList<>();
        HalfEdge current = start;
        int guard = 0;
        while (true) {
            if (current.used()) {
                CreateBitsnBobs.LOGGER.debug("[GirderCap] aborting loop trace - encountered used edge mid trace");
                return List.of();
            }
            current.setUsed(true);
            if (current.twin() != null && !current.twin().used()) {
                current.twin().setUsed(true);
            }
            loop.add(current.startVertex().copy());

            HalfEdge next = findNextEdge(current, outgoing);
            if (next == null) {
                CreateBitsnBobs.LOGGER.debug("[GirderCap] loop trace failed - could not find next edge from vertex {}", current.endIndex());
                return List.of();
            }

            current = next;
            if (current == start) {
                break;
            }

            guard++;
            if (guard > 1024) {
                CreateBitsnBobs.LOGGER.warn("[GirderCap] aborting loop trace - guard triggered");
                return List.of();
            }
        }
        return loop;
    }

    private static HalfEdge findNextEdge(HalfEdge current, Map<Integer, List<HalfEdge>> outgoing) {
        List<HalfEdge> star = outgoing.get(current.endIndex());
        if (star == null || star.isEmpty()) {
            return null;
        }
        int twinIndex = current.twin() == null ? -1 : star.indexOf(current.twin());
        if (twinIndex < 0) {
            for (HalfEdge candidate : star) {
                if (!candidate.used()) {
                    return candidate;
                }
            }
            return null;
        }

        int size = star.size();
        for (int offset = 1; offset < size; offset++) {
            int index = (twinIndex - offset + size) % size;
            HalfEdge candidate = star.get(index);
            if (!candidate.used()) {
                return candidate;
            }
        }
        return null;
    }

    private record CapSegment(CapVertex start, CapVertex end, int tintIndex, boolean shade) {
    }

    private record LoopKey(int tintIndex, boolean shade) {
    }

    private static final class CapVertex {

        private final Vector3f position;
        private final float u;
        private final float v;
        private final int color;
        private final int light;
        private final TextureAtlasSprite sourceSprite;

        CapVertex(GirderVertex vertex, TextureAtlasSprite sprite) {
            this(new Vector3f(vertex.position()), vertex.u(), vertex.v(), vertex.color(), vertex.light(), sprite);
        }

        private CapVertex(Vector3f position, float u, float v, int color, int light, TextureAtlasSprite sourceSprite) {
            this.position = position;
            this.u = u;
            this.v = v;
            this.color = color;
            this.light = light;
            this.sourceSprite = sourceSprite;
        }

        Vector3f position() {
            return position;
        }

        float u() {
            return u;
        }

        float v() {
            return v;
        }

        int color() {
            return color;
        }

        int light() {
            return light;
        }

        TextureAtlasSprite sourceSprite() {
            return sourceSprite;
        }

        CapVertex copy() {
            return new CapVertex(new Vector3f(position), u, v, color, light, sourceSprite);
        }
    }

    private Vector3f buildPerpendicular(Vector3f normal) {
        Vector3f basis = Math.abs(normal.x) < 0.9f ? new Vector3f(1f, 0f, 0f) : new Vector3f(0f, 1f, 0f);
        Vector3f perpendicular = new Vector3f(normal).cross(basis);
        if (perpendicular.lengthSquared() <= GirderGeometry.EPSILON) {
            perpendicular = new Vector3f(normal).cross(new Vector3f(0f, 0f, 1f));
        }
        if (perpendicular.lengthSquared() > GirderGeometry.EPSILON) {
            perpendicular.normalize();
        }
        return perpendicular;
    }

    private static final class HalfEdge {

        private final int startIndex;
        private final int endIndex;
        private final CapVertex startVertex;
        private final CapVertex endVertex;
        private HalfEdge twin;
        private boolean used;
        private float angle;

        HalfEdge(int startIndex, int endIndex, CapVertex startVertex, CapVertex endVertex) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.startVertex = startVertex;
            this.endVertex = endVertex;
        }

        int startIndex() {
            return startIndex;
        }

        int endIndex() {
            return endIndex;
        }

        CapVertex startVertex() {
            return startVertex;
        }

        CapVertex endVertex() {
            return endVertex;
        }

        HalfEdge twin() {
            return twin;
        }

        void setTwin(HalfEdge twin) {
            this.twin = twin;
        }

        boolean used() {
            return used;
        }

        void setUsed(boolean used) {
            this.used = used;
        }

        float angle() {
            return angle;
        }

        void setAngle(float angle) {
            this.angle = angle;
        }
    }

    private record VertexKey(int x, int y, int z) {

        static VertexKey from(Vector3f position) {
            return new VertexKey(
                Math.round(position.x / POSITION_TOLERANCE),
                Math.round(position.y / POSITION_TOLERANCE),
                Math.round(position.z / POSITION_TOLERANCE)
            );
        }
    }
}
