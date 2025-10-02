package com.kipti.bnb.content.girder_strut.cap;

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
import java.util.List;

public final class GirderCapAccumulator {

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

        java.util.Map<LoopKey, List<CapSegment>> grouped = new java.util.HashMap<>();
        for (CapSegment segment : segments) {
            grouped.computeIfAbsent(new LoopKey(segment.tintIndex(), segment.shade()), key -> new ArrayList<>()).add(segment);
        }

        for (java.util.Map.Entry<LoopKey, List<CapSegment>> entry : grouped.entrySet()) {
            List<CapVertex> uniqueVertices = new ArrayList<>();
            List<Edge> edges = new ArrayList<>();
            LoopKey key = entry.getKey();

            for (CapSegment segment : entry.getValue()) {
                int startIndex = indexFor(uniqueVertices, segment.start());
                int endIndex = indexFor(uniqueVertices, segment.end());
                if (startIndex == endIndex) {
                    continue;
                }
                edges.add(new Edge(startIndex, endIndex));
            }

            if (uniqueVertices.size() < 3 || edges.isEmpty()) {
                continue;
            }

            List<List<Integer>> components = buildComponents(uniqueVertices.size(), edges);
            for (List<Integer> component : components) {
                if (component.size() < 3) {
                    continue;
                }
                List<Integer> ordered = orderComponent(component, uniqueVertices, normal);
                if (ordered.size() < 3) {
                    continue;
                }
                emitLoop(ordered, uniqueVertices, key.tintIndex(), key.shade(), normal, point, stoneSprite, consumer);
            }
        }

