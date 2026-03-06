package com.kipti.bnb.registry.azimuth;

import com.cake.azimuth.registration.BehaviourApplicators;
import com.cake.azimuth.registration.VisualWrapperInterest;
import com.kipti.bnb.content.dyeable_pipes.DyeablePipeBehaviour;
import com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour.CogwheelChainBehaviour;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainCandidate;
import com.simibubi.create.AllBlockEntityTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.List;
import java.util.function.Supplier;

public class BnbBehaviourApplicators {

    public static void register() {
        BehaviourApplicators.register(be -> {
            if (CogwheelChainCandidate.isValidCandidate(be.getBlockState())) {
                return List.of(new CogwheelChainBehaviour(be));
            }
            return null;
        });
        VisualWrapperInterest.registerInterest(
                BnbBehaviourApplicators::isSomeCogwheelBlockEntity
        );
        registerDyeablePipeBehaviours();
    }

    private static void registerDyeablePipeBehaviours() {
        registerDyeablePipeBehaviour(AllBlockEntityTypes.FLUID_PIPE);
        registerDyeablePipeBehaviour(AllBlockEntityTypes.ENCASED_FLUID_PIPE);
        registerDyeablePipeBehaviour(AllBlockEntityTypes.GLASS_FLUID_PIPE);
    }

    private static void registerDyeablePipeBehaviour(final Supplier<? extends BlockEntityType<?>> typeSupplier) {
        BehaviourApplicators.registerForType(typeSupplier, be -> List.of(new DyeablePipeBehaviour(be)));
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
