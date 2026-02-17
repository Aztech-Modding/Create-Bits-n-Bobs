package com.kipti.bnb.content.decoration.light.headlamp;

import com.kipti.bnb.registry.BnbPartialModels;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.model.BakedQuadHelper;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

/**
 * Block entity renderer for headlamps that uses a cached static {@link SuperByteBuffer}
 * keyed by the packed render state from {@link HeadlampBlockEntity#getRenderStateAsLong()}.
 * <p>
 * Vertex buffers are built facing up with no rotational or positional data, so the same
 * buffer can be reused across headlamps facing different directions by applying the
 * appropriate rotation transform at render time.
 */
public class HeadlampBlockEntityRenderer extends SmartBlockEntityRenderer<HeadlampBlockEntity> {

    private static final int RENDER_STATE_ON_OFF_BITS = 4;
    private static final int RENDER_STATE_SLOT_BITS = 5;
    private static final long RENDER_STATE_SLOT_MASK = 0x1FL;
    private static final int PLACEMENT_COUNT = 9;

    private static final RandomSource RANDOM = RandomSource.createNewThreadLocalInstance();

    public HeadlampBlockEntityRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(
            final HeadlampBlockEntity blockEntity,
            final float partialTicks,
            final PoseStack ms,
            final MultiBufferSource buffer,
            final int light,
            final int overlay
    ) {
        super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);

        final long renderState = blockEntity.getRenderStateAsLong();
        if (renderState == 0L) {
            return; // No headlamps to render
        }

        final SuperByteBuffer cached = HeadlampVertexBufferCache.getOrCreate(
                renderState, bb -> buildHeadlampGeometry(bb, renderState)
        );

        if (cached == null) {
            return;
        }

        final Direction facing = blockEntity.getBlockState().getValue(HeadlampBlock.FACING);

        ms.pushPose();
        if (facing != Direction.UP) {
            TransformStack.of(ms)
                    .center()
                    .rotateTo(Direction.UP, facing)
                    .uncenter();
        }
        cached.light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        ms.popPose();
    }

    /**
     * Builds headlamp geometry facing up into the provided {@link BufferBuilder}.
     * All state is decoded from the packed {@code renderState} long so the resulting
     * buffer contains no rotational or positional data and can be reused across
     * headlamps facing different directions.
     */
    private static void buildHeadlampGeometry(final BufferBuilder builder, final long renderState) {
        final int onOffBits = (int) (renderState & 0xFL);
        final HeadlampBlockEntity.HeadlampPlacement[] allPlacements = HeadlampBlockEntity.HeadlampPlacement.values();

        for (int i = 0; i < PLACEMENT_COUNT; i++) {
            final int placementValue = (int) ((renderState >> (RENDER_STATE_ON_OFF_BITS + i * RENDER_STATE_SLOT_BITS)) & RENDER_STATE_SLOT_MASK);
            if (placementValue == 0) {
                continue;
            }

            final HeadlampBlockEntity.HeadlampPlacement placement = allPlacements[i];

            // Determine on/off per-placement using CC addressing bits where applicable
            final boolean shouldDisplayOn;
            final TriState ccState = getCCAddressingFromBits(onOffBits, placement);
            if (ccState != TriState.DEFAULT) {
                shouldDisplayOn = ccState == TriState.TRUE;
            } else {
                shouldDisplayOn = onOffBits == 0xF;
            }

            // Position the headlamp within the block, facing up
            final Matrix4f transform = new Matrix4f()
                    .translation(
                            (float) placement.horizontalAlignment().getOffset(),
                            0.0f,
                            (float) placement.verticalAlignment().getOffset()
                    );

            @Nullable final DyeColor color = placementValue == 1 ? null :
                    DyeColor.values()[Math.clamp(placementValue - 2, 0, DyeColor.values().length - 1)];

            final List<BakedQuad> sourceQuads = (shouldDisplayOn
                    ? BnbPartialModels.HEADLAMP_ON
                    : BnbPartialModels.HEADLAMP_OFF
            ).get().getQuads(null, null, RANDOM, ModelData.EMPTY, RenderType.solid());

            for (final BakedQuad quad : sourceQuads) {
                emitTransformedQuad(builder, quad, transform, color);
            }
        }
    }

    /**
     * Derives the CC addressing tri-state for a placement from the packed on/off bits.
     * When all 4 bits are uniformly on or off (0b1111 or 0b0000), the result is
     * {@link TriState#DEFAULT} indicating the light renderer controls the state.
     * Otherwise, the individual bit for this placement's mask coordinate is checked.
     */
    private static TriState getCCAddressingFromBits(final int onOffBits, final HeadlampBlockEntity.HeadlampPlacement placement) {
        if (onOffBits == 0xF || onOffBits == 0x0) {
            return TriState.DEFAULT;
        }
        final var coord = CCLightAddressing.getLocalMaskCoordinateForPlacement(placement);
        final boolean value = CCLightAddressing.getMaskValue((byte) onOffBits, coord);
        return value ? TriState.TRUE : TriState.FALSE;
    }

    private static void emitTransformedQuad(
            final BufferBuilder builder,
            final BakedQuad quad,
            final Matrix4f transform,
            final @Nullable DyeColor color
    ) {
        final int[] vertices = quad.getVertices();
        final TextureAtlasSprite oldSprite = quad.getSprite();
        final TextureAtlasSprite newSprite = HeadlampRenderCache.getTintedSprite(oldSprite, color);

        final Vector3f pos = new Vector3f();
        final Vector3f normal = new Vector3f();

        for (int i = 0; i < vertices.length / BakedQuadHelper.VERTEX_STRIDE; i++) {
            final Vec3 vertex = BakedQuadHelper.getXYZ(vertices, i);
            final Vec3 vertexNormal = BakedQuadHelper.getNormalXYZ(vertices, i);
            float uvX = BakedQuadHelper.getU(vertices, i);
            float uvY = BakedQuadHelper.getV(vertices, i);

            if (!oldSprite.equals(newSprite)) {
                uvX = (uvX - oldSprite.getU0()) / (oldSprite.getU1() - oldSprite.getU0())
                        * (newSprite.getU1() - newSprite.getU0()) + newSprite.getU0();
                uvY = (uvY - oldSprite.getV0()) / (oldSprite.getV1() - oldSprite.getV0())
                        * (newSprite.getV1() - newSprite.getV0()) + newSprite.getV0();
            }

            transform.transformPosition((float) vertex.x, (float) vertex.y, (float) vertex.z, pos);
            transform.transformDirection((float) vertexNormal.x, (float) vertexNormal.y, (float) vertexNormal.z, normal);

            builder.addVertex(pos.x, pos.y, pos.z)
                    .setColor(1.0f, 1.0f, 1.0f, 1.0f)
                    .setUv(uvX, uvY)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(0)
                    .setNormal(normal.x, normal.y, normal.z);
        }
    }
}
