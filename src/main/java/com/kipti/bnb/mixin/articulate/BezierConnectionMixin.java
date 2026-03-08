package com.kipti.bnb.mixin.articulate;

import com.kipti.bnb.content.articulate.ArticulatedTrackUtils;
import com.kipti.bnb.mixin_accessor.ArticulatedBezierConnection;
import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.TrackMaterial;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BezierConnection.class)
public abstract class BezierConnectionMixin implements ArticulatedBezierConnection {
    @Shadow
    public Couple<Vec3> axes;

    @Shadow
    public Couple<Vec3> normals;

    @Shadow
    private BezierConnection.SegmentAngles[] bakedSegments;
    @Shadow
    private BezierConnection.GirderAngles[] bakedGirders;
    @Unique
    private Couple<Float> articulate$tilt = Couple.create(0f, 0f);

    @Unique
    private Couple<Vec3> articulate$baseNormals;

    @Override
    public @NotNull Couple<Float> articulate$getTilt() {
        return this.articulate$tilt.copy();
    }

    @Override
    public @NotNull Couple<Vec3> articulate$getBaseNormals() {
        if (this.articulate$baseNormals == null) {
            this.articulate$baseNormals = this.normals.copy();
        }

        return this.articulate$baseNormals.copy();
    }

    @Override
    public void articulate$setBaseNormals(final @NotNull Couple<Vec3> baseNormals) {
        this.articulate$baseNormals = baseNormals.copy();
    }

    @Override
    public float articulate$getEndpointTilt(final boolean first) {
        return this.articulate$tilt.get(first);
    }

    @Override
    public float articulate$getTiltAt(final double t) {
        return ArticulatedTrackUtils.interpolateTilt(
                (float) Mth.clamp(t, 0.0d, 1.0d),
                this.articulate$tilt.getFirst(),
                this.articulate$tilt.getSecond()
        );
    }

    @Override
    public void articulate$setTilt(final @NotNull Couple<Float> tilt) {
        this.articulate$tilt = tilt.copy();
        this.articulate$applyTiltToNormals();
        this.bakedSegments = null;
        this.bakedGirders = null;
    }

    @Inject(method = "<init>(Lnet/createmod/catnip/data/Couple;Lnet/createmod/catnip/data/Couple;Lnet/createmod/catnip/data/Couple;Lnet/createmod/catnip/data/Couple;ZZLcom/simibubi/create/content/trains/track/TrackMaterial;)V",
            at = @At("TAIL"))
    private void articulate$captureBaseNormals(final Couple<BlockPos> positions, final Couple<Vec3> starts,
                                               final Couple<Vec3> axes, final Couple<Vec3> normals,
                                               final boolean primary, final boolean girder,
                                               final TrackMaterial material, final CallbackInfo ci) {
        this.articulate$baseNormals = this.normals.copy();
    }

