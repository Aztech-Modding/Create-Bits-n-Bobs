package com.kipti.bnb.registry.azimuth;

import com.cake.azimuth.registration.BehaviourApplicators;
import com.cake.azimuth.registration.RenderedBehaviourInterest;
import com.cake.azimuth.registration.RenderedBehaviourWrapPlan;
import com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour.CogwheelChainBehaviour;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.List;

public class BnbBehaviourApplicators {

    public static void init() {
        BehaviourApplicators.register(be -> {
            if (be.getBlockState().getBlock() instanceof ICogWheel) {
                return List.of(new CogwheelChainBehaviour(be));
            }
            return null;
        });
        RenderedBehaviourInterest.registerInterest(
                BnbBehaviourApplicators::isSomeCogwheelBlockEntity,
                RenderedBehaviourWrapPlan.wrapAll()
        );
    }

    private static boolean isSomeCogwheelBlockEntity(final BlockEntityType<?> type) {
        for (final Block block : type.getValidBlocks()) {
            if (!(block instanceof ICogWheel)) {
                continue;
            }
            return true;
        }
        return false;
    }

}
