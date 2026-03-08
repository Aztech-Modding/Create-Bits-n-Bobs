package com.kipti.bnb.mixin.articulate;

import com.kipti.bnb.mixin_accessor.ArticulatedCarriageBogey;
import com.kipti.bnb.mixin_accessor.ArticulatedTrackEdge;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.TravellingPoint;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Iterate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CarriageBogey.class)
public abstract class CarriageBogeyMixin implements ArticulatedCarriageBogey {

    @Shadow
    public Carriage carriage;

    @Shadow
    public abstract TravellingPoint leading();

    @Shadow
    public abstract TravellingPoint trailing();

    @Shadow
    public abstract @Nullable ResourceKey<Level> getDimension();

    @Unique
    private final LerpedFloat articulate$roll = LerpedFloat.angular();

    @Inject(method = "updateAngles", at = @At("TAIL"))
    private void articulate$updateRoll(final CarriageContraptionEntity entity, final double distanceMoved, final CallbackInfo ci) {
        final float targetRoll = this.articulate$resolveTargetRoll(entity);
        for (final boolean twice : Iterate.trueAndFalse) {
            if (twice && !entity.firstPositionUpdate) {
                continue;
            }
            this.articulate$roll.setValue(targetRoll);
        }
    }

    @Override
    public float articulate$getRoll() {
        return this.articulate$roll.getValue();
    }

    @Override
    public float articulate$getRoll(final float partialTicks) {
        return this.articulate$roll.getValue(partialTicks);
    }

    @Unique
    private float articulate$resolveTargetRoll(final CarriageContraptionEntity entity) {
        final TravellingPoint leadingPoint = this.leading();
        final TravellingPoint trailingPoint = this.trailing();
        if (leadingPoint.edge == null || trailingPoint.edge == null) {
            return 0f;
        }

        if (this.carriage == null || this.carriage.train == null || this.carriage.train.derailed) {
            return 0f;
        }

        final ResourceKey<Level> dimension = this.getDimension();
        if (dimension == null || !entity.level().dimension().equals(dimension)) {
            return 0f;
        }

        if (this.carriage.train.graph == null) {
            return 0f;
        }

        final float leadingTilt = this.articulate$resolvePointTilt(leadingPoint);
        final float trailingTilt = this.articulate$resolvePointTilt(trailingPoint);
        return (leadingTilt + trailingTilt) * 0.5f;
    }

    @Unique
    private float articulate$resolvePointTilt(final @Nullable TravellingPoint point) {
        if (point == null || point.edge == null) {
            return 0f;
        }

        final double edgeLength = point.edge.getLength();
        if (edgeLength <= 0d) {
            return 0f;
        }

        final double t = Mth.clamp(point.position / edgeLength, 0.0d, 1.0d);
        return ((ArticulatedTrackEdge) point.edge).articulate$getTilt(t);
    }

}
