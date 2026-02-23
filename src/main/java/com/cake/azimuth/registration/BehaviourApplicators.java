package com.cake.azimuth.registration;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BehaviourApplicators {

    private static final List<Function<SmartBlockEntity, List<BlockEntityBehaviour>>> BEHAVIOUR_APPLICATORS = new ArrayList<>();

    public static void register(Function<SmartBlockEntity, List<BlockEntityBehaviour>> applicator) {
        BEHAVIOUR_APPLICATORS.add(applicator);
    }

    public static List<BlockEntityBehaviour> getBehavioursFor(SmartBlockEntity be) {
        List<BlockEntityBehaviour> behaviours = new ArrayList<>();
        for (Function<SmartBlockEntity, List<BlockEntityBehaviour>> applicator : BEHAVIOUR_APPLICATORS) {
            List<BlockEntityBehaviour> appliedBehaviours = applicator.apply(be);
            if (appliedBehaviours != null)
                behaviours.addAll(appliedBehaviours);
        }
        return behaviours;
    }

}
