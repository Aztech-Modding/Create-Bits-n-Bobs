package com.kipti.bnb.content.girder_strut.cap;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.girder_strut.geometry.GirderGeometry;
import com.kipti.bnb.content.girder_strut.geometry.GirderVertex;
import com.kipti.bnb.content.girder_strut.mesh.GirderMeshQuad;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class GirderCapAccumulator {

    private static final float POSITION_TOLERANCE = 1.0e-4f;
    private static final boolean DEBUG_OUTLINES = true;
    private static final AtomicInteger DEBUG_SEQUENCE = new AtomicInteger();
    private static final String DEBUG_KEY_ROOT = "girderCap";
    private static final Color SEGMENT_COLOR = new Color(245, 168, 66);
    private static final Color LOOP_COLOR = new Color(201, 86, 228);
    private static final Color PROJECTED_COLOR = new Color(86, 203, 228);
    private static final Color FINAL_COLOR = new Color(114, 228, 86);
    private static final Color PLANE_NORMAL_COLOR = new Color(255, 255, 255);

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
        int debugId = 0;
        if (DEBUG_OUTLINES) {
            debugId = DEBUG_SEQUENCE.incrementAndGet();
            debugPlane(debugId, planePoint, planeNormal);
            debugSegments(debugId);
        }

        List<CapLoop> loops = buildLoops(planePoint, planeNormal);
        if (DEBUG_OUTLINES) {
            debugLoopInputs(debugId, loops);
        }
        if (loops.isEmpty()) {
            return;
        }

        TextureAtlasSprite stoneSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stoneLocation);

        for (int i = 0; i < loops.size(); i++) {
            CapLoop loop = loops.get(i);
            emitLoop(loop.vertices(), planeNormal, planePoint, stoneSprite, loop.key().tintIndex(), loop.key().shade(), consumer, debugId, i);
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

                VertexKey startKey = VertexKey.from(start.position());
                VertexKey endKey = VertexKey.from(end.position());

                float forwardAngle = (float) Math.atan2(delta.y, delta.x);
                float reverseAngle = (float) Math.atan2(-delta.y, -delta.x);

                CapEdge forward = new CapEdge(start, end, startKey, endKey, forwardAngle);
                CapEdge backward = new CapEdge(end.copy(), start.copy(), endKey, startKey, reverseAngle);
                forward.setTwin(backward);
                backward.setTwin(forward);

                edges.add(forward);

                registerEdge(outgoing, forward);
                registerEdge(outgoing, backward);
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

        List<CapLoop> deduped = dedupeLoops(loops);
        if (deduped.size() != loops.size()) {
            CreateBitsnBobs.LOGGER.debug(
                "[GirderCap] removed {} duplicate loops", loops.size() - deduped.size()
            );
        }
        return Collections.unmodifiableList(deduped);
    }

    private void emitLoop(
        List<CapVertex> loopVertices,
        Vector3f planeNormal,
        Vector3f planePoint,
        TextureAtlasSprite stoneSprite,
        int tintIndex,
        boolean shade,
        List<BakedQuad> consumer,
        int debugId,
        int loopIndex
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

        if (DEBUG_OUTLINES) {
            debugProjectedLoop(debugId, loopIndex, loop, PROJECTED_COLOR, 1f / 64f, "projected");
        }

        List<GirderVertex> cleaned = GirderGeometry.dedupeLoopVertices(loop);
        if (cleaned.size() < 3) {
            CreateBitsnBobs.LOGGER.debug("[GirderCap] loop dropped after dedupe - insufficient vertices {}", cleaned.size());
            if (DEBUG_OUTLINES) {
                debugProjectedLoop(debugId, loopIndex, cleaned, FINAL_COLOR, 1f / 96f, "final");
            }
            return;
        }

        if (DEBUG_OUTLINES) {
            debugProjectedLoop(debugId, loopIndex, cleaned, FINAL_COLOR, 1f / 96f, "final");
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

    private static void registerEdge(Map<VertexKey, List<CapEdge>> outgoing, CapEdge edge) {
        outgoing.computeIfAbsent(edge.startKey(), keyIgnored -> new ArrayList<>()).add(edge);
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
        VertexKey endKey = current.endKey();
        List<CapEdge> star = outgoing.get(endKey);
        if (star == null || star.isEmpty()) {
            return null;
        }

        float baseAngle = current.twin() != null ? current.twin().normalizedAngle() : CapEdge.normalizeAngle(current.angle() + (float) Math.PI);

        CapEdge best = selectNextEdge(star, baseAngle, start);
        if (best != null) {
            return best;
        }

        // If every unused edge lies exactly on the base direction we may have a degenerate wedge.
        // Fall back to any available edge to avoid abandoning the loop outright.
        for (CapEdge candidate : star) {
            if (!candidate.used() || candidate == start) {
                return candidate;
            }
        }

        return null;
    }

    private static List<CapLoop> dedupeLoops(List<CapLoop> loops) {
        Map<LoopSignature, CapLoop> unique = new LinkedHashMap<>();
        for (CapLoop loop : loops) {
            LoopSignature signature = LoopSignature.from(loop);
            unique.putIfAbsent(signature, loop);
        }
        return new ArrayList<>(unique.values());
    }

    private static CapEdge selectNextEdge(List<CapEdge> star, float baseAngle, CapEdge start) {
        CapEdge best = null;
        float bestDelta = Float.POSITIVE_INFINITY;
        for (CapEdge candidate : star) {
            if (candidate.used() && candidate != start) {
                continue;
            }
            float delta = angleDelta(baseAngle, candidate.normalizedAngle());
            if (delta < bestDelta - GirderGeometry.EPSILON) {
                bestDelta = delta;
                best = candidate;
            }
        }
        return best;
    }

    private static float angleDelta(float from, float to) {
        float delta = to - from;
        while (delta <= 0f) {
            delta += (float) (Math.PI * 2.0);
        }
        return delta;
    }

    private void debugPlane(int debugId, Vector3f planePoint, Vector3f planeNormal) {
        if (!DEBUG_OUTLINES) {
            return;
        }
        Vector3f normal = new Vector3f(planeNormal);
        if (normal.lengthSquared() <= GirderGeometry.EPSILON) {
            return;
        }
        normal.normalize();
        Vector3f start = new Vector3f(planePoint);
        Vector3f end = new Vector3f(planePoint).add(new Vector3f(normal).mul(0.5f));
        showLine(debugKey(debugId, "plane", 0, 0), start, end, PLANE_NORMAL_COLOR, 1f / 32f);
    }

    private void debugSegments(int debugId) {
        if (!DEBUG_OUTLINES || segments.isEmpty()) {
            return;
        }
        int index = 0;
        for (CapSegment segment : segments) {
            Vector3f start = segment.start().position();
            Vector3f end = segment.end().position();
            if (GirderGeometry.positionsEqual(start, end)) {
                continue;
            }
            showLine(debugKey(debugId, "segments", index, 0), start, end, SEGMENT_COLOR, 1f / 24f);
            index++;
        }
    }

    private void debugLoopInputs(int debugId, List<CapLoop> loops) {
        if (!DEBUG_OUTLINES || loops.isEmpty()) {
            return;
        }
        for (int i = 0; i < loops.size(); i++) {
            CapLoop loop = loops.get(i);
            List<Vector3f> positions = new ArrayList<>(loop.vertices().size());
            for (CapVertex vertex : loop.vertices()) {
                positions.add(new Vector3f(vertex.position()));
            }
            showPolyline(debugId, "loops", i, positions, LOOP_COLOR, 1f / 48f, true);
        }
    }

    private void debugProjectedLoop(int debugId, int loopIndex, List<GirderVertex> vertices, Color color, float width, String stage) {
        if (!DEBUG_OUTLINES || vertices.isEmpty()) {
            return;
        }
        List<Vector3f> positions = new ArrayList<>(vertices.size());
        for (GirderVertex vertex : vertices) {
            positions.add(new Vector3f(vertex.position()));
        }
        showPolyline(debugId, stage, loopIndex, positions, color, width, true);
    }

    private static void showPolyline(int debugId, String stage, int groupIndex, List<Vector3f> positions, Color color, float width, boolean closed) {
        if (positions.size() < 2) {
            return;
        }
        int segmentIndex = 0;
        for (int i = 0; i < positions.size() - 1; i++) {
            Vector3f start = positions.get(i);
            Vector3f end = positions.get(i + 1);
            if (GirderGeometry.positionsEqual(start, end)) {
                continue;
            }
            showLine(debugKey(debugId, stage, groupIndex, segmentIndex++), start, end, color, width);
        }
        if (closed) {
            Vector3f start = positions.get(positions.size() - 1);
            Vector3f end = positions.get(0);
            if (!GirderGeometry.positionsEqual(start, end)) {
                showLine(debugKey(debugId, stage, groupIndex, segmentIndex), start, end, color, width);
            }
        }
    }

    private static void showLine(String key, Vector3f from, Vector3f to, Color color, float width) {
        GirderCapDebugOutlines.queueLine(key, from, to, color, width);
    }

    private static String debugKey(int debugId, String stage, int groupIndex, int segmentIndex) {
        return DEBUG_KEY_ROOT + '/' + debugId + '/' + stage + '/' + groupIndex + '/' + segmentIndex;
    }

    private record CapSegment(CapVertex start, CapVertex end, int tintIndex, boolean shade) {
    }

    record CapLoop(LoopKey key, List<CapVertex> vertices) {
    }

    private record LoopKey(int tintIndex, boolean shade) {
    }

    private record LoopSignature(LoopKey key, String geometry) {

        static LoopSignature from(CapLoop loop) {
            return new LoopSignature(loop.key(), canonicalGeometry(loop));
        }

        private static String canonicalGeometry(CapLoop loop) {
            List<VertexKey> keys = new ArrayList<>(loop.vertices().size());
            for (CapVertex vertex : loop.vertices()) {
                keys.add(VertexKey.from(vertex.position()));
            }
            String forward = canonicalOrientation(keys);
            List<VertexKey> reversed = new ArrayList<>(keys);
            java.util.Collections.reverse(reversed);
            String backward = canonicalOrientation(reversed);
            return forward.compareTo(backward) <= 0 ? forward : backward;
        }

        private static String canonicalOrientation(List<VertexKey> keys) {
            if (keys.isEmpty()) {
                return "";
            }
            int size = keys.size();
            String best = null;
            for (int offset = 0; offset < size; offset++) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < size; i++) {
                    VertexKey key = keys.get((offset + i) % size);
                    builder.append(key.x()).append(',').append(key.y()).append(',').append(key.z()).append(';');
                }
                String candidate = builder.toString();
                if (best == null || candidate.compareTo(best) < 0) {
                    best = candidate;
                }
            }
            return best;
        }
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
        private final VertexKey startKey;
        private final VertexKey endKey;
        private final float angle;
        private final float normalizedAngle;
        private CapEdge twin;
        private boolean used;

        CapEdge(CapVertex start, CapVertex end, VertexKey startKey, VertexKey endKey, float angle) {
            this.start = start;
            this.end = end;
            this.startKey = startKey;
            this.endKey = endKey;
            this.angle = angle;
            this.normalizedAngle = normalizeAngle(angle);
        }

        CapVertex start() {
            return start;
        }

        CapVertex end() {
            return end;
        }

        VertexKey startKey() {
            return startKey;
        }

        VertexKey endKey() {
            return endKey;
        }

        float angle() {
            return angle;
        }

        float normalizedAngle() {
            return normalizedAngle;
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

        static float normalizeAngle(float angle) {
            float normalized = angle % (float) (Math.PI * 2.0);
            if (normalized < 0f) {
                normalized += (float) (Math.PI * 2.0);
            }
            return normalized;
        }
    }

    private static Vector2f planarCoordinates(Vector3f position, Vector3f planePoint, Vector3f uAxis, Vector3f vAxis) {
        Vector3f relative = new Vector3f(position).sub(planePoint);
        return new Vector2f(relative.dot(uAxis), relative.dot(vAxis));
    }
}
