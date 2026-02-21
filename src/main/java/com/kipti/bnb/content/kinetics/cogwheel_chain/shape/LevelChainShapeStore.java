package com.kipti.bnb.content.kinetics.cogwheel_chain.shape;

import com.kipti.bnb.content.kinetics.cogwheel_chain.block.CogwheelChainBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class LevelChainShapeStore {
    private static final long VALIDATE_INTERVAL_TICKS = 10;

    private final Map<BlockPos, List<CogwheelChainShape>> chains = new ConcurrentHashMap<>();
    private long nextValidationGameTime = Long.MIN_VALUE;

    void put(final BlockPos controllerPos, final List<CogwheelChainShape> shapes) {
        if (shapes == null || shapes.isEmpty()) {
            chains.remove(controllerPos);
            return;
        }
        chains.put(controllerPos.immutable(), List.copyOf(shapes));
    }

    void invalidate(final BlockPos controllerPos) {
        chains.remove(controllerPos);
    }

    Iterable<Map.Entry<BlockPos, List<CogwheelChainShape>>> entries() {
        return chains.entrySet();
    }

    void validate(final Level level) {
        final long gameTime = level.getGameTime();
        if (gameTime < nextValidationGameTime) {
            return;
        }
        nextValidationGameTime = gameTime + VALIDATE_INTERVAL_TICKS;

        chains.entrySet().removeIf(entry -> {
            final BlockPos pos = entry.getKey();
            if (!level.isLoaded(pos)) {
                return true;
            }

            if (!(level.getBlockEntity(pos) instanceof CogwheelChainBlockEntity chainBE)) {
                return true;
            }

            return !chainBE.isController() || chainBE.getChain() == null;
        });
    }
}
