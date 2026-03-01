package com.cake.azimuth.behaviour.extensions;

import com.cake.azimuth.behaviour.BehaviourExtension;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Allows behaviours to add additional propagation locations to a kinetic block entity.
 */
public interface KineticBehaviourExtension extends BehaviourExtension {

    /**
     * Allows a behaviour to add to (or modify) the neighbors list in kinetic propagation
     *
     * @return an array list of the neighbors by default
     */
    default List<BlockPos> addExtraPropagationLocations(final IRotate block, final BlockState state, final List<BlockPos> neighbours) {
        return neighbours;
    }

    /**
     * Provides a base level propagation value for a kinetic propagation (i.e. instead of 0).
     * If you want to override the base propagation value from the block entity, use {@link KineticBehaviourExtension#forcePropagateRotationTo} instead.
     *
     * @return the propagated rotation to use instead of 0
     */
    default float propagateRotationTo(final KineticBlockEntity target, final BlockState stateFrom, final BlockState stateTo, final BlockPos diff, final boolean connectedViaAxes, final boolean connectedViaCogs) {
        return 0;
    }

    /**
     * Provides a high level propagation value for a kinetic propagation (i.e. instead of whatever the block entity wants).
     * If you just want to provide the base propagation value of the block entity, use {@link KineticBehaviourExtension#propagateRotationTo} instead.
     *
     * @return the propagated rotation to use, ignored if 0
     */
    default float forcePropagateRotationTo(final KineticBlockEntity target, final BlockState stateFrom, final BlockState stateTo, final BlockPos diff, final boolean connectedViaAxes, final boolean connectedViaCogs) {
        return 0;
    }

    default void detachKinetics() {
        final BlockEntity blockEntity = getBlockEntity();
        if (blockEntity instanceof final KineticBlockEntity kineticBlockEntity) {
            kineticBlockEntity.detachKinetics();
        } else {
            throw new IllegalStateException("BlockEntity must be a KineticBlockEntity to detach kinetics");
        }
    }

    default void attachKinetics() {
        final BlockEntity blockEntity = getBlockEntity();
        if (blockEntity instanceof final KineticBlockEntity kineticBlockEntity) {
            kineticBlockEntity.attachKinetics();
        } else {
            throw new IllegalStateException("BlockEntity must be a KineticBlockEntity to attach kinetics");
        }
    }

    default void repropagateKinetics() {
        final BlockEntity blockEntity = getBlockEntity();
        if (blockEntity instanceof final KineticBlockEntity kineticBlockEntity) {
            kineticBlockEntity.detachKinetics();
            kineticBlockEntity.updateSpeed = true;
        } else {
            throw new IllegalStateException("BlockEntity must be a KineticBlockEntity to detach kinetics");
        }
    }

    BlockEntity getBlockEntity();

}
