package com.kipti.bnb.mixin.dyeable_pipes;

import com.kipti.bnb.content.dyeable_pipes.DyeablePipeBehaviour;
import com.kipti.bnb.content.dyeable_pipes.DyedPipeTransitionHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FluidPipeBlock.class)
public class FluidPipeBlockMixin {

    @WrapOperation(
            method = "updateBlockState",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/pipes/FluidPipeBlock;canConnectTo(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z")
    )
    private boolean bnb$filterDyeConnections(
            final BlockAndTintGetter neighbourWorld,
            final BlockPos neighbourPos,
            final BlockState neighbourState,
            final Direction direction,
            final Operation<Boolean> original,
            // Captured outer method parameters:
            final BlockState state,
            final Direction preferredDirection,
            final Direction ignore,
            final BlockAndTintGetter world,
            final BlockPos pos
    ) {
        return bnb$filterRegularPipeConnection(
                original.call(neighbourWorld, neighbourPos, neighbourState, direction),
                world,
                pos,
                neighbourPos,
                neighbourState
        );
    }

    @WrapOperation(
            method = "shouldDrawRim",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/pipes/FluidPipeBlock;canConnectTo(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z")
    )
    private static boolean bnb$filterDyeConnectionsWhileDrawingRims(
            final BlockAndTintGetter neighbourWorld,
            final BlockPos neighbourPos,
            final BlockState neighbourState,
            final Direction direction,
            final Operation<Boolean> original,
            final BlockAndTintGetter world,
            final BlockPos pos,
            final BlockState state,
            final Direction queriedDirection
    ) {
        return bnb$filterRegularPipeConnection(
                original.call(neighbourWorld, neighbourPos, neighbourState, direction),
                world,
                pos,
                neighbourPos,
                neighbourState
        );
    }

    @Unique
    private static boolean bnb$filterRegularPipeConnection(
            final boolean canConnect,
            final BlockAndTintGetter world,
            final BlockPos pos,
            final BlockPos neighbourPos,
            final BlockState neighbourState
    ) {
        if (!canConnect) {
            return false;
        }
        if (!FluidPipeBlock.isPipe(neighbourState)) {
            return true;
        }

        final @Nullable DyeColor selfColor = bnb$getEffectiveColor(world, pos);
        final @Nullable DyeColor neighbourColor = bnb$getEffectiveColor(world, neighbourPos);

        if (selfColor == null || neighbourColor == null) {
            return true;
        }

        return selfColor == neighbourColor;
    }

    @Unique
    @Nullable
    private static DyeColor bnb$getEffectiveColor(final BlockAndTintGetter world, final BlockPos pos) {
        final DyeablePipeBehaviour behaviour = BlockEntityBehaviour.get(world, pos, DyeablePipeBehaviour.TYPE);
        if (behaviour != null && behaviour.getColor() != null) {
            return behaviour.getColor();
        }
        return DyedPipeTransitionHelper.getPendingPlacementColor(world, pos);
    }

}
