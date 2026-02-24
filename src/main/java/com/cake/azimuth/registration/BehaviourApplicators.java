package com.cake.azimuth.registration;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class BehaviourApplicators {

    private static final List<Function<SmartBlockEntity, List<BlockEntityBehaviour>>> BEHAVIOUR_APPLICATORS = new ArrayList<>();
    private static final Map<BlockEntityType<?>, List<Function<SmartBlockEntity, List<BlockEntityBehaviour>>>> TYPE_APPLICATORS = new IdentityHashMap<>();
    private static final List<PendingTypeApplicator> PENDING_TYPE_APPLICATORS = new ArrayList<>();

    public static void register(final Function<SmartBlockEntity, List<BlockEntityBehaviour>> applicator) {
        BEHAVIOUR_APPLICATORS.add(applicator);
    }

    public static List<BlockEntityBehaviour> getBehavioursFor(final SmartBlockEntity be) {
        resolveRegisteredTypes();
        final List<BlockEntityBehaviour> behaviours = new ArrayList<>();
        final List<Function<SmartBlockEntity, List<BlockEntityBehaviour>>> typeApplicators = TYPE_APPLICATORS.get(be.getType());
        if (typeApplicators != null) {
            for (final Function<SmartBlockEntity, List<BlockEntityBehaviour>> applicator : typeApplicators) {
                final List<BlockEntityBehaviour> appliedBehaviours = applicator.apply(be);
                if (appliedBehaviours != null) {
                    behaviours.addAll(appliedBehaviours);
                }
            }
            return behaviours;
        }

        for (final Function<SmartBlockEntity, List<BlockEntityBehaviour>> applicator : BEHAVIOUR_APPLICATORS) {
            final List<BlockEntityBehaviour> appliedBehaviours = applicator.apply(be);
            if (appliedBehaviours != null)
                behaviours.addAll(appliedBehaviours);
        }
        return behaviours;
    }

    public static void registerForType(final Supplier<? extends BlockEntityType<?>> typeSupplier, final Function<SmartBlockEntity, List<BlockEntityBehaviour>> applicator) {
        PENDING_TYPE_APPLICATORS.add(new PendingTypeApplicator(typeSupplier, applicator));
    }

    public static void resolveRegisteredTypes() {
        if (PENDING_TYPE_APPLICATORS.isEmpty()) {
            return;
        }

        final List<PendingTypeApplicator> unresolved = new ArrayList<>();
        for (final PendingTypeApplicator pending : PENDING_TYPE_APPLICATORS) {
            final BlockEntityType<?> type;
            try {
                type = pending.typeSupplier().get();
            } catch (final RuntimeException e) {
                unresolved.add(pending);
                continue;
            }

            if (type == null) {
                unresolved.add(pending);
                continue;
            }
            TYPE_APPLICATORS.computeIfAbsent(type, ignored -> new ArrayList<>()).add(pending.applicator());
        }
        PENDING_TYPE_APPLICATORS.clear();
        PENDING_TYPE_APPLICATORS.addAll(unresolved);
    }

    private record PendingTypeApplicator(
            Supplier<? extends BlockEntityType<?>> typeSupplier,
            Function<SmartBlockEntity, List<BlockEntityBehaviour>> applicator
    ) {
    }

}
