package com.kipti.bnb.mixin.dyeable_pipes;

import com.kipti.bnb.content.dyeable_pipes.DyedPipeTransitionHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GlassFluidPipeBlock.class)
public class GlassFluidPipeBlockTransitionMixin {

    @WrapOperation(
            method = "onWrenched",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/FluidTransportBehaviour;cacheFlows(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V")
    )
    private void bnb$cacheColorBeforeRegular(final Level level, final BlockPos pos, final Operation<Void> original) {
        DyedPipeTransitionHelper.saveCurrentDye(level, pos);
        original.call(level, pos);
    }

    @WrapOperation(
            method = "onWrenched",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/FluidTransportBehaviour;loadFlows(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V")
    )
    private void bnb$loadColorAfterRegular(final Level level, final BlockPos pos, final Operation<Void> original) {
        original.call(level, pos);
        DyedPipeTransitionHelper.applyPreviousDye(level, pos);
    }

}
