package com.kipti.bnb.mixin.articulate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.trains.track.TrackBlockOutline;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fixes curve outline rendering to include the roll (tilt/banking) rotation.
 * <p>
 * Create's {@code drawCurveSelection()} applies only yaw and pitch from the
 * computed model angles, but omits the roll component. The actual track model
 * rendering (ties, rails, girders in {@code BezierConnection}) applies all three
 * via {@code rotateY / rotateX / rotateZ}. This mixin adds the missing roll
 * so that tilted curve outlines visually match their actual geometry.
 * <p>
 * For untilted curves the roll baseline is π/2 (due to how
 * {@code TrackRenderer.getModelAngles} derives roll from the face normal).
 * We subtract that baseline so untilted outlines are unaffected.
 */
@Mixin(TrackBlockOutline.class)
public abstract class TrackBlockOutlineMixin {

    @Inject(method = "drawCurveSelection",
            at = @At(value = "INVOKE",
                     target = "Lcom/simibubi/create/content/trains/track/TrackBlockOutline;renderShape(Lnet/minecraft/world/phys/shapes/VoxelShape;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Ljava/lang/Boolean;)V"))
    private static void articulate$applyRollToOutline(PoseStack ms, MultiBufferSource buffer, Vec3 camera, CallbackInfo ci) {
        TrackBlockOutline.BezierPointSelection selection = TrackBlockOutline.result;
        if (selection == null) {
            return;
        }

        // getModelAngles returns (pitch, yaw, roll) where the baseline roll for
        // a perfectly flat track is π/2. Subtracting that baseline yields the
        // effective tilt rotation: zero for untilted tracks, ±tiltAngle otherwise.
        float rollCorrection = (float) (selection.angles().z - Math.PI / 2.0);
        if (Math.abs(rollCorrection) < 1.0e-4f) {
            return;
        }

        // The PoseStack currently has: T(pos) * R_Y * R_X * T(centerOffset)
        // We want:                     T(pos) * R_Y * R_X * R_Z(tilt) * T(centerOffset)
        // So undo the center offset, apply the roll, then redo the offset.
        TransformStack.of(ms)
                .translate(0.5, 0.125, 0.5)
                .rotateZ(rollCorrection)
                .translate(-0.5, -0.125, -0.5);
    }
}
