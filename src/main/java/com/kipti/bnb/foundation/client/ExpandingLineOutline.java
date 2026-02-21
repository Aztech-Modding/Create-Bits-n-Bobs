package com.kipti.bnb.foundation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.createmod.catnip.outliner.LineOutline;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector4f;

public class ExpandingLineOutline extends LineOutline {

    protected int growingTicksElapsed = 0;
    protected int growingTicks;

    public ExpandingLineOutline setGrowingTicks(int growingTicks) {
        this.growingTicks = growingTicks;
        return this;
    }

    public ExpandingLineOutline setGrowingTicksElapsed(int growingTicksElapsed) {
        this.growingTicksElapsed = growingTicksElapsed;
        return this;
    }

    public ExpandingLineOutline tickGrowingTicksElapsed() {
        this.growingTicksElapsed++;
        return this;
    }

    @Override
    protected void renderInner(final @NotNull PoseStack ms,
                               final @NotNull VertexConsumer consumer,
                               final @NotNull Vec3 camera,
                               final float pt,
                               final float width,
                               final @NotNull Vector4f color,
                               final int lightmap,
                               final boolean disableNormals) {
        bufferCuboidLine(ms, consumer, camera, lerpGrowingLinePoint(start, pt), lerpGrowingLinePoint(end, pt), width, color, lightmap, disableNormals);
    }

    private Vector3d lerpGrowingLinePoint(final Vector3d point, final float pt) {
        final float progress = Math.min(1f, (growingTicksElapsed + pt) / growingTicks);

        //Do an ease-out interpolation for a smoother effect
        final float oneMinusProgress = 1 - progress;
        final float easedProgress = 1 - (oneMinusProgress * oneMinusProgress * oneMinusProgress);

        final Vector3d midpoint = new Vector3d(
                (start.x + end.x) / 2,
                (start.y + end.y) / 2,
                (start.z + end.z) / 2
        );

        return new Vector3d(
                midpoint.x + (point.x - midpoint.x) * easedProgress,
                midpoint.y + (point.y - midpoint.y) * easedProgress,
                midpoint.z + (point.z - midpoint.z) * easedProgress
        );
    }
}

