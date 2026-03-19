package com.kipti.bnb.content.kinetics.cogwheel_chain.world;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour.CogwheelChainBehaviour;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PathedCogwheelNode;
import net.createmod.catnip.data.WorldAttached;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-level in-memory tracker of all loaded cogwheel chain drives.
 * Acts strictly as a data store and query layer.
 */
public class CogwheelChainWorld {

    private static final WorldAttached<CogwheelChainWorld> INSTANCES =
            new WorldAttached<>($ -> new CogwheelChainWorld());

    private static final long VALIDATE_INTERVAL_TICKS = 10;

    private final Map<BlockPos, CogwheelChain> chains = new ConcurrentHashMap<>();
    private long nextValidationGameTime = Long.MIN_VALUE;

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
        return this.chains.get(controllerPos);
    }

    public boolean containsChain(final BlockPos controllerPos) {
        return this.chains.containsKey(controllerPos);
    }

    /**
     * Returns an iterable view of all tracked controller → chain entries.
     */
    public Iterable<Map.Entry<BlockPos, CogwheelChain>> entries() {
        return this.chains.entrySet();
    }

    /**
     * Returns an unmodifiable view of all currently tracked controller positions.
     */
    public Set<BlockPos> getControllerPositions() {
        return Collections.unmodifiableSet(this.chains.keySet());
    }

    /**
     * Returns all chain drives that have a node at the given world position.
     * Performs a simple scan — chain count per world is small.
     */
    public List<CogwheelChain> getChainDrivesAtPosition(final BlockPos worldPos) {
        final List<CogwheelChain> result = new ArrayList<>();
        for (final Map.Entry<BlockPos, CogwheelChain> entry : this.chains.entrySet()) {
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
                return true;
            }

            final CogwheelChainBehaviour behaviour = SuperBlockEntityBehaviour.get(level, pos, CogwheelChainBehaviour.TYPE);
            if (behaviour == null) {
                return true;
            }
            return !behaviour.isController();
        });
    }
}
