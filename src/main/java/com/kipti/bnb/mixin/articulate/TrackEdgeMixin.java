package com.kipti.bnb.mixin.articulate;

import com.kipti.bnb.content.articulate.ArticulatedTrackLogic;
import com.kipti.bnb.content.articulate.ArticulatedTrackUtils;
import com.kipti.bnb.mixin_accessor.ArticulatedBezierConnection;
import com.kipti.bnb.mixin_accessor.ArticulatedTrackEdge;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.track.BezierConnection;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrackEdge.class)
public abstract class TrackEdgeMixin implements ArticulatedTrackEdge {

    @Shadow
    public TrackNode node1;

    @Shadow
    public TrackNode node2;

    @Shadow
    BezierConnection turn;

    @Shadow
    boolean interDimensional;

    @Shadow
    public abstract Vec3 getDirection(boolean fromFirst);

    @Override
    public float articulate$getEndpointTilt(final boolean fromFirst) {
        return this.articulate$getTilt(fromFirst ? 0.0d : 1.0d);
    }

    @Override
    public float articulate$getTilt(final double t) {
        if (this.interDimensional) {
            return 0f;
        }

        if (this.turn != null) {
            return ((ArticulatedBezierConnection) this.turn).articulate$getTiltAt(t);
        }

        return ArticulatedTrackLogic.straightTiltProfile(
                this.node1.getNormal(),
                this.getDirection(true),
                this.node2.getNormal(),
                this.getDirection(false)
        ).tiltAt(t);
    }

    @Inject(method = "canTravelTo", at = @At("RETURN"), cancellable = true)
    private void articulate$rejectUnsafeTiltDiscontinuities(final TrackEdge other,
                                                            final CallbackInfoReturnable<Boolean> cir) {
        if (!Boolean.TRUE.equals(cir.getReturnValue()) || this.interDimensional || other.isInterDimensional()) {
            return;
        }

        final float exitTilt = this.articulate$getEndpointTilt(false);
        final float entryTilt = ((ArticulatedTrackEdge) other).articulate$getEndpointTilt(true);
        cir.setReturnValue(ArticulatedTrackUtils.isValidTiltTransition(exitTilt, entryTilt, 1.0f));
    }

}
