package com.kipti.bnb.mixin.articulate;

import com.kipti.bnb.mixin_accessor.ArticulatedCarriageBogey;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity.ContraptionRotationState;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(OrientedContraptionEntity.class)
public abstract class OrientedContraptionEntityMixin {

    @Shadow
    public abstract float getInitialYaw();

    @Shadow
    public abstract float getViewXRot(float partialTicks);

    @Shadow
    public abstract float getViewYRot(float partialTicks);

    @Inject(method = "getRotationState", at = @At("RETURN"))
    private void articulate$applyRollToRotationState(final CallbackInfoReturnable<ContraptionRotationState> cir) {
        final Carriage carriage = this.articulate$getCarriage();
        if (carriage == null) {
            return;
        }

        cir.getReturnValue().xRotation = this.articulate$getCurrentRoll();
    }

    @Inject(method = "applyRotation", at = @At("HEAD"), cancellable = true)
    private void articulate$applyRollRotation(final @NotNull Vec3 localPos, final float partialTicks, final CallbackInfoReturnable<Vec3> cir) {
        if (this.articulate$getCarriage() == null) {
            return;
        }

        cir.setReturnValue(this.articulate$rotateWithRoll(localPos, partialTicks));
    }

    @Inject(method = "reverseRotation", at = @At("HEAD"), cancellable = true)
    private void articulate$reverseRollRotation(final @NotNull Vec3 localPos, final float partialTicks, final CallbackInfoReturnable<Vec3> cir) {
        if (this.articulate$getCarriage() == null) {
            return;
        }

        cir.setReturnValue(this.articulate$reverseRotateWithRoll(localPos, partialTicks));
    }

    @Inject(
            method = "applyLocalTransforms",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/engine_room/flywheel/lib/transform/PoseTransformStack;rotateYDegrees(F)Ldev/engine_room/flywheel/lib/transform/Rotate;",
                    ordinal = 1,
                    shift = At.Shift.BEFORE
            )
    )
    private void articulate$applyRollTransform(final PoseStack matrixStack, final float partialTicks, final CallbackInfo ci) {
        if (this.articulate$getCarriage() == null) {
            return;
        }

        final float bodyRoll = this.articulate$getViewRoll(partialTicks);
        if (Mth.equal(bodyRoll, 0f)) {
            return;
        }

        TransformStack.of(matrixStack).rotateXDegrees(bodyRoll);
    }

    @Unique
    private @Nullable Carriage articulate$getCarriage() {
        if (!((Object) this instanceof final CarriageContraptionEntity carriageContraptionEntity)) {
            return null;
        }

        return carriageContraptionEntity.getCarriage();
    }

    @Unique
    private float articulate$getCurrentRoll() {
        return this.articulate$getBodyRoll(this.articulate$getCarriage());
    }

    @Unique
    private float articulate$getViewRoll(final float partialTicks) {
        return this.articulate$getBodyRoll(this.articulate$getCarriage(), partialTicks);
    }

    @Unique
    private float articulate$getBodyRoll(final @Nullable Carriage carriage) {
        if (carriage == null) {
            return 0f;
        }

        final CarriageBogey leadingBogey = carriage.leadingBogey();
        final CarriageBogey trailingBogey = carriage.bogeys.getSecond();
        final float leadingRoll = this.articulate$getBogeyRoll(leadingBogey);
        if (trailingBogey == null) {
            return leadingRoll;
        }

        return (leadingRoll + this.articulate$getBogeyRoll(trailingBogey)) * 0.5f;
    }

    @Unique
    private float articulate$getBodyRoll(final @Nullable Carriage carriage, final float partialTicks) {
        if (carriage == null) {
            return 0f;
        }

        final CarriageBogey leadingBogey = carriage.leadingBogey();
        final CarriageBogey trailingBogey = carriage.bogeys.getSecond();
        final float leadingRoll = this.articulate$getBogeyRoll(leadingBogey, partialTicks);
        if (trailingBogey == null) {
            return leadingRoll;
        }

        return (leadingRoll + this.articulate$getBogeyRoll(trailingBogey, partialTicks)) * 0.5f;
    }

    @Unique
    private float articulate$getBogeyRoll(final @Nullable CarriageBogey bogey) {
        if (!(bogey instanceof final ArticulatedCarriageBogey articulatedCarriageBogey)) {
            return 0f;
        }

        return articulatedCarriageBogey.articulate$getRoll();
    }

    @Unique
    private float articulate$getBogeyRoll(final @Nullable CarriageBogey bogey, final float partialTicks) {
        if (!(bogey instanceof final ArticulatedCarriageBogey articulatedCarriageBogey)) {
            return 0f;
        }

        return articulatedCarriageBogey.articulate$getRoll(partialTicks);
    }

    @Unique
    private @NotNull Vec3 articulate$rotateWithRoll(final @NotNull Vec3 localPos, final float partialTicks) {
        Vec3 rotatedPos = localPos;
        rotatedPos = this.articulate$rotateNonnull(rotatedPos, this.getInitialYaw(), Axis.Y);
        rotatedPos = this.articulate$rotateNonnull(rotatedPos, this.getViewXRot(partialTicks), Axis.Z);
        rotatedPos = this.articulate$rotateNonnull(rotatedPos, this.articulate$getViewRoll(partialTicks), Axis.X);
        rotatedPos = this.articulate$rotateNonnull(rotatedPos, this.getViewYRot(partialTicks), Axis.Y);
        return rotatedPos;
    }

    @Unique
    private @NotNull Vec3 articulate$reverseRotateWithRoll(final @NotNull Vec3 localPos, final float partialTicks) {
        Vec3 rotatedPos = localPos;
        rotatedPos = this.articulate$rotateNonnull(rotatedPos, -this.getViewYRot(partialTicks), Axis.Y);
        rotatedPos = this.articulate$rotateNonnull(rotatedPos, -this.articulate$getViewRoll(partialTicks), Axis.X);
        rotatedPos = this.articulate$rotateNonnull(rotatedPos, -this.getViewXRot(partialTicks), Axis.Z);
        rotatedPos = this.articulate$rotateNonnull(rotatedPos, -this.getInitialYaw(), Axis.Y);
        return rotatedPos;
    }

    @Unique
    private @NotNull Vec3 articulate$rotateNonnull(final @NotNull Vec3 localPos, final float angle, final @NotNull Axis axis) {
        return Objects.requireNonNull(VecHelper.rotate(localPos, angle, axis));
    }

}
