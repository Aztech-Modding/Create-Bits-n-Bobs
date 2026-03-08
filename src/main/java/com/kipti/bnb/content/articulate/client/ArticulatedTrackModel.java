package com.kipti.bnb.content.articulate.client;

import com.kipti.bnb.content.articulate.ArticulatedTrackModelData;
import com.kipti.bnb.content.articulate.ArticulatedTrackUtils;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackBlockEntityTilt;
import com.simibubi.create.content.trains.track.TrackShape;
import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import com.simibubi.create.foundation.model.BakedQuadHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class ArticulatedTrackModel extends BakedModelWrapperWithData {

    private static final Vec3 TRACK_CENTER = new Vec3(0.5d, 0.25d, 0.5d);
    private static final double VECTOR_EPSILON = 1.0e-7d;

    public ArticulatedTrackModel(final BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    protected @NotNull ModelData.Builder gatherModelData(final @NotNull ModelData.Builder builder,
                                                         final @NotNull BlockAndTintGetter world,
                                                         final @NotNull BlockPos pos,
                                                         final @NotNull BlockState state,
                                                         final @NotNull ModelData blockEntityData) {
        if (blockEntityData.has(TrackBlockEntityTilt.ASCENDING_PROPERTY)) {
            final Double ascendingAngle = blockEntityData.get(TrackBlockEntityTilt.ASCENDING_PROPERTY);
            if (ascendingAngle != null) {
                builder.with(TrackBlockEntityTilt.ASCENDING_PROPERTY, ascendingAngle);
            }
        }

        if (blockEntityData.has(ArticulatedTrackModelData.TILT_PROPERTY)) {
            final Float tiltDegrees = blockEntityData.get(ArticulatedTrackModelData.TILT_PROPERTY);
            if (tiltDegrees != null && !Mth.equal(tiltDegrees, 0f)) {
                builder.with(ArticulatedTrackModelData.TILT_PROPERTY, tiltDegrees);
            }
        }

        return builder;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable final BlockState state,
                                             @Nullable final Direction side,
                                             final @NotNull RandomSource rand,
                                             final @NotNull ModelData extraData,
                                             @Nullable final RenderType renderType) {
        final List<BakedQuad> templateQuads = super.getQuads(state, side, rand, extraData, renderType);
        if (state == null || templateQuads.isEmpty() || !extraData.has(ArticulatedTrackModelData.TILT_PROPERTY)) {
            return templateQuads;
        }
        if (!(state.getBlock() instanceof TrackBlock) || !state.hasProperty(TrackBlock.SHAPE)) {
            return templateQuads;
        }

        final Float tiltDegrees = extraData.get(ArticulatedTrackModelData.TILT_PROPERTY);
        if (tiltDegrees == null || Mth.equal(tiltDegrees, 0f)) {
            return templateQuads;
        }

        final TrackShape shape = state.getValue(TrackBlock.SHAPE);
        if (shape.isJunction() || shape.isPortal()) {
            return templateQuads;
        }

        final Double ascendingAngle = extraData.has(TrackBlockEntityTilt.ASCENDING_PROPERTY)
                ? extraData.get(TrackBlockEntityTilt.ASCENDING_PROPERTY)
                : null;
        final Vec3 forwardAxis = articulate$getForwardAxis(shape, ascendingAngle);
        if (forwardAxis.lengthSqr() < VECTOR_EPSILON) {
            return templateQuads;
        }

        final Vec3 pivot = articulate$getPivot(shape, ascendingAngle);
        final UnaryOperator<Vec3> pointTransform = ArticulatedTrackUtils.tiltTransformCanonical(forwardAxis, tiltDegrees, pivot);
        final UnaryOperator<Vec3> slopeNormalTransform = articulate$buildSlopeNormalTransform(shape, ascendingAngle);
        final List<BakedQuad> quads = new ArrayList<>(templateQuads.size());

        for (final BakedQuad templateQuad : templateQuads) {
            quads.add(this.articulate$transformQuad(templateQuad, pointTransform, slopeNormalTransform, forwardAxis, tiltDegrees));
        }

        return quads;
    }

    private @NotNull BakedQuad articulate$transformQuad(final @NotNull BakedQuad templateQuad,
                                                        final @NotNull UnaryOperator<Vec3> pointTransform,
                                                        final @NotNull UnaryOperator<Vec3> slopeNormalTransform,
                                                        final @NotNull Vec3 forwardAxis,
                                                        final float tiltDegrees) {
        final int[] vertexData = templateQuad.getVertices().clone();

        for (int vertex = 0; vertex < 4; vertex++) {
            final Vec3 transformedPosition = pointTransform.apply(BakedQuadHelper.getXYZ(vertexData, vertex));
            BakedQuadHelper.setXYZ(vertexData, vertex, transformedPosition);

            final Vec3 baseNormal = BakedQuadHelper.getNormalXYZ(vertexData, vertex);
            final Vec3 transformedNormal = this.articulate$transformNormal(baseNormal, slopeNormalTransform, forwardAxis, tiltDegrees);
            BakedQuadHelper.setNormalXYZ(vertexData, vertex, transformedNormal);
        }

        final Vec3 baseDirection = Vec3.atLowerCornerOf(templateQuad.getDirection().getNormal());
        final Vec3 transformedDirectionVector = this.articulate$transformNormal(baseDirection, slopeNormalTransform, forwardAxis, tiltDegrees);
        final Direction transformedDirection = Direction.getNearest(
                (float) transformedDirectionVector.x,
                (float) transformedDirectionVector.y,
                (float) transformedDirectionVector.z
        );
        return new BakedQuad(
                vertexData,
                templateQuad.getTintIndex(),
                transformedDirection,
                templateQuad.getSprite(),
                templateQuad.isShade()
        );
    }

    private @NotNull Vec3 articulate$transformNormal(final @NotNull Vec3 baseNormal,
                                                     final @NotNull UnaryOperator<Vec3> slopeNormalTransform,
                                                     final @NotNull Vec3 forwardAxis,
                                                     final float tiltDegrees) {
        final Vec3 slopedNormal = slopeNormalTransform.apply(baseNormal);
        final Vec3 tiltedNormal = ArticulatedTrackUtils.rotateFaceNormalCanonical(slopedNormal, forwardAxis, tiltDegrees);
        if (tiltedNormal.lengthSqr() < VECTOR_EPSILON) {
            return tiltedNormal;
        }
        return tiltedNormal.normalize();
    }

    private static @NotNull Vec3 articulate$getPivot(final @NotNull TrackShape shape, final @Nullable Double ascendingAngle) {
        return articulate$buildSlopePointTransform(shape, ascendingAngle).apply(TRACK_CENTER);
    }

    private static @NotNull Vec3 articulate$getForwardAxis(final @NotNull TrackShape shape, final @Nullable Double ascendingAngle) {
        final Vec3 baseForwardAxis = articulate$getBaseForwardAxis(shape);
        if (baseForwardAxis.lengthSqr() < VECTOR_EPSILON) {
            return baseForwardAxis;
        }

        final UnaryOperator<Vec3> slopePointTransform = articulate$buildSlopePointTransform(shape, ascendingAngle);
        final Vec3 pivot = slopePointTransform.apply(TRACK_CENTER);
        final Vec3 transformedForward = slopePointTransform.apply(TRACK_CENTER.add(baseForwardAxis));
        final Vec3 forwardAxis = transformedForward.subtract(pivot);
        if (forwardAxis.lengthSqr() < VECTOR_EPSILON) {
            return forwardAxis;
        }
        return forwardAxis.normalize();
    }

    private static @NotNull Vec3 articulate$getBaseForwardAxis(final @NotNull TrackShape shape) {
        if (shape.getAxes().isEmpty()) {
            return Vec3.ZERO;
        }
        return shape.getAxes().getFirst().normalize();
    }

    private static @NotNull UnaryOperator<Vec3> articulate$buildSlopePointTransform(final @NotNull TrackShape shape,
                                                                                     final @Nullable Double ascendingAngle) {
        if (ascendingAngle == null || Mth.equal(ascendingAngle.floatValue(), 0f)) {
            return UnaryOperator.identity();
        }

        final double slopeAngle = Math.abs(ascendingAngle);
        final boolean flip = ascendingAngle < 0d;
        final double horizontalAngle = articulate$getHorizontalAngle(shape);
        final Vec3 verticalOffset = new Vec3(0d, -0.25d, 0d);
        final Vec3 diagonalRotationPoint = articulate$getDiagonalRotationPoint(shape);

        return point -> {
            Vec3 transformed = point.add(verticalOffset);
            transformed = VecHelper.rotateCentered(transformed, horizontalAngle, Axis.Y);
            transformed = transformed.add(diagonalRotationPoint);
            transformed = VecHelper.rotate(transformed, slopeAngle, Axis.Z);
            transformed = transformed.subtract(diagonalRotationPoint);
            transformed = VecHelper.rotateCentered(transformed, -horizontalAngle + (flip ? 180d : 0d), Axis.Y);
            return transformed.subtract(verticalOffset);
        };
    }

    private static @NotNull UnaryOperator<Vec3> articulate$buildSlopeNormalTransform(final @NotNull TrackShape shape,
                                                                                      final @Nullable Double ascendingAngle) {
        if (ascendingAngle == null || Mth.equal(ascendingAngle.floatValue(), 0f)) {
            return UnaryOperator.identity();
        }

        final double slopeAngle = Math.abs(ascendingAngle);
        final boolean flip = ascendingAngle < 0d;
        final double horizontalAngle = articulate$getHorizontalAngle(shape);

        return normal -> {
            Vec3 transformed = VecHelper.rotate(normal, horizontalAngle, Axis.Y);
            transformed = VecHelper.rotate(transformed, slopeAngle, Axis.Z);
            transformed = VecHelper.rotate(transformed, -horizontalAngle + (flip ? 180d : 0d), Axis.Y);
            return transformed;
        };
    }

    private static double articulate$getHorizontalAngle(final @NotNull TrackShape shape) {
        return switch (shape) {
            case XO -> 0d;
            case PD -> 45d;
            case ZO -> 90d;
            case ND -> 135d;
            default -> 0d;
        };
    }

    private static @NotNull Vec3 articulate$getDiagonalRotationPoint(final @NotNull TrackShape shape) {
        if (shape != TrackShape.ND && shape != TrackShape.PD) {
            return Vec3.ZERO;
        }
        return new Vec3((Mth.SQRT_OF_TWO - 1d) / 2d, 0d, 0d);
    }

}
