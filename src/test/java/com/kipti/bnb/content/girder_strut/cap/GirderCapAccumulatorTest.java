package com.kipti.bnb.content.girder_strut.cap;

import com.kipti.bnb.content.girder_strut.geometry.GirderGeometry;
import com.kipti.bnb.content.girder_strut.geometry.GirderVertex;
import com.kipti.bnb.content.girder_strut.mesh.GirderMeshQuad;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GirderCapAccumulatorTest {

    private static final Vector3f PLANE_POINT = new Vector3f(0f, 0f, 0f);
    private static final Vector3f PLANE_NORMAL = new Vector3f(0f, 0f, 1f);

    @Test
    void singleRectangleProducesLoopWithProjectedVertices() {
        GirderCapAccumulator accumulator = new GirderCapAccumulator(new ResourceLocation("test", "stone"));
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

        Set<Vector3f> expected = collectProjectedPositions(segments);
        assertVerticesMatch(loop.vertices(), expected);
    }

    @Test
    void duplicateSegmentsProduceIndependentLoops() {
        GirderCapAccumulator accumulator = new GirderCapAccumulator(new ResourceLocation("test", "stone"));
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
        assertEquals(2, loops.size(), "duplicated segments should trace two separate loops");

        Set<Vector3f> expected = collectProjectedPositions(baseSegments);
        for (GirderCapAccumulator.CapLoop loop : loops) {
            assertEquals(4, loop.vertices().size(), "each loop should contain the square corners");
            assertVerticesMatch(loop.vertices(), expected);
        }
    }

    private static void assertVerticesMatch(List<GirderCapAccumulator.CapVertex> vertices, Set<Vector3f> expected) {
        assertFalse(vertices.isEmpty(), "no vertices emitted for loop");
        for (GirderCapAccumulator.CapVertex vertex : vertices) {
            boolean match = expected.stream().anyMatch(candidate -> closeEnough(candidate, vertex.position()));
            assertTrue(match, "vertex %s not matched against expected set %s".formatted(vertex.position(), expected));
        }
    }

    private static Set<Vector3f> collectProjectedPositions(List<GirderMeshQuad.Segment> segments) {
        Set<Vector3f> projected = new HashSet<>();
        for (GirderMeshQuad.Segment segment : segments) {
            projected.add(project(segment.start().position()));
            projected.add(project(segment.end().position()));
        }
        return projected;
    }

    private static Vector3f project(Vector3f position) {
        Vector3f projected = new Vector3f(position);
        float distance = GirderGeometry.signedDistance(projected, PLANE_NORMAL, PLANE_POINT);
        if (Math.abs(distance) > GirderGeometry.EPSILON) {
            projected.sub(new Vector3f(PLANE_NORMAL).mul(distance));
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
