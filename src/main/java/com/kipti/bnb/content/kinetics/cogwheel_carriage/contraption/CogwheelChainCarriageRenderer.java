package com.kipti.bnb.content.kinetics.cogwheel_carriage.contraption;

import com.kipti.bnb.registry.client.BnbPartialModels;
import com.kipti.bnb.registry.content.blocks.BnbKineticBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.contraptions.render.ContraptionEntityRenderer;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

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
    public void render(final @NonNull CogwheelChainCarriageContraptionEntity entity, final float entityYaw,
                       final float partialTicks, final @NonNull PoseStack ms,
                       final @NonNull MultiBufferSource buffers, final int packedLight) {
        super.render(entity, entityYaw, partialTicks, ms, buffers, packedLight);

        this.renderShoeArm(entity, partialTicks, ms, buffers, packedLight);
        this.renderShoe(
                entity,
                partialTicks,
                ms,
                buffers,
                packedLight,
                CogwheelChainCarriageContraptionEntity.SHOE_OFFSET,
                entity.getFrontShoeDir(partialTicks)
        );
        this.renderShoe(
                entity,
                partialTicks,
                ms,
                buffers,
                packedLight,
                -CogwheelChainCarriageContraptionEntity.SHOE_OFFSET,
                entity.getBackShoeDir(partialTicks)
        );
    }

    private void renderShoeArm(final CogwheelChainCarriageContraptionEntity entity, final float partialTicks,
                               final PoseStack ms, final MultiBufferSource buffers, final int packedLight) {
        final BlockState blockState = BnbKineticBlocks.COGWHEEL_CHAIN_CARRIAGE.getDefaultState();
        final VertexConsumer vertexConsumer = buffers.getBuffer(RenderType.cutout());
        ms.pushPose();
        entity.applyLocalTransforms(ms, partialTicks);

        TransformStack.of(ms)
                .center()
                .rotateToFace(entity.getInitialOrientation().getOpposite())
                .uncenter()
                .translate(0, 0.625, 0);
        CachedBuffers.partial(BnbPartialModels.COGWHEEL_CHAIN_CARRIAGE_SHOE_ARM, blockState)
                .light(packedLight)
                .renderInto(ms, vertexConsumer);

        ms.popPose();
    }

    private void renderShoe(final CogwheelChainCarriageContraptionEntity entity, final float partialTicks,
                            final PoseStack ms, final MultiBufferSource buffers, final int packedLight,
                            final float zOffset, final Vec3 dir) {
        final BlockState blockState = BnbKineticBlocks.COGWHEEL_CHAIN_CARRIAGE.getDefaultState();
        final VertexConsumer vertexConsumer = buffers.getBuffer(RenderType.solid());

        ms.pushPose();
        entity.applyLocalTransforms(ms, partialTicks);

        TransformStack.of(ms)
                .center()
                .rotateToFace(entity.getInitialOrientation().getOpposite())
                .uncenter();

        ms.translate(0, 0, zOffset);

        TransformStack.of(ms)
                .center()
                .rotateY((float) Math.toRadians(this.getDirYRot(dir) - entity.getViewYRot(partialTicks)))
                .uncenter();

        CachedBuffers.partial(BnbPartialModels.COGWHEEL_CHAIN_CARRIAGE_SHOE, blockState)
                .light(packedLight)
                .renderInto(ms, vertexConsumer);

        ms.popPose();
    }

    private float getDirYRot(final Vec3 dir) {
        return (float) Math.toDegrees(Math.atan2(dir.x, dir.z));
    }
}