package com.kipti.bnb.content.trinkets.light.headlamp.rendering.pipeline.block_entity;

import com.kipti.bnb.content.trinkets.light.founation.LightBlock;
import com.kipti.bnb.content.trinkets.light.headlamp.HeadlampBlock;
import com.kipti.bnb.content.trinkets.light.headlamp.HeadlampBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import static com.kipti.bnb.content.trinkets.light.headlamp.rendering.pipeline.block_entity.HeadlampModelBuilder.buildHeadlampGeometry;

/**
 * Block entity renderer for headlamps that uses a cached static {@link SuperByteBuffer}
 * keyed by the packed render state from {@link HeadlampBlockEntity#getRenderStateAsLong()}.
 * <p>
 * Vertex buffers are built facing up with no rotational or positional data, so the same
 * buffer can be reused across headlamps facing different directions by applying the
 * appropriate rotation transform at render time.
 */
public class HeadlampBlockEntityRenderer extends SmartBlockEntityRenderer<HeadlampBlockEntity> {

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

        if (VisualizationManager.supportsVisualization(blockEntity.getLevel())) return;

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
                    .rotateYDegrees(facing.get2DDataValue() != -1 ? facing.get2DDataValue() * -90.0f : 0)
                    .uncenter();
        }
        final BlockState state = blockEntity.getBlockState();
        cached
                .light(LightBlock.isEmissive(state, null, null) ? LightTexture.FULL_BLOCK : light)
                .disableDiffuse()
                .renderInto(ms, buffer.getBuffer(RenderType.translucent()));
        ms.popPose();
    }

}

