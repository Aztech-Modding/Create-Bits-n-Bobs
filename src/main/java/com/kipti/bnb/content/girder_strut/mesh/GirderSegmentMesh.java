package com.kipti.bnb.content.girder_strut.mesh;

import com.kipti.bnb.content.girder_strut.geometry.GirderGeometry;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GirderSegmentMesh {

    private final List<GirderMeshQuad> baseQuads;

    public GirderSegmentMesh(final List<BakedQuad> quads) {
        this.baseQuads = quads.stream()
                .map(GirderMeshQuad::from)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<GirderMeshQuad> forLength(final float length) {
        final int fullSegments = Mth.floor(length + GirderGeometry.EPSILON);
        final float partial = length - fullSegments;

        final List<GirderMeshQuad> result = new ArrayList<>(baseQuads.size() * (fullSegments + 1));

        for (int i = 0; i < fullSegments; i++) {
            final float offset = i;
            for (final GirderMeshQuad quad : baseQuads) {
                result.add(quad.translate(0f, 0f, offset));
            }
        }

        if (partial > GirderGeometry.EPSILON) {
            for (final GirderMeshQuad quad : baseQuads) {
                final GirderMeshQuad clipped = quad.clipZ(partial);
                if (clipped != null) {
                    result.add(clipped.translate(0f, 0f, fullSegments));
                }
            }
        }

        if (result.isEmpty()) {
            for (final GirderMeshQuad quad : baseQuads) {
                final GirderMeshQuad fallback = quad.clipZ(Math.max(partial, GirderGeometry.EPSILON));
                if (fallback != null) {
                    result.add(fallback);
                }
            }
        }

        return result;
    }
}
