package com.kipti.bnb.content.decoration.light.headlamp.rendering.pipeline.block_entity;

import com.kipti.bnb.content.decoration.light.headlamp.CCLightAddressing;
import com.kipti.bnb.content.decoration.light.headlamp.HeadlampBlockEntity;
import com.kipti.bnb.content.decoration.light.headlamp.rendering.HeadlampConstants;
import com.kipti.bnb.registry.BnbPartialModels;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2i;

import java.util.List;

/**
 * Code for rendering the headlamp model geometry into a vertex consumer.
 * Bases the rendered result on purely the packed render state long from {@link HeadlampBlockEntity#getRenderStateAsLong()}.
 * The resulting geometry contains no positional or rotational data and can be reused across headlamps facing different directions by applying the appropriate transform at render time.
 */
public class HeadlampModelBuilder {

    private static final RandomSource RANDOM = RandomSource.createNewThreadLocalInstance();

    /**
     * Builds headlamp geometry facing up into the provided {@link BufferBuilder}.
     * All state is decoded from the packed {@code renderState} long (see
     * {@link HeadlampBlockEntity#getRenderStateAsLong()}) so the resulting buffer
     * contains no rotational or positional data and can be reused across
     * headlamps facing different directions.
     */
    public static void buildHeadlampGeometry(final VertexConsumer builder, final long renderState) {
        final int onOffBits = (int) (renderState & 0xFL);
        final HeadlampBlockEntity.HeadlampPlacement[] allPlacements = HeadlampBlockEntity.HeadlampPlacement.values();

        for (int i = 0; i < HeadlampConstants.PLACEMENT_COUNT; i++) {
            final int placementValue = (int) ((renderState >> (HeadlampConstants.RENDER_STATE_ON_OFF_BITS + i * HeadlampConstants.RENDER_STATE_SLOT_BITS)) & HeadlampConstants.SLOT_VALUE_MASK);
            if (placementValue == 0) {
                continue;
            }

            final HeadlampBlockEntity.HeadlampPlacement placement = allPlacements[i];
            final boolean shouldDisplayOn = getLightOnOffState(onOffBits, placement);

            // Position the headlamp within the block, facing up
            final Matrix4f transform = new Matrix4f()
                    .translation(
                            (float) placement.horizontalAlignment().getOffset(),
                            0.0f,
                            (float) placement.verticalAlignment().getOffset()
                    );

            @Nullable final DyeColor color = placementValue == 1 ? null :
                    DyeColor.values()[Math.clamp(placementValue - HeadlampConstants.DYE_COLOR_OFFSET, 0, DyeColor.values().length - 1)];

            final List<BakedQuad> sourceQuads = (shouldDisplayOn
                    ? BnbPartialModels.HEADLAMP_ON
                    : BnbPartialModels.HEADLAMP_OFF
            ).get().getQuads(null, null, RANDOM, ModelData.EMPTY, null);

            for (final BakedQuad quad : sourceQuads) {
                emitTransformedQuad(builder, quad, transform, color, shouldDisplayOn);
            }
        }
    }

    /**
     * Returns whether the light at the given placement should display as on,
     * based on the precalculated on/off bits from the packed render state.
     */
    private static boolean getLightOnOffState(final int onOffBits, final HeadlampBlockEntity.HeadlampPlacement placement) {
        final Vector2i coord = CCLightAddressing.getLocalMaskCoordinateForPlacement(placement);
        return CCLightAddressing.getMaskValue((byte) onOffBits, coord);
    }

    private static void emitTransformedQuad(
            final VertexConsumer builder,
            final BakedQuad quad,
            final Matrix4f transform,
            final @Nullable DyeColor color,
            final boolean isOn
    ) {
        final int[] vertices = quad.getVertices();
        final TextureAtlasSprite oldSprite = quad.getSprite();
        final TextureAtlasSprite newSprite = HeadlampRenderCache.getTintedSprite(oldSprite, color);

        final BakedQuad quadToRender = oldSprite.equals(newSprite) ? quad :
                new BakedQuad(vertices, -1, quad.getDirection(), newSprite, quad.isShade());

        // Build a simple translation pose (we only used a translation in the original transform)
        final PoseStack poseStack = new PoseStack();
        // extract translation components from the JOML matrix (translation only expected)
        final float tx = transform.m30();
        final float ty = transform.m31();
        final float tz = transform.m32();
        poseStack.pushPose();
        poseStack.translate(tx, ty, tz);
        final PoseStack.Pose pose = poseStack.last();

        // Use the PoseStack.Pose-based bulk API to emit the quad in one call.
        builder.putBulkData(pose, quadToRender, 1.0f, 1.0f, 1.0f, 1.0f, isOn ? 0xF000F0 : 0, OverlayTexture.NO_OVERLAY, false);
        poseStack.popPose();
    }

}