        segments.clear();
    }

    private int indexFor(List<CapVertex> vertices, CapVertex vertex) {
        for (int i = 0; i < vertices.size(); i++) {
            CapVertex existing = vertices.get(i);
            if (!positionsClose(existing.position(), vertex.position())) {
                continue;
            }
            if (!attributesMatch(existing, vertex)) {
                // The geometry can contain seams where two quads share the same
                // position but use different texture coordinates (or lighting).
                // Treat these as distinct vertices so that both sides emit a cap.
                continue;
            }
            return i;
        }
        vertices.add(vertex.copy());
        return vertices.size() - 1;
    }

    /**
     * Compare two positions using a tolerance that is tight enough to keep distinct
     * features (like seams) separate while still swallowing minor floating point error.
     */
    private static boolean positionsClose(org.joml.Vector3f a, org.joml.Vector3f b) {
        float dx = a.x - b.x;
        float dy = a.y - b.y;
        float dz = a.z - b.z;
        // Use a tolerance that is high enough to cope with floating point
        // rounding errors but small enough to keep distinct features apart.
        float tol = 1.0e-4f;
        return dx * dx + dy * dy + dz * dz <= tol * tol;
    }

    private static boolean attributesMatch(CapVertex a, CapVertex b) {
        if (a.color() != b.color() || a.light() != b.light()) {
            return false;
        }
        if (a.sourceSprite() != b.sourceSprite()) {
            return false;
        }
        float tol = 1.0e-4f;
        if (Math.abs(a.u() - b.u()) > tol) {
            return false;
        }
        if (Math.abs(a.v() - b.v()) > tol) {
            return false;
        }
        return true;
    }

    private void emitLoop(
        List<Integer> loopIndices,
        List<CapVertex> vertices,
        int tintIndex,
        boolean shade,
        Vector3f planeNormal,
        Vector3f planePoint,
        TextureAtlasSprite stoneSprite,
        List<BakedQuad> consumer
    ) {
        // Use the cut-facing normal (flip the supplied plane normal) so the cap
        // quads face into the cut, not towards the surface.
        Vector3f normalizedPlane = new Vector3f(planeNormal);
        if (normalizedPlane.lengthSquared() > GirderGeometry.EPSILON) {
            normalizedPlane.normalize();
        }
        Vector3f faceNormal = new Vector3f(normalizedPlane).negate();

        List<GirderVertex> loopVertices = new ArrayList<>(loopIndices.size());
        for (int index : loopIndices) {
            CapVertex data = vertices.get(index);
            // Project the vertex onto the clipping plane
            Vector3f projectedPosition = new Vector3f(data.position());
            float distance = GirderGeometry.signedDistance(projectedPosition, normalizedPlane, planePoint);
            if (Math.abs(distance) > GirderGeometry.EPSILON) {
                projectedPosition.sub(new Vector3f(normalizedPlane).mul(distance));
            }
            
            // Use proper UV mapping based on position
            // Create a coordinate system on the plane for UV mapping
            float remappedU = GirderGeometry.remapU(data.u(), data.sourceSprite(), stoneSprite);
            float remappedV = GirderGeometry.remapV(data.v(), data.sourceSprite(), stoneSprite);
            
            loopVertices.add(new GirderVertex(
                projectedPosition,
                new Vector3f(faceNormal),
                remappedU,
                remappedV,
                data.color(),
                data.light()
            ));
        }

        List<GirderVertex> cleaned = GirderGeometry.dedupeLoopVertices(loopVertices);
        if (cleaned.size() < 3) {
            return;
        }

        // Check winding order and reverse if needed
        Vector3f polygonNormal = GirderGeometry.computePolygonNormal(cleaned);
        if (polygonNormal.lengthSquared() > GirderGeometry.EPSILON && polygonNormal.dot(faceNormal) < 0f) {
            java.util.Collections.reverse(cleaned);
        }

        Direction face = Direction.getNearest(faceNormal.x, faceNormal.y, faceNormal.z);
        GirderGeometry.emitPolygon(cleaned, stoneSprite, face, tintIndex, shade, consumer);
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

    private record Edge(int start, int end) {
    }

    private List<List<Integer>> buildComponents(int vertexCount, List<Edge> edges) {
        java.util.Map<Integer, List<Integer>> adjacency = new java.util.HashMap<>();
        java.util.Set<Integer> relevant = new java.util.HashSet<>();
        for (Edge edge : edges) {
            adjacency.computeIfAbsent(edge.start(), key -> new ArrayList<>()).add(edge.end());
            adjacency.computeIfAbsent(edge.end(), key -> new ArrayList<>()).add(edge.start());
            relevant.add(edge.start());
            relevant.add(edge.end());
        }

        boolean[] visited = new boolean[vertexCount];
        List<List<Integer>> components = new ArrayList<>();

        java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>();
        for (int start : relevant) {
            if (visited[start]) {
                continue;
            }
            List<Integer> component = new ArrayList<>();
            queue.add(start);
            while (!queue.isEmpty()) {
                int current = queue.removeFirst();
                if (visited[current]) {
                    continue;
                }
                visited[current] = true;
                component.add(current);
                List<Integer> neighbours = adjacency.get(current);
                if (neighbours == null) {
                    continue;
                }
                for (int neighbour : neighbours) {
                    if (!visited[neighbour]) {
                        queue.add(neighbour);
                    }
                }
            }
            if (component.size() >= 3) {
                components.add(component);
            }
        }
        return components;
    }

    private List<Integer> orderComponent(List<Integer> component, List<CapVertex> vertices, Vector3f planeNormal) {
        Vector3f centroid = new Vector3f();
        for (int index : component) {
            centroid.add(vertices.get(index).position());
        }
        centroid.div(component.size());

        Vector3f uAxis = buildPerpendicular(planeNormal);
        Vector3f vAxis = new Vector3f(planeNormal).cross(uAxis).normalize();

        component.sort((a, b) -> {
            Vector3f da = new Vector3f(vertices.get(a).position()).sub(centroid);
            Vector3f db = new Vector3f(vertices.get(b).position()).sub(centroid);
            float angleA = (float) Math.atan2(da.dot(vAxis), da.dot(uAxis));
            float angleB = (float) Math.atan2(db.dot(vAxis), db.dot(uAxis));
            return Float.compare(angleA, angleB);
        });

        return component;
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
}
