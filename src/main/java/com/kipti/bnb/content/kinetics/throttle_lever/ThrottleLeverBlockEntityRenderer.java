package com.kipti.bnb.content.kinetics.throttle_lever;

import com.kipti.bnb.registry.client.BnbPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

/** Renders the throttle lever handle at the current animated power position. */
public class ThrottleLeverBlockEntityRenderer extends SmartBlockEntityRenderer<ThrottleLeverBlockEntity> {

    public ThrottleLeverBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(ThrottleLeverBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);

        float currentPower = blockEntity.getCurrentPower(partialTicks);
        BlockState leverState = blockEntity.getBlockState();
        AttachFace face = leverState.getValue(ThrottleLeverBlock.FACE);
        Direction facing = leverState.getValue(ThrottleLeverBlock.FACING);

        ms.pushPose();

        float rX = face == AttachFace.FLOOR ? 0 : face == AttachFace.WALL ? 90 : 180;
        float rY = AngleHelper.horizontalAngle(facing);

        TransformStack tstack = TransformStack.of(ms);
        tstack.center();
        tstack.rotateYDegrees(rY);
        tstack.rotateXDegrees(rX);

        tstack.translate(0, -6 / 16f, 0);
        tstack.rotateXDegrees(-45 + (currentPower / 15f) * 90);
        tstack.translate(0, 6 / 16f, 0);
        tstack.uncenter();

        CachedBuffers.partial(BnbPartialModels.THROTTLE_LEVER_HANDLE, leverState)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

        ms.popPose();
    }
}

