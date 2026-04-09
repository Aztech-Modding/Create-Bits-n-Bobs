package com.kipti.bnb.mixin;

import com.kipti.bnb.content.kinetics.cogwheel_chain.riding.CogwheelChainRidingHelper;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRidingHandler;

@Mixin(ChainConveyorRidingHandler.class)
public class ChainConveyorRidingHandlerMixin {

    @Inject(method = "embark", at = @At("HEAD"))
    private static void bits_n_bobs$disembarkPreviousRide(final BlockPos lift, final float position,
                                                          final BlockPos connection, final CallbackInfo ci) {
        CogwheelChainRidingHelper.disembarkFromAnyPreviousRide();
    }
}
