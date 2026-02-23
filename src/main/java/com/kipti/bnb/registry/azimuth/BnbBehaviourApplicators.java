package com.kipti.bnb.registry.azimuth;

import com.cake.azimuth.registration.BehaviourApplicators;
import com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour.CogwheelChainBehaviour;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;

import java.util.List;

public class BnbBehaviourApplicators {

    public static void init() {
        BehaviourApplicators.register((be) -> {
            if (be.getBlockState().getBlock() instanceof ICogWheel) {
                return List.of(new CogwheelChainBehaviour(be));
            }
            return null;
        });
    }

}
