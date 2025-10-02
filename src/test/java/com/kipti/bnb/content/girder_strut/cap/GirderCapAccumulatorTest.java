package com.kipti.bnb.content.girder_strut.cap;

import com.kipti.bnb.content.girder_strut.geometry.GirderGeometry;
import com.kipti.bnb.content.girder_strut.geometry.GirderVertex;
import com.kipti.bnb.content.girder_strut.mesh.GirderMeshQuad;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GirderCapAccumulatorTest {

    private static final Vector3f PLANE_POINT = new Vector3f(0f, 0f, 0f);
    private static final Vector3f PLANE_NORMAL = new Vector3f(0f, 0f, 1f);

    @Test
    void singleRectangleProducesLoopWithProjectedVertices() {
        GirderCapAccumulator accumulator = new GirderCapAccumulator(ResourceLocation.fromNamespaceAndPath("test", "stone"));
        List<GirderMeshQuad.Segment> segments = List.of(
            new GirderMeshQuad.Segment(vertex(0f, 0f, 0f), vertex(1f, 0f, 0f)),
            new GirderMeshQuad.Segment(vertex(1f, 0f, 0f), vertex(1f, 1f, 0f)),
            new GirderMeshQuad.Segment(vertex(1f, 1f, 0f), vertex(0f, 1f, 0f)),
            new GirderMeshQuad.Segment(vertex(0f, 1f, 0f), vertex(0f, 0f, 0f))
        );
        accumulator.addSegments(null, 0, false, segments);

        List<GirderCapAccumulator.CapLoop> loops = accumulator.buildLoops(PLANE_POINT, PLANE_NORMAL);
        assertEquals(1, loops.size(), "expected a single loop for the rectangle");

        GirderCapAccumulator.CapLoop loop = loops.getFirst();
        assertEquals(4, loop.vertices().size(), "loop should contain each corner");

        Set<Vector3f> expected = collectProjectedPositions(segments, PLANE_POINT, PLANE_NORMAL);
        assertVerticesMatch(loop.vertices(), expected);
    }

    @Test
    void duplicateSegmentsProduceIndependentLoops() {
        GirderCapAccumulator accumulator = new GirderCapAccumulator(ResourceLocation.fromNamespaceAndPath("test", "stone"));
        List<GirderMeshQuad.Segment> baseSegments = List.of(
            new GirderMeshQuad.Segment(vertex(0.25f, 0.25f, 0f), vertex(0.75f, 0.25f, 0f)),
            new GirderMeshQuad.Segment(vertex(0.75f, 0.25f, 0f), vertex(0.75f, 0.75f, 0f)),
            new GirderMeshQuad.Segment(vertex(0.75f, 0.75f, 0f), vertex(0.25f, 0.75f, 0f)),
            new GirderMeshQuad.Segment(vertex(0.25f, 0.75f, 0f), vertex(0.25f, 0.25f, 0f))
        );
        List<GirderMeshQuad.Segment> segments = new ArrayList<>();
        segments.addAll(baseSegments);
        segments.addAll(baseSegments);
        accumulator.addSegments(null, 1, true, segments);

        List<GirderCapAccumulator.CapLoop> loops = accumulator.buildLoops(PLANE_POINT, PLANE_NORMAL);
        assertEquals(1, loops.size(), "duplicated segments with identical attributes collapse to a single loop");

        Set<Vector3f> expected = collectProjectedPositions(baseSegments, PLANE_POINT, PLANE_NORMAL);
        GirderCapAccumulator.CapLoop loop = loops.getFirst();
        assertEquals(4, loop.vertices().size(), "loop should contain the square corners");
        assertVerticesMatch(loop.vertices(), expected);
        assertPositiveArea(loop.vertices());
        assertAllProjectedVerticesCovered(loops, segments, PLANE_POINT, PLANE_NORMAL);
    }

    @Test
    void complexSegmentSetProducesExpectedCoverage() {
        GirderCapAccumulator accumulator = new GirderCapAccumulator(ResourceLocation.fromNamespaceAndPath("test", "stone"));
        List<GirderMeshQuad.Segment> segments = List.of(
            segment(0.3750f, 1.0300f, 1.0010f, 0.3750f, 0.6768f, 1.0010f),
            segment(0.3750f, 0.6768f, 1.0010f, 0.6250f, 0.6768f, 1.0010f),
            segment(0.6250f, 0.6768f, 1.0010f, 0.6250f, 1.0300f, 1.0010f),
            segment(0.6250f, 1.0300f, 1.0010f, 0.3750f, 1.0300f, 1.0010f),
            segment(0.6250f, 0.9786f, 1.0010f, 0.6250f, 0.2714f, 1.0010f),
            segment(0.3750f, 0.9786f, 1.0010f, 0.3750f, 0.2714f, 1.0010f),
            segment(0.7500f, 0.09467f, 1.0010f, 0.2500f, 0.09467f, 1.0010f),
            segment(0.7500f, 0.2714f, 1.0010f, 0.2500f, 0.2714f, 1.0010f),
            segment(0.2500f, 0.6768f, 1.0010f, 0.2500f, 0.5884f, 1.0010f),
            segment(0.2500f, 0.5884f, 1.0010f, 0.7500f, 0.5884f, 1.0010f),
            segment(0.7500f, 0.5884f, 1.0010f, 0.7500f, 0.6768f, 1.0010f),
            segment(0.7500f, 0.6768f, 1.0010f, 0.2500f, 0.6768f, 1.0010f),
            segment(0.7500f, 0.2714f, 1.0010f, 0.7500f, 0.09467f, 1.0010f),
            segment(0.2500f, 0.2714f, 1.0010f, 0.2500f, 0.09467f, 1.0010f),
            segment(0.7500f, 0.9786f, 1.0010f, 0.2500f, 0.9786f, 1.0010f),
            segment(0.2500f, 1.0820f, 1.0010f, 0.7500f, 1.0820f, 1.0010f),
            segment(0.7500f, 1.0820f, 1.0010f, 0.7500f, 0.9786f, 1.0010f),
            segment(0.2500f, 0.9786f, 1.0010f, 0.2500f, 1.0820f, 1.0010f)
        );

        assertDoesNotThrow(() -> accumulator.addSegments(null, 2, true, segments));
        List<GirderCapAccumulator.CapLoop> loops = accumulator.buildLoops(new Vector3f(0f, 0f, 1.001f), PLANE_NORMAL);

        assertEquals(4, loops.size(), "expected four discrete loops for the girder cap");

        Vector3f planePoint = new Vector3f(0f, 0f, 1.001f);
        Set<Vector3f> expected = collectProjectedPositions(segments, planePoint, PLANE_NORMAL);
        for (GirderCapAccumulator.CapLoop loop : loops) {
            assertFalse(loop.vertices().isEmpty(), "loop should contain vertices");
            assertVerticesMatch(loop.vertices(), expected);
            assertPositiveArea(loop.vertices());
        }
        assertAllProjectedVerticesCovered(loops, segments, planePoint, PLANE_NORMAL);
    }

    private static void assertVerticesMatch(List<GirderCapAccumulator.CapVertex> vertices, Set<Vector3f> expected) {
        assertFalse(vertices.isEmpty(), "no vertices emitted for loop");
        for (GirderCapAccumulator.CapVertex vertex : vertices) {
            boolean match = expected.stream().anyMatch(candidate -> closeEnough(candidate, vertex.position()));
            assertTrue(match, "vertex %s not matched against expected set %s".formatted(vertex.position(), expected));
        }
    }

    private static void assertAllProjectedVerticesCovered(
        List<GirderCapAccumulator.CapLoop> loops,
        List<GirderMeshQuad.Segment> segments,
        Vector3f planePoint,
        Vector3f planeNormal
    ) {
        Map<String, Integer> usage = new HashMap<>();
        for (GirderMeshQuad.Segment segment : segments) {
            increment(usage, project(segment.start().position(), planePoint, planeNormal));
            increment(usage, project(segment.end().position(), planePoint, planeNormal));
        }

        List<Vector3f> missing = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : usage.entrySet()) {
            if (entry.getValue() < 2) {
                continue;
            }
            Vector3f position = decode(entry.getKey());
            boolean covered = false;
            for (GirderCapAccumulator.CapLoop loop : loops) {
                covered = loop.vertices().stream().anyMatch(vertex -> closeEnough(position, vertex.position()));
                if (covered) {
                    break;
                }
            }
            if (!covered) {
                missing.add(position);
            }
        }
        assertTrue(missing.isEmpty(), "projected vertices were not covered: " + missing);
    }

    private static void assertPositiveArea(List<GirderCapAccumulator.CapVertex> vertices) {
        float area = 0f;
        int size = vertices.size();
        for (int i = 0; i < size; i++) {
            Vector3f current = vertices.get(i).position();
            Vector3f next = vertices.get((i + 1) % size).position();
            area += (current.x * next.y) - (next.x * current.y);
        }
        assertTrue(Math.abs(area) > GirderGeometry.EPSILON, "loop collapsed to zero-area polygon");
    }

    private static void increment(Map<String, Integer> usage, Vector3f position) {
        usage.merge(quantize(position), 1, Integer::sum);
    }

    private static String quantize(Vector3f position) {
        int x = Math.round(position.x / GirderGeometry.EPSILON);
        int y = Math.round(position.y / GirderGeometry.EPSILON);
        int z = Math.round(position.z / GirderGeometry.EPSILON);
        return x + ":" + y + ":" + z;
    }

    private static Vector3f decode(String key) {
        String[] parts = key.split(":");
        float x = Integer.parseInt(parts[0]) * GirderGeometry.EPSILON;
        float y = Integer.parseInt(parts[1]) * GirderGeometry.EPSILON;
        float z = Integer.parseInt(parts[2]) * GirderGeometry.EPSILON;
        return new Vector3f(x, y, z);
    }

    private static Set<Vector3f> collectProjectedPositions(
        List<GirderMeshQuad.Segment> segments,
        Vector3f planePoint,
        Vector3f planeNormal
    ) {
        Set<Vector3f> projected = new HashSet<>();
        for (GirderMeshQuad.Segment segment : segments) {
            projected.add(project(segment.start().position(), planePoint, planeNormal));
            projected.add(project(segment.end().position(), planePoint, planeNormal));
        }
        return projected;
    }

    private static GirderMeshQuad.Segment segment(
        float sx,
        float sy,
        float sz,
        float ex,
        float ey,
        float ez
    ) {
        return new GirderMeshQuad.Segment(vertex(sx, sy, sz), vertex(ex, ey, ez));
    }

    private static Vector3f project(Vector3f position, Vector3f planePoint, Vector3f planeNormal) {
        Vector3f projected = new Vector3f(position);
        float distance = GirderGeometry.signedDistance(projected, planeNormal, planePoint);
        if (Math.abs(distance) > GirderGeometry.EPSILON) {
            projected.sub(new Vector3f(planeNormal).mul(distance));
        }
        return projected;
    }

    private static GirderVertex vertex(float x, float y, float z) {
        return new GirderVertex(
            new Vector3f(x, y, z),
            new Vector3f(0f, 0f, -1f),
            0f,
            0f,
            GirderGeometry.DEFAULT_COLOR,
            GirderGeometry.DEFAULT_LIGHT
        );
    }

    private static boolean closeEnough(Vector3f a, Vector3f b) {
        return new Vector3f(a).sub(b).lengthSquared() <= GirderGeometry.EPSILON * GirderGeometry.EPSILON * 4f;
    }
}
