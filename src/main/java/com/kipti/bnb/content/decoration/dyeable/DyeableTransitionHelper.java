package com.kipti.bnb.content.decoration.dyeable;

import com.kipti.bnb.content.decoration.dyeable.pipes.DyeablePipeBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.data.WorldAttached;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DyeableTransitionHelper {

    private static final WorldAttached<Map<BlockPos, DyeColor>> CACHED_DYES = new WorldAttached<>($ -> new HashMap<>());
    private static final WorldAttached<Map<BlockPos, DyeColor>> PENDING_PLACEMENT_DYES = new WorldAttached<>($ -> new HashMap<>());

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

    public static void savePendingPlacementColor(final Level level, final BlockPos pos, final DyeColor color) {
        PENDING_PLACEMENT_DYES.get(level).put(pos.immutable(), color);
    }

    @Nullable
    public static DyeColor getPendingPlacementColor(final BlockAndTintGetter world, final BlockPos pos) {
        if (!(world instanceof final Level level)) {
            return null;
        }
        return PENDING_PLACEMENT_DYES.get(level).get(pos);
    }

    public static void consumePendingPlacementColor(final Level level, final BlockPos pos) {
        PENDING_PLACEMENT_DYES.get(level).remove(pos.immutable());
    }

}
