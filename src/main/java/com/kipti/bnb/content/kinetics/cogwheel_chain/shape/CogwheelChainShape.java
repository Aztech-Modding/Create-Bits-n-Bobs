package com.kipti.bnb.content.kinetics.cogwheel_chain.shape;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;

public abstract class CogwheelChainShape {

    @Nullable
    public abstract Vec3 intersect(Vec3 from, Vec3 to);

    public abstract float getChainPosition(Vec3 intersection);

    protected abstract void drawOutline(BlockPos anchor, PoseStack ms, VertexConsumer vb);

    public abstract Vec3 getVec(BlockPos anchor, float position);

}
