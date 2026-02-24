package com.cake.azimuth.registration;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class RenderedBehaviourInterest {
    private static final List<PendingTypePredicateInterest> PENDING_TYPE_PREDICATES = new ArrayList<>();
    private static final Map<BlockEntityType<?>, RenderedBehaviourWrapPlan> PLANS_BY_TYPE = new IdentityHashMap<>();
    private static boolean resolved = false;

    public static void registerInterest(final Predicate<BlockEntityType<?>> typePredicate, final RenderedBehaviourWrapPlan wrapPlan) {
        PENDING_TYPE_PREDICATES.add(new PendingTypePredicateInterest(typePredicate, wrapPlan));
    }

    public static void resolve() {
        if (resolved && PENDING_TYPE_PREDICATES.isEmpty()) {
            return;
        }
        resolved = true;
        for (final BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            RenderedBehaviourWrapPlan plan = null;
            for (final PendingTypePredicateInterest pending : PENDING_TYPE_PREDICATES) {
                if (pending.typePredicate().test(type)) {
                    if (plan == null) {
                        plan = pending.wrapPlan();
                    } else {
                        plan = plan.union(pending.wrapPlan());
                    }
                }
            }
            PLANS_BY_TYPE.put(type, plan);
        }
        PENDING_TYPE_PREDICATES.clear();
    }

    public static boolean isInterested(final BlockEntityType<?> type) {
        if (!resolved) {
            resolve();
        }
        return PLANS_BY_TYPE.containsKey(type);
    }

    public static @Nullable RenderedBehaviourWrapPlan getPlan(final BlockEntityType<?> type) {
        if (!resolved) {
            resolve();
        }
        return PLANS_BY_TYPE.get(type);
    }

    private record PendingTypePredicateInterest(
            Predicate<BlockEntityType<?>> typePredicate,
            RenderedBehaviourWrapPlan wrapPlan
    ) {
    }
}
