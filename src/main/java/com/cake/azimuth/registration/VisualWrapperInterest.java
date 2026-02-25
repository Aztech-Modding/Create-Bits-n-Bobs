package com.cake.azimuth.registration;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Used to limit the impact of wrapping visuals to only the block entities that need it.
 * This is necessary because wrapping visuals is slightly dodgy, and likely incurs some form of (small but non-zero) performance penalty.
 * This is not necessary for renderers.
 */
public class VisualWrapperInterest {
    private static final List<PendingTypePredicateInterest> PENDING_TYPE_PREDICATES = new ArrayList<>();
    private static final Set<BlockEntityType<?>> INTERESTED_BLOCK_ENTITY_TYPES = new HashSet<>();
    private static boolean resolved = false;

    public static void registerInterest(final Predicate<BlockEntityType<?>> typePredicate) {
        PENDING_TYPE_PREDICATES.add(new PendingTypePredicateInterest(typePredicate));
    }

    public static void resolve() {
        if (resolved && PENDING_TYPE_PREDICATES.isEmpty()) {
            return;
        }
        resolved = true;
        for (final BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            boolean interested = false;
            for (final PendingTypePredicateInterest pending : PENDING_TYPE_PREDICATES) {
                if (pending.typePredicate().test(type)) {
                    interested = true;
                    break;
                }
            }
            if (interested) {
                INTERESTED_BLOCK_ENTITY_TYPES.add(type);
            }
        }
        PENDING_TYPE_PREDICATES.clear();
    }

    public static boolean isInterested(final BlockEntityType<?> type) {
        if (!resolved) {
            resolve();
        }
        return INTERESTED_BLOCK_ENTITY_TYPES.contains(type);
    }

    private record PendingTypePredicateInterest(
            Predicate<BlockEntityType<?>> typePredicate
    ) {
    }
}