    @Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/BlockPos;)V", at = @At("TAIL"))
    private void articulate$readTilt(final CompoundTag compound, final BlockPos localTo, final CallbackInfo ci) {
        final float firstTilt = compound.contains("TiltFirst") ? compound.getFloat("TiltFirst") : 0f;
        final float secondTilt = compound.contains("TiltSecond") ? compound.getFloat("TiltSecond") : 0f;
        final Couple<Float> tilt = Couple.create(firstTilt, secondTilt);
        this.articulate$setBaseNormals(this.articulate$deriveBaseNormals(tilt));
        this.articulate$setTilt(tilt);
    }

    @Inject(method = "write(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"))
    private void articulate$writeTilt(final BlockPos localTo, final CallbackInfoReturnable<CompoundTag> cir) {
        final CompoundTag compound = cir.getReturnValue();
        if (Float.compare(this.articulate$tilt.getFirst(), 0f) == 0 && Float.compare(this.articulate$tilt.getSecond(), 0f) == 0) {
            return;
        }

        compound.putFloat("TiltFirst", this.articulate$tilt.getFirst());
        compound.putFloat("TiltSecond", this.articulate$tilt.getSecond());
    }

    @Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("TAIL"))
    private void articulate$readTilt(final FriendlyByteBuf buffer, final CallbackInfo ci) {
        if (!buffer.readBoolean()) {
            return;
        }

        final Couple<Float> tilt = Couple.create(buffer.readFloat(), buffer.readFloat());
        this.articulate$setBaseNormals(this.articulate$deriveBaseNormals(tilt));
        this.articulate$setTilt(tilt);
    }

    @Inject(method = "write(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("TAIL"))
    private void articulate$writeTilt(final FriendlyByteBuf buffer, final CallbackInfo ci) {
        final boolean hasTilt = Float.compare(this.articulate$tilt.getFirst(), 0f) != 0
                || Float.compare(this.articulate$tilt.getSecond(), 0f) != 0;
        buffer.writeBoolean(hasTilt);
        if (!hasTilt) {
            return;
        }

        buffer.writeFloat(this.articulate$tilt.getFirst());
        buffer.writeFloat(this.articulate$tilt.getSecond());
    }

    @Inject(method = "secondary()Lcom/simibubi/create/content/trains/track/BezierConnection;", at = @At("RETURN"))
    private void articulate$copyTiltToSecondary(final CallbackInfoReturnable<BezierConnection> cir) {
        final ArticulatedBezierConnection articulatedConnection = (ArticulatedBezierConnection) cir.getReturnValue();
        articulatedConnection.articulate$setBaseNormals(this.articulate$getBaseNormals().swap());
        articulatedConnection.articulate$setTilt(this.articulate$reverseTilt(this.articulate$tilt));
    }

    @Inject(method = "clone()Lcom/simibubi/create/content/trains/track/BezierConnection;", at = @At("RETURN"))
    private void articulate$copyTiltToClone(final CallbackInfoReturnable<BezierConnection> cir) {
        final ArticulatedBezierConnection articulatedConnection = (ArticulatedBezierConnection) cir.getReturnValue();
        articulatedConnection.articulate$setBaseNormals(this.articulate$getBaseNormals());
        articulatedConnection.articulate$setTilt(this.articulate$tilt);
    }

    @Unique
    private @NotNull Couple<Float> articulate$reverseTilt(final @NotNull Couple<Float> tilt) {
        return Couple.create(tilt.getSecond(), tilt.getFirst());
    }

    @Unique
    private void articulate$applyTiltToNormals() {
        final Couple<Vec3> baseNormals = this.articulate$getBaseNormals();
        this.normals.setFirst(this.articulate$rotateNormal(baseNormals.getFirst(), this.axes.getFirst(), this.articulate$tilt.getFirst()));
        this.normals.setSecond(this.articulate$rotateNormal(baseNormals.getSecond(), this.axes.getSecond(), this.articulate$tilt.getSecond()));
    }

    @Unique
    private @NotNull Couple<Vec3> articulate$deriveBaseNormals(final @NotNull Couple<Float> tilt) {
        return Couple.create(
                this.articulate$rotateNormal(this.normals.getFirst(), this.axes.getFirst(), -tilt.getFirst()),
                this.articulate$rotateNormal(this.normals.getSecond(), this.axes.getSecond(), -tilt.getSecond())
        );
    }

    @Unique
    private @NotNull Vec3 articulate$rotateNormal(final @NotNull Vec3 normal, final @NotNull Vec3 axis, final float tiltDegrees) {
        if (normal == null) {
            return new Vec3(0, 1, 0);
        }
        if (axis == null) {
            return normal;
        }
        final Vec3 rotatedNormal = ArticulatedTrackUtils.rotateFaceNormalCanonical(normal, axis, tiltDegrees);
        if (rotatedNormal.lengthSqr() < 1.0e-7d) {
            return rotatedNormal;
        }

        return rotatedNormal.normalize();
    }

}
