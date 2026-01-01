package com.kipti.bnb.mixin;

import com.kipti.bnb.mixin_accessor.ReverseRenderableBakedGlyph;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BakedGlyph.class)
public class BakedGlyphMixin implements ReverseRenderableBakedGlyph {

    @Shadow
    @Final
    private float left;

    @Shadow
    @Final
    private float right;

    @Shadow
    @Final
    private float up;

    @Shadow
    @Final
    private float down;

    @Shadow
    @Final
    private float u0;

    @Shadow
    @Final
    private float v0;

    @Shadow
    @Final
    private float u1;

    @Shadow
    @Final
    private float v1;

    @Override
    public void bits_n_bobs$renderReverse(
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
    ) {
        final float f = x + this.left;
        final float f1 = x + this.right;
        final float f2 = y + this.up;
        final float f3 = y + this.down;
        final float f4 = italic ? 1.0F - 0.25F * this.up : 0.0F;
        final float f5 = italic ? 1.0F - 0.25F * this.down : 0.0F;
        buffer.addVertex(matrix, f1 + f4, f2, 0.0F).setColor(red, green, blue, alpha).setUv(this.u1, this.v0).setLight(packedLight);
        buffer.addVertex(matrix, f1 + f5, f3, 0.0F).setColor(red, green, blue, alpha).setUv(this.u1, this.v1).setLight(packedLight);
        buffer.addVertex(matrix, f + f5, f3, 0.0F).setColor(red, green, blue, alpha).setUv(this.u0, this.v1).setLight(packedLight);
        buffer.addVertex(matrix, f + f4, f2, 0.0F).setColor(red, green, blue, alpha).setUv(this.u0, this.v0).setLight(packedLight);
    }

}
