package com.kipti.bnb.content.decoration.truss;

import com.cake.azimuth.utility.client.model.QuadTransformer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class TrussBlockModel extends BakedModelWrapper<BakedModel> {

    protected static final ModelProperty<Boolean> ALTERNATING = new ModelProperty<>();
    protected static final ModelProperty<Direction.Axis> ALTERNATING_AXIS = new ModelProperty<>();

    public TrussBlockModel(final BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public @NonNull ModelData getModelData(final @NonNull BlockAndTintGetter level,
                                           final BlockPos pos,
                                           final BlockState state,
                                           final @NonNull ModelData modelData) {
        return ModelData.builder()
                .with(ALTERNATING, pos.get(state.getValue(TrussBlock.AXIS)) % 2 == 0)
                .with(ALTERNATING_AXIS, state.getValue(TrussBlock.AXIS))
                .build();
    }

    @Override
    public @NonNull List<BakedQuad> getQuads(@Nullable final BlockState state,
                                             @Nullable final Direction side,
                                             final @NonNull RandomSource rand,
                                             final @NonNull ModelData extraData,
                                             @Nullable final RenderType renderType) {
        List<BakedQuad> quads = super.getQuads(state, side, rand, extraData, renderType);

        quads = alternateQuads(extraData, quads, null);

        return quads;
    }

    public static List<BakedQuad> alternateQuads(@NonNull final ModelData extraData,
                                                 List<BakedQuad> quads,
                                                 final @Nullable Direction.Axis axisToRotateTo) {
        final Matrix4f transform = new Matrix4f();
        transform.translate(0.5f, 0.5f, 0.5f);
        if (extraData.has(ALTERNATING) && Boolean.TRUE.equals(extraData.get(ALTERNATING))) {
            final Direction.Axis axis = extraData.get(ALTERNATING_AXIS);
            transform.rotate(
                    (float) Math.PI / 2,
                    axis == Direction.Axis.X ? 1 : 0,
                    axis == Direction.Axis.Y ? 1 : 0,
                    axis == Direction.Axis.Z ? 1 : 0
            );
        }
        if (axisToRotateTo != null) {
            transform.rotate(new Quaternionf()
                                     .rotateX(axisToRotateTo == Direction.Axis.Y ? 0 : (float) (Math.PI / 2))
                                     .rotateZ((float) (axisToRotateTo == Direction.Axis.X ? Math.PI / 2 : axisToRotateTo == Direction.Axis.Z ? Math.PI : 0)));


        }
        transform.translate(-0.5f, -0.5f, -0.5f);
        quads = QuadTransformer.transform(
                quads, transform
        );
        return quads;
    }
}
