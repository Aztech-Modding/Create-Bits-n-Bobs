package com.kipti.bnb.mixin.dyeable.pipes;

import com.kipti.bnb.content.decoration.dyeable.DyeableTransitionHelper;
import com.kipti.bnb.content.decoration.dyeable.fluid_tank.DyeableFluidTankBehaviour;
import com.kipti.bnb.content.decoration.dyeable.pipes.DyeablePipeBehaviour;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
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

        final @Nullable DyeColor selfColor = bnb$getEffectiveColor(world, pos);

        if (FluidPipeBlock.isPipe(neighbourState)) {
            final @Nullable DyeColor neighbourColor = bnb$getEffectiveColor(world, neighbourPos);
            return bnb$areDyeColorsCompatible(selfColor, neighbourColor);
        }

        if (FluidTankBlock.isTank(neighbourState)) {
            final @Nullable DyeColor tankColor = bnb$getEffectiveTankColor(world, neighbourPos);
            return bnb$areDyeColorsCompatible(selfColor, tankColor);
        }

        return true;
    }

    @Unique
    private static boolean bnb$areDyeColorsCompatible(@Nullable final DyeColor a, @Nullable final DyeColor b) {
        if (a == null || b == null) {
            return true;
        }
        return a == b;
    }

    @Unique
    @Nullable
    private static DyeColor bnb$getEffectiveTankColor(final BlockAndTintGetter world, final BlockPos pos) {
        final DyeableFluidTankBehaviour behaviour = BlockEntityBehaviour.get(
                world,
                pos,
                DyeableFluidTankBehaviour.TYPE
        );
        if (behaviour != null && behaviour.getColor() != null) {
            return behaviour.getColor();
        }
        return DyeableTransitionHelper.getPendingPlacementColor(world, pos);
    }

    @Unique
    @Nullable
    private static DyeColor bnb$getEffectiveColor(final BlockAndTintGetter world, final BlockPos pos) {
        final DyeablePipeBehaviour behaviour = BlockEntityBehaviour.get(world, pos, DyeablePipeBehaviour.TYPE);
        if (behaviour != null && behaviour.getColor() != null) {
            return behaviour.getColor();
        }
        return DyeableTransitionHelper.getPendingPlacementColor(world, pos);
    }

}
