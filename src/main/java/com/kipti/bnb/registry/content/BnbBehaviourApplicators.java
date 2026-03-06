package com.kipti.bnb.registry.content;

import com.cake.azimuth.registration.BehaviourApplicators;
import com.kipti.bnb.content.dyeable_pipes.DyeablePipeBehaviour;
import com.simibubi.create.AllBlockEntityTypes;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.List;
import java.util.function.Supplier;

public class BnbBehaviourApplicators {

    public static void register() {
        registerDyeablePipeBehaviours();
    }

    private static void registerDyeablePipeBehaviours() {
        registerDyeablePipeBehaviour(() -> AllBlockEntityTypes.FLUID_PIPE.get());
        registerDyeablePipeBehaviour(() -> AllBlockEntityTypes.ENCASED_FLUID_PIPE.get());
        registerDyeablePipeBehaviour(() -> AllBlockEntityTypes.GLASS_FLUID_PIPE.get());
    }

    private static void registerDyeablePipeBehaviour(final Supplier<? extends BlockEntityType<?>> typeSupplier) {
        BehaviourApplicators.registerForType(typeSupplier, be -> List.of(new DyeablePipeBehaviour(be)));
    }

}
