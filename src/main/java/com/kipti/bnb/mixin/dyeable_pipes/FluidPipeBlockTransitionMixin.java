package com.kipti.bnb.mixin.dyeable_pipes;

import com.kipti.bnb.content.dyeable_pipes.DyedPipeTransitionHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FluidPipeBlock.class)
public class FluidPipeBlockTransitionMixin {

    @WrapOperation(
            method = "onWrenched",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/FluidTransportBehaviour;cacheFlows(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)V")
    )
    private void bnb$saveCurrentDye(final LevelAccessor world, final BlockPos pos, final Operation<Void> original) {
        if (world instanceof final Level level)
            DyedPipeTransitionHelper.saveCurrentDye(level, pos);
        original.call(world, pos);
    }

    @WrapOperation(
            method = "onWrenched",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/FluidTransportBehaviour;loadFlows(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)V")
    )
    private void bnb$loadPreviousDye(final LevelAccessor world, final BlockPos pos, final Operation<Void> original) {
        original.call(world, pos);
        if (world instanceof final Level level)
            DyedPipeTransitionHelper.applyPreviousDye(level, pos);
    }

}
