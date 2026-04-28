package com.kipti.bnb.content.kinetics.cogwheel_chain.shape;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

public abstract class CogwheelChainShape {

    @Nullable
    public abstract Vec3 intersect(Vec3 from, Vec3 to);

    public abstract float getChainPosition(Vec3 intersection);

    protected abstract void drawOutline(PoseStack ms, VertexConsumer vb, UnaryOperator<Vec3> positionTransform);

    public abstract Vec3 getLocalVec(float position);

    public Vec3 getVec(final BlockPos anchor, final float position) {
        return this.getLocalVec(position).add(Vec3.atLowerCornerOf(anchor));
    }

}

