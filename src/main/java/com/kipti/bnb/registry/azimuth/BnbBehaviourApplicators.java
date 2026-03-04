package com.kipti.bnb.registry.azimuth;

import com.cake.azimuth.registration.BehaviourApplicators;
import com.cake.azimuth.registration.VisualWrapperInterest;
import com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour.CogwheelChainBehaviour;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainCandidate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.List;

public class BnbBehaviourApplicators {

    public static void init() {
        BehaviourApplicators.register(be -> {
            if (CogwheelChainCandidate.isValidCandidate(be.getBlockState())) {
                return List.of(new CogwheelChainBehaviour(be));
            }
            return null;
        });
        VisualWrapperInterest.registerInterest(
                BnbBehaviourApplicators::isSomeCogwheelBlockEntity
        );
    }

    private static boolean isSomeCogwheelBlockEntity(final BlockEntityType<?> type) {
        for (final Block block : type.getValidBlocks()) {
            if (!(CogwheelChainCandidate.isValidCandidate(block))) {
                continue;
            }
            return true;
        }
        return false;
    }

}
