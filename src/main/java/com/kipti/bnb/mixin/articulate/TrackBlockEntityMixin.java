package com.kipti.bnb.mixin.articulate;

import com.kipti.bnb.content.articulate.ArticulatedTrackBehaviour;
import com.kipti.bnb.content.articulate.ArticulatedTrackModelData;
import com.simibubi.create.content.trains.track.TrackBlockEntity;
import com.simibubi.create.content.trains.track.TrackBlockEntityTilt;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrackBlockEntity.class)
public abstract class TrackBlockEntityMixin {

    @Inject(method = "getModelData", at = @At("RETURN"), cancellable = true)
    private void articulate$addTiltModelData(final CallbackInfoReturnable<ModelData> cir) {
        final TrackBlockEntity self = (TrackBlockEntity) (Object) this;
        final ArticulatedTrackBehaviour behaviour = ArticulatedTrackBehaviour.get(self);
        if (behaviour == null || !behaviour.hasTilt()) {
            return;
        }

        final ModelData original = cir.getReturnValue();
        final ModelData.Builder builder = ModelData.builder();
        if (original.has(TrackBlockEntityTilt.ASCENDING_PROPERTY)) {
            builder.with(TrackBlockEntityTilt.ASCENDING_PROPERTY, original.get(TrackBlockEntityTilt.ASCENDING_PROPERTY));
        }
        builder.with(ArticulatedTrackModelData.TILT_PROPERTY, behaviour.getTiltDegrees());
        cir.setReturnValue(builder.build());
    }

}
