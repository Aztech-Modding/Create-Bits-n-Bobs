package com.kipti.bnb.mixin_accessor;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

public interface ReverseRenderableBakedGlyph {
    void bits_n_bobs$renderReverse(
            final boolean italic,
            final float x,
            final float y,
            final Matrix4f matrix,
            final VertexConsumer buffer,
            final float red,
            final float green,
            final float blue,
            final float alpha,
            final int packedLight
    );
}
