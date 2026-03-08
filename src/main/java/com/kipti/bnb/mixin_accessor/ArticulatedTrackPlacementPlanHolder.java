package com.kipti.bnb.mixin_accessor;

import com.kipti.bnb.content.articulate.ArticulatedTrackPlacementPlan;
import org.jetbrains.annotations.Nullable;

public interface ArticulatedTrackPlacementPlanHolder {

    @Nullable ArticulatedTrackPlacementPlan articulate$getPlacementPlan();

    void articulate$setPlacementPlan(@Nullable ArticulatedTrackPlacementPlan plan);

}
