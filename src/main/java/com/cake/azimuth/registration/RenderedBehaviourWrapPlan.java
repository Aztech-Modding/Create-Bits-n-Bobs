package com.cake.azimuth.registration;

public record RenderedBehaviourWrapPlan(
        boolean wrapRenderer,
        boolean wrapVisual
) {
    public static RenderedBehaviourWrapPlan wrapAll() {
        return new RenderedBehaviourWrapPlan(
                true,
                true
        );
    }

    public RenderedBehaviourWrapPlan union(final RenderedBehaviourWrapPlan otherPlan) {
        return new RenderedBehaviourWrapPlan(
                this.wrapRenderer || otherPlan.wrapRenderer,
                this.wrapVisual || otherPlan.wrapVisual
        );
    }
}
