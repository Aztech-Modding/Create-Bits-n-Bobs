package com.kipti.bnb.mixin.dyeable.pipes;

import com.kipti.bnb.content.decoration.dyeable.DyeableTransitionHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EncasedPipeBlock.class)
public class EncasedPipeBlockTransitionMixin {

    // Encased → Regular (onWrenched)
    @WrapOperation(
            method = "onWrenched",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/FluidTransportBehaviour;cacheFlows(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)V")
    )
    private void bnb$cacheColorBeforeUnencasing(final LevelAccessor world,
                                                final BlockPos pos,
                                                final Operation<Void> original) {
        if (world instanceof final Level level)
            DyeableTransitionHelper.saveCurrentDye(level, pos);
        original.call(world, pos);
    }

    @WrapOperation(
            method = "onWrenched",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/FluidTransportBehaviour;loadFlows(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)V")
    )
    private void bnb$loadColorAfterUnencasing(final LevelAccessor world,
                                              final BlockPos pos,
                                              final Operation<Void> original) {
        original.call(world, pos);
        if (world instanceof final Level level)
            DyeableTransitionHelper.applyPreviousDye(level, pos);
    }

    // Regular → Encased (handleEncasing)
    @WrapOperation(
            method = "handleEncasing",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/FluidTransportBehaviour;cacheFlows(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)V")
    )
    private void bnb$cacheColorBeforeEncasing(final LevelAccessor world,
                                              final BlockPos pos,
                                              final Operation<Void> original) {
        if (world instanceof final Level level)
            DyeableTransitionHelper.saveCurrentDye(level, pos);
        original.call(world, pos);
    }

    @WrapOperation(
            method = "handleEncasing",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/FluidTransportBehaviour;loadFlows(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)V")
    )
    private void bnb$loadColorAfterEncasing(final LevelAccessor world,
                                            final BlockPos pos,
                                            final Operation<Void> original) {
        original.call(world, pos);
        if (world instanceof final Level level)
            DyeableTransitionHelper.applyPreviousDye(level, pos);
    }

}
