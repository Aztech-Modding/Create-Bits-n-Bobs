package com.kipti.bnb.content.kinetics.cogwheel_chain.carriage;

import com.kipti.bnb.registry.client.BnbPartialModels;
import com.kipti.bnb.registry.content.blocks.BnbKineticBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.content.contraptions.render.ContraptionEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Renders the dynamic shoe arm and shoe elements of a cogwheel chain carriage.
 * Static block elements (base plate, column, bracket) are rendered as assembled
 * contraption blocks via {@link ContraptionEntityRenderer#render}.
 */
public class CogwheelChainCarriageRenderer extends ContraptionEntityRenderer<CogwheelChainCarriageContraptionEntity> {

    public CogwheelChainCarriageRenderer(final EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(final CogwheelChainCarriageContraptionEntity entity, final float entityYaw,
                       final float partialTicks, final PoseStack ms,
                       final MultiBufferSource buffers, final int packedLight) {
        super.render(entity, entityYaw, partialTicks, ms, buffers, packedLight);

        this.renderShoeArm(entity, partialTicks, ms, buffers, packedLight);
        this.renderShoe(entity, partialTicks, ms, buffers, packedLight, CogwheelChainCarriageContraption.FRONT_SHOE_OFFSET);
        this.renderShoe(entity, partialTicks, ms, buffers, packedLight, CogwheelChainCarriageContraption.BACK_SHOE_OFFSET);
    }

    private void renderShoeArm(final CogwheelChainCarriageContraptionEntity entity, final float partialTicks,
                               final PoseStack ms, final MultiBufferSource buffers, final int packedLight) {
        BlockState blockState = BnbKineticBlocks.COGWHEEL_CHAIN_CARRIAGE.getDefaultState();
        VertexConsumer vertexConsumer = buffers.getBuffer(RenderType.solid());
        float interpolatedYaw = entity.getInterpolatedYaw(partialTicks);

        ms.pushPose();
        ms.mulPose(Axis.YP.rotationDegrees(interpolatedYaw));

        CachedBuffers.partial(BnbPartialModels.COGWHEEL_CHAIN_CARRIAGE_SHOE_ARM, blockState)
                .light(packedLight)
                .renderInto(ms, vertexConsumer);

        ms.popPose();
    }

    private void renderShoe(final CogwheelChainCarriageContraptionEntity entity, final float partialTicks,
                            final PoseStack ms, final MultiBufferSource buffers, final int packedLight,
                            final float zOffset) {
        BlockState blockState = BnbKineticBlocks.COGWHEEL_CHAIN_CARRIAGE.getDefaultState();
        VertexConsumer vertexConsumer = buffers.getBuffer(RenderType.solid());
        float interpolatedYaw = entity.getInterpolatedYaw(partialTicks);

        ms.pushPose();
        ms.mulPose(Axis.YP.rotationDegrees(interpolatedYaw));
        ms.translate(0, 0, zOffset);

        CachedBuffers.partial(BnbPartialModels.COGWHEEL_CHAIN_CARRIAGE_SHOE, blockState)
                .light(packedLight)
                .renderInto(ms, vertexConsumer);

        ms.popPose();
    }
}
