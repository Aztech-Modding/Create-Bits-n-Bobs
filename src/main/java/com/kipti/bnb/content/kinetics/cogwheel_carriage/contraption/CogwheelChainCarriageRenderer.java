package com.kipti.bnb.content.kinetics.cogwheel_carriage.contraption;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.render.ContraptionEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
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
    }

    private float getDirYRot(final Vec3 dir) {
        return (float) Math.toDegrees(Math.atan2(dir.x, dir.z));
    }
}