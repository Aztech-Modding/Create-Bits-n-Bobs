package com.kipti.bnb.content.dyeable_pipes;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.WorldAttached;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public final class DyedPipeTransitionHelper {

    private static final WorldAttached<Map<BlockPos, DyeColor>> CACHED_DYES = new WorldAttached<>($ -> new HashMap<>());

    private DyedPipeTransitionHelper() {
    }

    public static void saveCurrentDye(final Level level, final BlockPos pos) {
        final DyeablePipeBehaviour behaviour = BlockEntityBehaviour.get(level, pos, DyeablePipeBehaviour.TYPE);
        if (behaviour == null || behaviour.getColor() == null) {
            return;
        }

        CACHED_DYES.get(level).put(pos.immutable(), behaviour.getColor());
    }

    public static void applyPreviousDye(final Level level, final BlockPos pos) {
        final DyeColor cachedColor = CACHED_DYES.get(level).remove(pos.immutable());
        if (cachedColor == null) {
            return;
        }

        final DyeablePipeBehaviour behaviour = BlockEntityBehaviour.get(level, pos, DyeablePipeBehaviour.TYPE);
        if (behaviour != null) {
            behaviour.setColor(cachedColor);
        }
    }

}
