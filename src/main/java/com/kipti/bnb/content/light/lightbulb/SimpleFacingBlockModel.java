package com.kipti.bnb.content.light.lightbulb;

import com.kipti.bnb.content.nixie.foundation.DoubleOrientedDirections;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.model.BakedQuadHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.joml.Quaternionf;

/**
 * Based heavily off of
 */
public class SimpleFacingBlockModel extends BakedModelWrapper<BakedModel> {

    private static final ModelProperty<Quaternionf> FACING_ROTATION = new ModelProperty<>();

    public SimpleFacingBlockModel(final BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public @NotNull ModelData getModelData(final BlockAndTintGetter level, final BlockPos pos, final BlockState state, final ModelData modelData) {
        return ModelData.builder().with(FACING_ROTATION, Direction.UP.getRotation()).build();
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, final RandomSource rand, final ModelData data, @Nullable final RenderType renderType) {
        if (data.has(FACING_ROTATION)) {
            Quaternionf rotation = data.get(FACING_ROTATION);
            if (rotation != null) {
                PoseStack poseStack = new PoseStack();
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.mulPose(rotation);
                poseStack.translate(-0.5, -0.5, -0.5);
                return new ArrayList<>(transformQuads(super.getQuads(state, side, rand, data, renderType), poseStack));
            }
        }
        return Collections.emptyList();
    }

    private List<BakedQuad> transformQuads(List<BakedQuad> quads, PoseStack poseStack) {
        final Matrix4f pose = poseStack.last().pose();
        List<BakedQuad> transformedQuads = new ArrayList<>();
        for (BakedQuad quad : quads) {
            int[] vertices = quad.getVertices();
            int[] transformedVertices = vertices.clone();

            for (int i = 0; i < vertices.length / 8; i++) {
                float x = (float) BakedQuadHelper.getXYZ(vertices, i).x;
                float y = (float) BakedQuadHelper.getXYZ(vertices, i).y;
                float z = (float) BakedQuadHelper.getXYZ(vertices, i).z;

                Vector3f transformed = pose.transformPosition(new Vector3f(x, y, z));
                BakedQuadHelper.setXYZ(transformedVertices, i, new net.minecraft.world.phys.Vec3(transformed.x, transformed.y, transformed.z));
            }

            transformedQuads.add(new BakedQuad(transformedVertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), false));
        }
        return transformedQuads;
    }

}
