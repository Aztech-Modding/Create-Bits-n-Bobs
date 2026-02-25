package com.kipti.bnb.mixin.azimuth;

import com.cake.azimuth.behaviour.AzimuthSmartBlockEntityExtension;
import com.cake.azimuth.behaviour.extensions.KineticBehaviourExtension;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(RotationPropagator.class)
public class RotationPropagatorMixin {

    @WrapOperation(method = "getPotentialNeighbourLocations", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;addPropagationLocations(Lcom/simibubi/create/content/kinetics/base/IRotate;Lnet/minecraft/world/level/block/state/BlockState;Ljava/util/List;)Ljava/util/List;"))
    private static List<BlockPos> addPropagationLocations(final KineticBlockEntity instance,
                                                          final IRotate block,
                                                          final BlockState state,
                                                          List<BlockPos> neighbours,
                                                          final Operation<List<BlockPos>> original) {
        neighbours = original.call(instance, block, state, neighbours);
        if (instance instanceof final AzimuthSmartBlockEntityExtension asbee) {
            for (final KineticBehaviourExtension behaviour : asbee.azimuth$getKineticExtensionCache()) {
                neighbours = behaviour.addExtraPropagationLocations(block, state, neighbours);
            }
        }
        return neighbours;
    }

    @WrapOperation(method = "getRotationSpeedModifier", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;propagateRotationTo(Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;ZZ)F"))
    private static float propagateRotationTo(final KineticBlockEntity instance, final KineticBlockEntity target, final BlockState stateFrom, final BlockState stateTo, final BlockPos diff, final boolean connectedViaAxes, final boolean connectedViaCogs, final Operation<Float> original) {
        final float originalValue = original.call(instance, target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);
        if (instance instanceof final AzimuthSmartBlockEntityExtension asbee) {
            for (final KineticBehaviourExtension behaviour : asbee.azimuth$getKineticExtensionCache()) {
                final float hardPropagated = behaviour.forcePropagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);
                if (hardPropagated != 0) {
                    return hardPropagated;
                }
            }
            if (originalValue != 0)
                return originalValue;

            for (final KineticBehaviourExtension behaviour : asbee.azimuth$getKineticExtensionCache()) {
                final float propagated = behaviour.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);
                if (propagated != 0) {
                    return propagated;
                }
            }
        }
        return originalValue;
    }

}
