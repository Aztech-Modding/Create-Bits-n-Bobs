package com.kipti.bnb.mixin.articulate;

import com.kipti.bnb.mixin_accessor.ArticulatedCarriageBogey;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntityRenderer;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CarriageContraptionEntityRenderer.class)
public abstract class CarriageContraptionEntityRendererMixin {

    @Inject(
            method = "translateBogey",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/engine_room/flywheel/lib/transform/PoseTransformStack;rotateZDegrees(F)Ldev/engine_room/flywheel/lib/transform/Rotate;",
                    shift = At.Shift.BEFORE
            )
    )
    private static void articulate$applyBogeyRoll(final PoseStack ms, final CarriageBogey bogey, final int bogeySpacing,
                                                  final float viewYRot, final float viewXRot, final float partialTicks,
                                                  final CallbackInfo ci) {
        if (!(bogey instanceof final ArticulatedCarriageBogey articulatedCarriageBogey)) {
            return;
        }

        float bogeyRoll = articulatedCarriageBogey.articulate$getRoll(partialTicks);
        if (bogey.isUpsideDown()) {
            bogeyRoll = -bogeyRoll;
        }

        if (Mth.equal(bogeyRoll, 0f)) {
            return;
        }

        TransformStack.of(ms).rotateZDegrees(bogeyRoll);
    }

}
