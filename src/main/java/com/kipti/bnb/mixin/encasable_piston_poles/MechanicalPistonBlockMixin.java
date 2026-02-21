package com.kipti.bnb.mixin.encasable_piston_poles;

import com.kipti.bnb.registry.content.blocks.BnbBlocksBootstrap;
import com.simibubi.create.content.contraptions.piston.MechanicalPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MechanicalPistonBlock.class)
public class MechanicalPistonBlockMixin {

    @Inject(method = "isExtensionPole", at = @At("RETURN"), cancellable = true)
    private static void isExtensionPole(final BlockState state, final CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() || BnbBlocksBootstrap.ENCASED_PISTON_EXTENSION_POLE.isIn(state));
    }

}

