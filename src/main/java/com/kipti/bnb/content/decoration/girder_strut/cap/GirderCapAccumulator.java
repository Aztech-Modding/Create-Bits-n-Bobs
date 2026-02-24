package com.kipti.bnb.content.decoration.girder_strut.cap;

import com.kipti.bnb.content.decoration.girder_strut.geometry.GirderGeometry;
import com.kipti.bnb.content.decoration.girder_strut.geometry.GirderVertex;
import com.kipti.bnb.content.decoration.girder_strut.mesh.GirderMeshQuad;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class GirderCapAccumulator {

    private final ResourceLocation capTexture;
    private final List<CapSegment> segments = new ArrayList<>();

    public GirderCapAccumulator(final ResourceLocation capTexture) {
        this.capTexture = capTexture;
    }

    public void addSegments(final TextureAtlasSprite sourceSprite, final int tintIndex, final boolean shade, final List<GirderMeshQuad.Segment> newSegments) {
        for (final GirderMeshQuad.Segment segment : newSegments) {
            final CapVertex start = new CapVertex(segment.start(), sourceSprite);
            final CapVertex end = new CapVertex(segment.end(), sourceSprite);
            if (GirderGeometry.positionsEqual(start.position(), end.position())) {
                continue;
            }
            final CapSegment candidate = new CapSegment(start, end, tintIndex, shade);
            segments.add(candidate);
        }
    }

    public void emitCapsToConsumer(final Vector3f planeNormal, final List<Consumer<BufferBuilder>> bufferConsumer, final Function<Vector3f, Integer> lightFunction) {
        if (segments.isEmpty()) {
            return;
        }
        final Vector3f normal = new Vector3f(planeNormal);
        if (normal.lengthSquared() <= GirderGeometry.EPSILON) {
            return;
        }
        normal.normalize();

        final TextureAtlasSprite capSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(capTexture);

        // Build unique vertex list and edge list
        final List<CapVertex> uniqueVertices = new ArrayList<>();
        final List<LoopEdge> edges = new ArrayList<>();

        for (final CapSegment segment : segments) {
            final int startIndex = indexFor(uniqueVertices, segment.start());
            final int endIndex = indexFor(uniqueVertices, segment.end());
            if (startIndex == endIndex) {
                continue;
            }
            edges.add(new LoopEdge(startIndex, endIndex, segment.tintIndex(), segment.shade()));
        }

        // Build closed loops from edges - emit each loop separately
        int loopCount = 0;
        while (true) {
            final LoopEdge startEdge = findUnusedEdge(edges);
            if (startEdge == null) {
                break;
            }

            final List<Integer> loop = new ArrayList<>();
            loop.add(startEdge.start());
            loop.add(startEdge.end());
            startEdge.markUsed();

            final int tintIndex = startEdge.tintIndex();
            final boolean shade = startEdge.shade();
            int current = startEdge.end();
            boolean closed = false;

            // Keep walking edges until we can't find the next edge or we close the loop
            final int maxSteps = edges.size() + 1; // Prevent infinite loops
            int steps = 0;
            while (steps < maxSteps) {
                steps++;
                if (current == loop.get(0)) {
                    // We've returned to the start - loop is closed
                    closed = true;
                    break;
                }

                final LoopEdge nextEdge = findAndUseEdge(edges, current);
                if (nextEdge == null) {
                    // Can't close the loop, try find a parrallel edge to link to (This is hack designed to work with the missing inside faces to the girder)

                    final int loopPre = loop.get(loop.size() - 2);
                    final int loopPost = loop.get(loop.size() - 1);
                    final Vector3f dir = new Vector3f(uniqueVertices.get(loopPost).position()).sub(uniqueVertices.get(loopPre).position()).normalize();

                    for (final LoopEdge edge : edges) {
                        if (!edge.used() && edge.start() != current && edge.end() != current) {
                            final Vector3f edgeDir = new Vector3f(uniqueVertices.get(edge.end()).position()).sub(uniqueVertices.get(edge.start()).position()).normalize();

                            if (Math.abs(dir.dot(edgeDir)) > 0.999f) {
                                //Check that the edge is parrallel, so the loop will be planar
                                final Vector3f toStart = new Vector3f(uniqueVertices.get(edge.start()).position()).sub(uniqueVertices.get(loopPost).position()).normalize();
                                final Vector3f toEnd = new Vector3f(uniqueVertices.get(edge.end()).position()).sub(uniqueVertices.get(loopPre).position()).normalize();
                                if (toStart.dot(toEnd) < 0.01f) {
                                    edge.markUsed();
                                    loop.add(edge.end());
                                    loop.add(edge.start());
                                    loop.add(edge.end()); // Purposely duplicate the last vertex so that a full quad remains later
                                    closed = true;
                                }

                                //Or check reverse combination
                                final Vector3f toStartR = new Vector3f(uniqueVertices.get(edge.start()).position()).sub(uniqueVertices.get(loop.get(0)).position()).normalize();
                                final Vector3f toEndR = new Vector3f(uniqueVertices.get(edge.end()).position()).sub(uniqueVertices.get(loop.get(1)).position()).normalize();
                                if (toStartR.dot(toEndR) < 0.01f) {
                                    edge.markUsed();
                                    loop.add(edge.end());
                                    loop.add(edge.start());
                                    loop.add(edge.end()); // Purposely duplicate the last vertex so that a full quad remains later
                                    closed = true;
                                }
                            }
                        }
                    }
                    break;
                }

                final int nextVertex = nextEdge.other(current);
                loop.add(nextVertex);
                current = nextVertex;
            }

            if (closed && loop.size() > 2) {
                // Remove the duplicate closing vertex
                loop.remove(loop.size() - 1);

                applyCapUVToVertices(loop, uniqueVertices, normal, capSprite);

                emitLoopToConsumer(loop, uniqueVertices, normal, bufferConsumer, lightFunction);
                loopCount++;
            }
        }

        segments.clear();
    }

    private void emitLoopToConsumer(final List<Integer> loopIndices, final List<CapVertex> vertices, final Vector3f normal, final List<Consumer<BufferBuilder>> bufferConsumer, final Function<Vector3f, Integer> lightFunction) {
        // Use the cut-facing normal (flip the supplied plane normal) so the cap
        // quads face into the cut, not towards the surface.
        final Vector3f normalizedPlane = new Vector3f(normal);
        if (normalizedPlane.lengthSquared() > GirderGeometry.EPSILON) {
            normalizedPlane.normalize();
        }
        final Vector3f faceNormal = new Vector3f(normalizedPlane).negate();

        final List<GirderVertex> loopVertices = getGirderVertices(loopIndices, vertices, faceNormal);

        final List<GirderVertex> cleaned = GirderGeometry.dedupeLoopVertices(loopVertices);
        if (cleaned.size() < 3) {
            return;
        }

        // Check winding order and reverse if needed
        final Vector3f polygonNormal = GirderGeometry.computePolygonNormal(cleaned);
        if (polygonNormal.lengthSquared() > GirderGeometry.EPSILON && polygonNormal.dot(faceNormal) < 0f) {
            java.util.Collections.reverse(cleaned);
        }

        GirderGeometry.emitPolygonToConsumer(cleaned, bufferConsumer, lightFunction);
    }

    public void emitCaps(final Vector3f planePoint, final Vector3f planeNormal, final List<BakedQuad> consumer) {
        if (segments.isEmpty()) {
            return;
        }
        final Vector3f normal = new Vector3f(planeNormal);
        if (normal.lengthSquared() <= GirderGeometry.EPSILON) {
            return;
        }
        normal.normalize();

        final TextureAtlasSprite stoneSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(capTexture);

//        dedupeSegments();

//        ClientEvents.clearDeferredDebugRenderOutlines();
//        //TEMP DEBUG when needed dont delete i guess,: put a triangle on each segment to visualize
//        System.out.println("Starting loops");
//        for (CapSegment segment : segments) {
//            List<CapVertex> loop = new ArrayList<>();
//            loop.add(segment.start().copy());
//            loop.add(segment.end().copy());
//            Vector3f midPoint = new Vector3f(segment.start().relativePos()).add(segment.end
//                .relativePos()).mul(0.5f);
//            loop.add(new CapVertex(new Vector3f(midPoint).add(new Vector3f(planeNormal).mul(-0.6f)).add((float) (Math.random() * 0.2f), (float) (Math.random() * 0.2f), (float) (Math.random() * 0.2f)), 0f, 0f,
//                GirderGeometry.DEFAULT_COLOR, GirderGeometry.DEFAULT_LIGHT, segment.start().sourceSprite()));
//
//            ClientEvents.pushNewDeferredDebugRenderOutline(
//                Pair.of(
//                    new Vec3(segment.start().relativePos().x, segment.start().relativePos().y, segment.start().relativePos().z),
//                    new Vec3(segment.end().relativePos().x, segment.end().relativePos().y, segment.end().relativePos().z)
//                )
//            );
//
//            System.out.println("Emitting loop with " + loop.size() + " vertices");
//            emitLoop(loop.stream().map(loop::indexOf).toList(), loop, segment.tintIndex(), segment.shade(), new Vector3f(planeNormal).cross(new Vector3f(segment.start.relativePos).sub(segment.end.relativePos).normalize()), stoneSprite, consumer);
//        }
//        segments.clear();

        // Build unique vertex list and edge list
        final List<CapVertex> uniqueVertices = new ArrayList<>();
        final List<LoopEdge> edges = new ArrayList<>();

        for (final CapSegment segment : segments) {
            final int startIndex = indexFor(uniqueVertices, segment.start());
            final int endIndex = indexFor(uniqueVertices, segment.end());
            if (startIndex == endIndex) {
                continue;
            }
            edges.add(new LoopEdge(startIndex, endIndex, segment.tintIndex(), segment.shade()));
        }

        // Build closed loops from edges - emit each loop separately
        int loopCount = 0;
        while (true) {
            final LoopEdge startEdge = findUnusedEdge(edges);
            if (startEdge == null) {
                break;
            }

            final List<Integer> loop = new ArrayList<>();
            loop.add(startEdge.start());
            loop.add(startEdge.end());
            startEdge.markUsed();

            final int tintIndex = startEdge.tintIndex();
            final boolean shade = startEdge.shade();
            int current = startEdge.end();
            boolean closed = false;

            // Keep walking edges until we can't find the next edge or we close the loop
            final int maxSteps = edges.size() + 1; // Prevent infinite loops
            int steps = 0;
            while (steps < maxSteps) {
                steps++;
                if (current == loop.get(0)) {
                    // We've returned to the start - loop is closed
                    closed = true;
                    break;
                }

                final LoopEdge nextEdge = findAndUseEdge(edges, current);
                if (nextEdge == null) {
                    // Can't close the loop, try find a parrallel edge to link to (This is hack designed to work with the missing inside faces to the girder)

                    final int loopPre = loop.get(loop.size() - 2);
                    final int loopPost = loop.get(loop.size() - 1);
                    final Vector3f dir = new Vector3f(uniqueVertices.get(loopPost).position()).sub(uniqueVertices.get(loopPre).position()).normalize();

                    for (final LoopEdge edge : edges) {
                        if (!edge.used() && edge.start() != current && edge.end() != current) {
                            final Vector3f edgeDir = new Vector3f(uniqueVertices.get(edge.end()).position()).sub(uniqueVertices.get(edge.start()).position()).normalize();

                            if (Math.abs(dir.dot(edgeDir)) > 0.999f) {
                                //Check that the edge is parrallel, so the loop will be planar
                                final Vector3f toStart = new Vector3f(uniqueVertices.get(edge.start()).position()).sub(uniqueVertices.get(loopPost).position()).normalize();
                                final Vector3f toEnd = new Vector3f(uniqueVertices.get(edge.end()).position()).sub(uniqueVertices.get(loopPre).position()).normalize();
                                if (toStart.dot(toEnd) < 0.01f) {
                                    edge.markUsed();
                                    loop.add(edge.end());
                                    loop.add(edge.start());
                                    loop.add(edge.end()); // Purposely duplicate the last vertex so that a full quad remains later
                                    closed = true;
                                }

                                //Or check reverse combination
                                final Vector3f toStartR = new Vector3f(uniqueVertices.get(edge.start()).position()).sub(uniqueVertices.get(loop.get(0)).position()).normalize();
                                final Vector3f toEndR = new Vector3f(uniqueVertices.get(edge.end()).position()).sub(uniqueVertices.get(loop.get(1)).position()).normalize();
                                if (toStartR.dot(toEndR) < 0.01f) {
                                    edge.markUsed();
                                    loop.add(edge.end());
                                    loop.add(edge.start());
                                    loop.add(edge.end()); // Purposely duplicate the last vertex so that a full quad remains later
                                    closed = true;
                                }
                            }
                        }
                    }
                    break;
                }

                final int nextVertex = nextEdge.other(current);
                loop.add(nextVertex);
                current = nextVertex;
            }

            if (closed && loop.size() > 2) {
                // Remove the duplicate closing vertex
                loop.remove(loop.size() - 1);

                applyCapUVToVertices(loop, uniqueVertices, normal, stoneSprite);

                emitLoop(loop, uniqueVertices, tintIndex, shade, normal, stoneSprite, consumer);
                loopCount++;
            }
        }

        segments.clear();
    }

    private void applyCapUVToVertices(final List<Integer> loop, final List<CapVertex> uniqueVertices, final Vector3f normal, final TextureAtlasSprite sprite) {
        // Compute a simple planar UV mapping for the cap
        // Find two orthogonal axes in the plane
        final Vector3f uvUp = new Vector3f(0, 1, 0).cross(normal);
        if (uvUp.lengthSquared() <= GirderGeometry.EPSILON) {
            uvUp.set(1, 0, 0);
        }
        uvUp.normalize();

        final Vector3f uvRight = new Vector3f(normal).cross(uvUp).normalize();

        final Vector3f uvOrigin = new Vector3f();
        for (final int index : loop) {
            uvOrigin.add(uniqueVertices.get(index).position());
        }
        uvOrigin.mul(1f / loop.size());

        final float uScale = sprite.getU1() - sprite.getU0();
        final float vScale = sprite.getV1() - sprite.getV0();

        final float uOffset = sprite.getU0() + uScale / 2f;
        final float vOffset = sprite.getV0() + vScale / 2f;

        for (final int index : loop) {
            final CapVertex vertex = uniqueVertices.get(index);
            final Vector3f toVertex = new Vector3f(vertex.position()).sub(uvOrigin);
            final float u = Math.clamp(toVertex.dot(uvRight), -0.5f, 0.5f);
            final float v = Math.clamp(toVertex.dot(uvUp), -0.5f, 0.5f);
            // Update vertex UVs
            uniqueVertices.set(index, new CapVertex(vertex.position(), uScale * u + uOffset, vScale * v + vOffset, vertex.color(), vertex.light(), vertex.sourceSprite()));
        }
    }

    private int indexFor(final List<CapVertex> vertices, final CapVertex vertex) {
        for (int i = 0; i < vertices.size(); i++) {
            if (positionsClose(vertices.get(i).position(), vertex.position())) {
                return i;
            }
        }
        vertices.add(vertex.copy());
        return vertices.size() - 1;
    }

    /**
     * Compare two positions using a very relaxed tolerance to merge vertices that lie on
     * the same edge of the clipping plane, even if they come from different quads.
     * This is necessary because each quad generates its own clipped vertices independently.
     */
    private static boolean positionsClose(final org.joml.Vector3f a, final org.joml.Vector3f b) {
        final float dx = a.x - b.x;
        final float dy = a.y - b.y;
        final float dz = a.z - b.z;
        // Use a larger tolerance to merge vertices on the same plane edge
        final float tol = 0.01f; // 1 centimeter in block units
        return dx * dx + dy * dy + dz * dz <= tol * tol;
    }

    private LoopEdge findUnusedEdge(final List<LoopEdge> edges) {
        for (final LoopEdge edge : edges) {
            if (!edge.used()) {
                return edge;
            }
        }
        return null;
    }

    private LoopEdge findAndUseEdge(final List<LoopEdge> edges, final int vertexIndex) {
        for (final LoopEdge edge : edges) {
            if (edge.used()) {
                continue;
            }
            if (edge.start() == vertexIndex || edge.end() == vertexIndex) {
                edge.markUsed();
                return edge;
            }
        }
        return null;
    }

    private void emitLoop(
            final List<Integer> loopIndices,
            final List<CapVertex> vertices,
            final int tintIndex,
            final boolean shade,
            final Vector3f normal,
            final TextureAtlasSprite stoneSprite,
            final List<BakedQuad> consumer
    ) {
        // Use the cut-facing normal (flip the supplied plane normal) so the cap
        // quads face into the cut, not towards the surface.
        final Vector3f normalizedPlane = new Vector3f(normal);
        if (normalizedPlane.lengthSquared() > GirderGeometry.EPSILON) {
            normalizedPlane.normalize();
        }
        final Vector3f faceNormal = new Vector3f(normalizedPlane).negate();

        final List<GirderVertex> loopVertices = getGirderVertices(loopIndices, vertices, faceNormal);

        final List<GirderVertex> cleaned = GirderGeometry.dedupeLoopVertices(loopVertices);
        if (cleaned.size() < 3) {
            return;
        }

        // Check winding order and reverse if needed
        final Vector3f polygonNormal = GirderGeometry.computePolygonNormal(cleaned);
        if (polygonNormal.lengthSquared() > GirderGeometry.EPSILON && polygonNormal.dot(faceNormal) < 0f) {
            java.util.Collections.reverse(cleaned);
        }

        final Direction face = Direction.getNearest(faceNormal.x, faceNormal.y, faceNormal.z);
        GirderGeometry.emitPolygon(cleaned, stoneSprite, face, tintIndex, shade, consumer);
    }

    private static @NotNull List<GirderVertex> getGirderVertices(final List<Integer> loopIndices, final List<CapVertex> vertices, final Vector3f faceNormal) {
        final List<GirderVertex> loopVertices = new ArrayList<>(loopIndices.size());
        for (final int index : loopIndices) {
            final CapVertex data = vertices.get(index);
            // Project the vertex onto the clipping plane
            final Vector3f point = new Vector3f(data.position());


            loopVertices.add(new GirderVertex(
                    point,
                    new Vector3f(faceNormal),
                    data.u,
                    data.v,
                    data.color(),
                    data.light()
            ));
        }
        return loopVertices;
    }

    private record CapSegment(CapVertex start, CapVertex end, int tintIndex, boolean shade) {
    }

    private static final class CapVertex {

        private final Vector3f position;
        private final float u;
        private final float v;
        private final int color;
        private final int light;
        private final TextureAtlasSprite sourceSprite;

        CapVertex(final GirderVertex vertex, final TextureAtlasSprite sprite) {
            this(new Vector3f(vertex.position()), vertex.u(), vertex.v(), vertex.color(), vertex.light(), sprite);
        }

        private CapVertex(final Vector3f position, final float u, final float v, final int color, final int light, final TextureAtlasSprite sourceSprite) {
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

    private static final class LoopEdge {

        private final int start;
        private final int end;
        private final int tintIndex;
        private final boolean shade;
        private boolean used;

        LoopEdge(final int start, final int end, final int tintIndex, final boolean shade) {
            this.start = start;
            this.end = end;
            this.tintIndex = tintIndex;
            this.shade = shade;
        }

        int start() {
            return start;
        }

        int end() {
            return end;
        }

        int tintIndex() {
            return tintIndex;
        }

        boolean shade() {
            return shade;
        }

        boolean used() {
            return used;
        }

        void markUsed() {
            used = true;
        }

        int other(final int vertex) {
            return vertex == start ? end : start;
        }
    }
}

