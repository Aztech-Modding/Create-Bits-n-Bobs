package com.kipti.bnb.mixin.articulate;

import com.kipti.bnb.content.articulate.ArticulatedTrackPlacementPlan;
import com.kipti.bnb.mixin_accessor.ArticulatedTrackPlacementPlanHolder;
import com.simibubi.create.content.trains.track.TrackPlacement;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TrackPlacement.PlacementInfo.class)
public class TrackPlacementInfoMixin implements ArticulatedTrackPlacementPlanHolder {

    @Unique
    private ArticulatedTrackPlacementPlan articulate$placementPlan;

    @Override
    public @Nullable ArticulatedTrackPlacementPlan articulate$getPlacementPlan() {
        return articulate$placementPlan;
    }

    @Override
    public void articulate$setPlacementPlan(final @Nullable ArticulatedTrackPlacementPlan plan) {
        articulate$placementPlan = plan;
    }

}
