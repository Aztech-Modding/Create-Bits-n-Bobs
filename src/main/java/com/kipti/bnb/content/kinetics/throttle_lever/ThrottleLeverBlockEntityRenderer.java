package com.kipti.bnb.content.kinetics.throttle_lever;

import com.kipti.bnb.registry.BnbPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public class ThrottleLeverBlockEntityRenderer extends SmartBlockEntityRenderer<ThrottleLeverBlockEntity> {

    public ThrottleLeverBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final ThrottleLeverBlockEntity blockEntity, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
        super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);

        //render THROTTLE_LEVER_HANDLE but rootated according to teh cyurrent power from -40 to 40 degrees
        final float currentPower = blockEntity.getCurrentPower(partialTicks);

        ms.pushPose();
        Direction facing = blockEntity.getBlockState().getValue(ThrottleLeverBlock.FACING);
        TransformStack.of(ms)
                .center()
                .rotateTo(Direction.UP, facing)
                .translate(0, -6 / 16f, 0);

        if (facing.get2DDataValue() != -1) {
            TransformStack.of(ms)
                    .rotateYDegrees((float) (facing.get2DDataValue() * 90));
        }
        TransformStack.of(ms)
                .rotateXDegrees((float) (-45 + (currentPower / 15f) * 90))
                .translate(0, 6 / 16f, 0)
                .uncenter();
        CachedBuffers.partial(BnbPartialModels.THROTTLE_LEVER_HANDLE, blockEntity.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
        ms.popPose();
    }
}
