package com.kipti.bnb.mixin.dyeable.pipes;

import com.kipti.bnb.content.decoration.dyeable.DyeableTransitionHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.fluids.pipes.AxisPipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AxisPipeBlock.class)
public class AxisPipeBlockTransitionMixin {

    @WrapOperation(
            method = "useItemOn",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/FluidTransportBehaviour;cacheFlows(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)V")
    )
    private void bnb$saveColorBeforeEncasing(final LevelAccessor world,
                                             final BlockPos pos,
                                             final Operation<Void> original) {
        if (world instanceof final Level level)
            DyeableTransitionHelper.saveCurrentDye(level, pos);
        original.call(world, pos);
    }

    @WrapOperation(
            method = "useItemOn",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/FluidTransportBehaviour;cacheFlows(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)V")
    )
    private void bnb$loadColorAfterEncasing(final LevelAccessor world,
                                            final BlockPos pos,
                                            final Operation<Void> original) {
        original.call(world, pos);
        if (world instanceof final Level level)
            DyeableTransitionHelper.applyPreviousDye(level, pos);
    }

}
