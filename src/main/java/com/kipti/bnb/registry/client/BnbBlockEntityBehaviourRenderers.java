package com.kipti.bnb.registry.client;

import com.cake.azimuth.behaviour.render.BlockEntityBehaviourRenderer;
import com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour.CogwheelChainBlockEntityBehaviourRenderer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import java.util.function.Supplier;

public class BnbBlockEntityBehaviourRenderers {
    public static final Supplier<Supplier<BlockEntityBehaviourRenderer<KineticBlockEntity>>> COGWHEEL_CHAIN = () -> CogwheelChainBlockEntityBehaviourRenderer::new;
}
