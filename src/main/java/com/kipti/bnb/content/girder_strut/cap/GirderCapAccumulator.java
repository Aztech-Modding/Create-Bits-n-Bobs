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
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        List<CapLoop> loops = buildLoops(planePoint, planeNormal);
        if (loops.isEmpty()) {
            return;
        }

        TextureAtlasSprite stoneSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stoneLocation);

        for (CapLoop loop : loops) {
            emitLoop(loop.vertices(), planeNormal, planePoint, stoneSprite, loop.key().tintIndex(), loop.key().shade(), consumer);
        }

        segments.clear();
    }

    List<CapLoop> buildLoops(Vector3f planePoint, Vector3f planeNormal) {
        if (segments.isEmpty()) {
            return List.of();
        }

        Vector3f normal = new Vector3f(planeNormal);
        if (normal.lengthSquared() <= GirderGeometry.EPSILON) {
            return List.of();
        }
        normal.normalize();

        Vector3f point = new Vector3f(planePoint);
        Vector3f uAxis = buildPerpendicular(normal);
        Vector3f vAxis = new Vector3f(normal).cross(uAxis);
        if (vAxis.lengthSquared() > GirderGeometry.EPSILON) {
            vAxis.normalize();
        }

        Map<LoopKey, List<CapSegment>> grouped = new HashMap<>();
        for (CapSegment segment : segments) {
            grouped.computeIfAbsent(new LoopKey(segment.tintIndex(), segment.shade()), key -> new ArrayList<>()).add(segment);
        }

        CreateBitsnBobs.LOGGER.debug("[GirderCap] building loops: {} segment groups", grouped.size());

        List<CapLoop> loops = new ArrayList<>();
        for (Map.Entry<LoopKey, List<CapSegment>> entry : grouped.entrySet()) {
            LoopKey key = entry.getKey();
            List<CapSegment> groupSegments = entry.getValue();
            CreateBitsnBobs.LOGGER.debug(
                "[GirderCap] tracing group tint={} shade={} segments={}",
                key.tintIndex(),
                key.shade(),
                groupSegments.size()
            );

            List<CapEdge> edges = new ArrayList<>();
            Map<VertexKey, List<CapEdge>> outgoing = new HashMap<>();
            Map<EdgeKey, ArrayDeque<CapEdge>> reverseBuckets = new HashMap<>();

            for (CapSegment segment : groupSegments) {
                CapVertex start = segment.start().copy();
                CapVertex end = segment.end().copy();
                if (GirderGeometry.positionsEqual(start.position(), end.position())) {
                    continue;
                }

                Vector2f startUv = planarCoordinates(start.position(), point, uAxis, vAxis);
                Vector2f endUv = planarCoordinates(end.position(), point, uAxis, vAxis);
                Vector2f delta = new Vector2f(endUv).sub(startUv);
                if (delta.lengthSquared() <= GirderGeometry.EPSILON) {
                    continue;
                }

                float angle = (float) Math.atan2(delta.y, delta.x);
                CapEdge edge = new CapEdge(start, end, angle);
                edges.add(edge);

                VertexKey startKey = VertexKey.from(start.position());
                outgoing.computeIfAbsent(startKey, keyIgnored -> new ArrayList<>()).add(edge);

                EdgeKey forwardKey = new EdgeKey(startKey, VertexKey.from(end.position()));
                EdgeKey reverseKey = forwardKey.reverse();
                ArrayDeque<CapEdge> reverse = reverseBuckets.get(reverseKey);
                if (reverse != null) {
                    CapEdge twin = reverse.poll();
                    if (twin != null) {
                        edge.setTwin(twin);
                        twin.setTwin(edge);
                    }
                    if (reverse.isEmpty()) {
                        reverseBuckets.remove(reverseKey);
                    }
                }
                reverseBuckets.computeIfAbsent(forwardKey, ignored -> new ArrayDeque<>()).add(edge);
            }

            for (List<CapEdge> star : outgoing.values()) {
                star.sort(Comparator.comparingDouble(CapEdge::angle));
            }

            int loopCount = 0;
            for (CapEdge edge : edges) {
                if (edge.used()) {
                    continue;
                }
                List<CapVertex> traced = traceLoop(edge, outgoing);
                if (traced.size() >= 3) {
                    loops.add(new CapLoop(key, traced));
                    loopCount++;
                }
            }

            CreateBitsnBobs.LOGGER.debug(
                "[GirderCap] traced {} loops for tint={} shade={} (edges={})",
                loopCount,
                key.tintIndex(),
                key.shade(),
                edges.size()
            );
        }

        return Collections.unmodifiableList(loops);
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

    private static List<CapVertex> traceLoop(CapEdge start, Map<VertexKey, List<CapEdge>> outgoing) {
        List<CapVertex> loop = new ArrayList<>();
        CapEdge current = start;
        int guard = 0;
        while (true) {
            if (current.used()) {
                CreateBitsnBobs.LOGGER.debug("[GirderCap] aborting loop trace - encountered used edge mid trace");
                return List.of();
            }
            current.setUsed(true);
            loop.add(current.start().copy());

            CapEdge next = findNextEdge(current, outgoing, start);
            if (next == null) {
                CreateBitsnBobs.LOGGER.debug("[GirderCap] loop trace failed - no exit from vertex {}", VertexKey.from(current.end().position()));
                return List.of();
            }

            if (next == start) {
                break;
            }

            current = next;
            guard++;
            if (guard > 4096) {
                CreateBitsnBobs.LOGGER.warn("[GirderCap] aborting loop trace - guard triggered");
                return List.of();
            }
        }
        return loop;
    }

    private static CapEdge findNextEdge(CapEdge current, Map<VertexKey, List<CapEdge>> outgoing, CapEdge start) {
        VertexKey endKey = VertexKey.from(current.end().position());
        List<CapEdge> star = outgoing.get(endKey);
        if (star == null || star.isEmpty()) {
            return null;
        }

        int twinIndex = current.twin() == null ? -1 : star.indexOf(current.twin());
        if (twinIndex >= 0) {
            int size = star.size();
            for (int offset = 1; offset <= size; offset++) {
                int index = (twinIndex - offset + size) % size;
                CapEdge candidate = star.get(index);
                if (!candidate.used() || candidate == start) {
                    return candidate;
                }
            }
        }

        for (CapEdge candidate : star) {
            if (!candidate.used() || candidate == start) {
                return candidate;
            }
        }

        return null;
    }

    private record CapSegment(CapVertex start, CapVertex end, int tintIndex, boolean shade) {
    }

    record CapLoop(LoopKey key, List<CapVertex> vertices) {
    }

    private record LoopKey(int tintIndex, boolean shade) {
    }

    static final class CapVertex {

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

    private record VertexKey(int x, int y, int z) {

        static VertexKey from(Vector3f position) {
            return new VertexKey(
                Math.round(position.x / POSITION_TOLERANCE),
                Math.round(position.y / POSITION_TOLERANCE),
                Math.round(position.z / POSITION_TOLERANCE)
            );
        }
    }

    private static final class CapEdge {

        private final CapVertex start;
        private final CapVertex end;
        private final float angle;
        private CapEdge twin;
        private boolean used;

        CapEdge(CapVertex start, CapVertex end, float angle) {
            this.start = start;
            this.end = end;
            this.angle = angle;
        }

        CapVertex start() {
            return start;
        }

        CapVertex end() {
            return end;
        }

        float angle() {
            return angle;
        }

        boolean used() {
            return used;
        }

        void setUsed(boolean used) {
            this.used = used;
        }

        CapEdge twin() {
            return twin;
        }

        void setTwin(CapEdge twin) {
            this.twin = twin;
        }
    }

    private record EdgeKey(VertexKey start, VertexKey end) {

        EdgeKey reverse() {
            return new EdgeKey(end, start);
        }
    }

    private static Vector2f planarCoordinates(Vector3f position, Vector3f planePoint, Vector3f uAxis, Vector3f vAxis) {
        Vector3f relative = new Vector3f(position).sub(planePoint);
        return new Vector2f(relative.dot(uAxis), relative.dot(vAxis));
    }
}
