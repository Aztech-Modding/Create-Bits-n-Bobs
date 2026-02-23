package com.kipti.bnb.registry.client;

import com.cake.azimuth.behaviour.extensions.RenderedBehaviourExtension;
import com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour.CogwheelChainBlockEntityBehaviourRenderer;

public class BnbBlockEntityBehaviourRenderers {
    public static final RenderedBehaviourExtension.BehaviourRenderSupplier COGWHEEL_CHAIN = () -> CogwheelChainBlockEntityBehaviourRenderer::new;
}
