package com.kipti.bnb.content.kinetics.flywheel_bearing;

import com.kipti.bnb.registry.BnbPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class FlywheelBearingBlockEntityRenderer extends KineticBlockEntityRenderer<FlywheelBearingBlockEntity> {
    public FlywheelBearingBlockEntityRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final FlywheelBearingBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer,
                              final int light, final int overlay) {
        final BlockState state = getRenderedBlockState(be);
        final RenderType type = getRenderType(be, state);
        renderRotatingBuffer(be, getRotatedModel(be, state), ms, buffer.getBuffer(type), light);

        final Direction facing = state.getValue(FlywheelBearingBlock.FACING);
        CachedBuffers.partialFacingVertical(AllPartialModels.BEARING_TOP, state, facing)
                .center()
                .rotate(facing.getAxis(), (float) Math.toRadians(be.getInterpolatedAngle(partialTicks)))
                .uncenter()
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }

    @Override
    protected SuperByteBuffer getRotatedModel(final FlywheelBearingBlockEntity be, final BlockState state) {
        return CachedBuffers.partialFacingVertical(BnbPartialModels.LARGE_STONE_COG_SHAFTLESS, state, state.getValue(FlywheelBearingBlock.FACING));
    }
}
