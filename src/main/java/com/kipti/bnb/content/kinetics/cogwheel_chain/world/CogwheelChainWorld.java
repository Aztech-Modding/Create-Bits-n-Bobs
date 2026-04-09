package com.kipti.bnb.content.kinetics.cogwheel_chain.world;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour.CogwheelChainBehaviour;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PathedCogwheelNode;
import net.createmod.catnip.data.WorldAttached;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-level in-memory tracker of all loaded cogwheel chain drives.
 */
public class CogwheelChainWorld {

    private static final WorldAttached<CogwheelChainWorld> INSTANCES =
            new WorldAttached<>(CogwheelChainWorld::new);

    private static final long VALIDATE_INTERVAL_TICKS = 10;

    private final Map<BlockPos, CogwheelChain> chains = new ConcurrentHashMap<>();
    private final WeakReference<Level> levelReference;
    private long nextValidationGameTime = Long.MIN_VALUE;

    private CogwheelChainWorld(final LevelAccessor levelAccessor) {
        if (levelAccessor instanceof final Level foundLevel) {
            this.levelReference = new WeakReference<>(foundLevel);
            return;
        }
        this.levelReference = new WeakReference<>(null);
    }

    public static CogwheelChainWorld get(final Level level) {
        return INSTANCES.get(level);
    }

    public void put(final BlockPos controllerPos, final CogwheelChain chain) {
        if (chain == null) {
            this.chains.remove(controllerPos);
            return;
        }
        this.chains.put(controllerPos.immutable(), chain);
    }

    public void remove(final BlockPos controllerPos) {
        this.chains.remove(controllerPos);
    }

    public CogwheelChain getChain(final BlockPos controllerPos) {
        final Level level = this.getLevel();
        if (level == null) {
            return this.chains.get(controllerPos);
        }
        this.validate(level);
        return this.getValidatedChain(level, controllerPos);
    }

    public boolean containsChain(final BlockPos controllerPos) {
        return this.getChain(controllerPos) != null;
    }

    /**
     * Returns an iterable view of all tracked controller → chain entries.
     */
    public Iterable<Map.Entry<BlockPos, CogwheelChain>> entries() {
        final Level level = this.getLevel();
        if (level == null) {
            return Collections.emptyList();
        }
        return this.getEntriesSnapshot(level);
    }

    /**
     * Returns an unmodifiable view of all currently tracked controller positions.
     */
    public Set<BlockPos> getControllerPositions() {
        final Set<BlockPos> positions = new HashSet<>();
        final Level level = this.getLevel();
        if (level == null) {
            return Collections.unmodifiableSet(positions);
        }
        for (final Map.Entry<BlockPos, CogwheelChain> entry : this.getEntriesSnapshot(level)) {
            positions.add(entry.getKey());
        }
        return Collections.unmodifiableSet(positions);
    }

    /**
     * Returns all chain drives that have a node at the given world position.
     * Performs a simple scan — chain count per world is small.
     */
    public List<CogwheelChain> getChainDrivesAtPosition(final BlockPos worldPos) {
        final List<CogwheelChain> result = new ArrayList<>();
        final Level level = this.getLevel();
        if (level == null) {
            return result;
        }
        for (final Map.Entry<BlockPos, CogwheelChain> entry : this.getEntriesSnapshot(level)) {
            final BlockPos controllerPos = entry.getKey();
            final CogwheelChain chain = entry.getValue();
            for (final PathedCogwheelNode node : chain.getChainPathCogwheelNodes()) {
                if (controllerPos.offset(node.localPos()).equals(worldPos)) {
                    result.add(chain);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Periodically removes stale entries where the controller block entity
     * no longer exists or is no longer a chain controller.
     */
    public void validate(final Level level) {
        final long gameTime = level.getGameTime();
        if (gameTime < this.nextValidationGameTime) {
            return;
        }
        this.nextValidationGameTime = gameTime + VALIDATE_INTERVAL_TICKS;

        this.chains.entrySet().removeIf(entry -> {
            final BlockPos pos = entry.getKey();
            if (!level.isLoaded(pos)) {
                return false;
            }

            final CogwheelChainBehaviour behaviour = SuperBlockEntityBehaviour.get(level, pos, CogwheelChainBehaviour.TYPE);
            if (behaviour == null) {
                return true;
            }
            return !behaviour.isController();
        });
    }

    private CogwheelChain getValidatedChain(final Level level, final BlockPos controllerPos) {
        final CogwheelChain chain = this.chains.get(controllerPos);
        if (chain == null) {
            return null;
        }
        if (!level.isLoaded(controllerPos)) {
            return chain;
        }
        if (!this.isController(level, controllerPos)) {
            this.chains.remove(controllerPos);
            return null;
        }
        return chain;
    }

    private List<Map.Entry<BlockPos, CogwheelChain>> getEntriesSnapshot(final Level level) {
        this.validate(level);
        final List<Map.Entry<BlockPos, CogwheelChain>> snapshot = new ArrayList<>();
        for (final Map.Entry<BlockPos, CogwheelChain> entry : this.chains.entrySet()) {
            final BlockPos controllerPos = entry.getKey();
            if (level.isLoaded(controllerPos) && !this.isController(level, controllerPos)) {
                this.chains.remove(controllerPos);
                continue;
            }
            snapshot.add(Map.entry(controllerPos, entry.getValue()));
        }
        return snapshot;
    }

    private boolean isController(final Level level, final BlockPos controllerPos) {
        final CogwheelChainBehaviour behaviour = SuperBlockEntityBehaviour.get(
                level, controllerPos, CogwheelChainBehaviour.TYPE);
        return behaviour != null && behaviour.isController();
    }

    private Level getLevel() {
        return this.levelReference.get();
    }
}
