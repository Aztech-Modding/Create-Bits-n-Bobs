package com.kipti.bnb.content.kinetics.cogwheel_carriage.block;

import com.kipti.bnb.registry.client.BnbPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Just statically render the extra bits that are missing when in world
 *
 */
public class CogwheelChainCarriageRenderer extends SmartBlockEntityRenderer<CogwheelChainCarriageBlockEntity> {

    public CogwheelChainCarriageRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final CogwheelChainCarriageBlockEntity blockEntity,
                              final float partialTicks,
                              final PoseStack ms,
                              final MultiBufferSource buffer,
                              final int light,
                              final int overlay) {
        super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);

        if (blockEntity.getLevel() == null || blockEntity.getLevel() instanceof VirtualRenderWorld) //TODO: better hide detection
            return;

        final VertexConsumer cutout = buffer.getBuffer(RenderType.CUTOUT);
        final BlockState blockState = blockEntity.getBlockState();
        final Direction facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        ms.pushPose();
        TransformStack.of(ms)
                .center()
                .rotateToFace(facing)
                .uncenter();
        CachedBuffers.partial(BnbPartialModels.COGWHEEL_CHAIN_CARRIAGE_SHOE_ARM, blockState)
                .light(light)
                .renderInto(ms, cutout);

        ms.translate(0, 0, 0.5);
        CachedBuffers.partial(BnbPartialModels.COGWHEEL_CHAIN_CARRIAGE_SHOE, blockState)
                .light(light)
                .renderInto(ms, cutout);

        ms.translate(0, 0, -1);
        CachedBuffers.partial(BnbPartialModels.COGWHEEL_CHAIN_CARRIAGE_SHOE, blockState)
                .light(light)
                .renderInto(ms, cutout);
        ms.popPose();
    }

}
