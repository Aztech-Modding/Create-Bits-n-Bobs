package com.kipti.bnb.content.decoration.light.headlamp.rendering.pipeline.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.createmod.catnip.client.render.model.ShadeSeparatedBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.jetbrains.annotations.UnknownNullability;

//Copied from catnip universal mesh emitter, but not package private
// Modified from https://github.com/Engine-Room/Flywheel/blob/2f67f54c8898d91a48126c3c753eefa6cd224f84/forge/src/lib/java/dev/engine_room/flywheel/lib/model/baked/MeshEmitter.java
public class MultiversalMeshEmitter implements VertexConsumer {
    @UnknownNullability
    private ShadeSeparatedBufferSource bufferSource;
    @UnknownNullability
    private RenderType layer;

    public void prepare(final ShadeSeparatedBufferSource bufferSource, final RenderType layer) {
        this.bufferSource = bufferSource;
        this.layer = layer;
    }

    public void clear() {
        bufferSource = null;
    }

    @Override
    public void putBulkData(final PoseStack.Pose pose, final BakedQuad quad, final float red, final float green, final float blue, final float alpha, final int light, final int overlay) {
        final VertexConsumer buffer = bufferSource.getBuffer(layer, quad.isShade());
        buffer.putBulkData(pose, quad, red, green, blue, alpha, light, overlay);
    }

    @Override
    public void putBulkData(final PoseStack.Pose pose, final BakedQuad quad, final float red, final float green, final float blue, final float alpha, final int light, final int overlay, final boolean readExistingColor) {
        final VertexConsumer buffer = bufferSource.getBuffer(layer, quad.isShade());
        buffer.putBulkData(pose, quad, red, green, blue, alpha, light, overlay, readExistingColor);
    }

    @Override
    public void putBulkData(final PoseStack.Pose pose, final BakedQuad quad, final float[] brightnesses, final float red, final float green, final float blue, final float alpha, final int[] lights, final int overlay, final boolean readExistingColor) {
        final VertexConsumer buffer = bufferSource.getBuffer(layer, quad.isShade());
        buffer.putBulkData(pose, quad, brightnesses, red, green, blue, alpha, lights, overlay, readExistingColor);
    }

    @Override
    public VertexConsumer addVertex(final float x, final float y, final float z) {
        throw new UnsupportedOperationException("UniversalMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setColor(final int red, final int green, final int blue, final int alpha) {
        throw new UnsupportedOperationException("UniversalMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setUv(final float u, final float v) {
        throw new UnsupportedOperationException("UniversalMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setUv1(final int u, final int v) {
        throw new UnsupportedOperationException("UniversalMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setUv2(final int u, final int v) {
        throw new UnsupportedOperationException("UniversalMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setNormal(final float x, final float y, final float z) {
        throw new UnsupportedOperationException("UniversalMeshEmitter only supports putBulkData!");
    }
}
