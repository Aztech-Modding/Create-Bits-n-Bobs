package com.kipti.bnb.mixin;

import com.kipti.bnb.registry.client.BnbPartialModels;
import com.simibubi.create.AllBlocks;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fix manual item application not accounting for block entities (the rope pulley) by attaching a rope pulley.
 */
@Mixin(GuiGameElement.class)
public class GuiGameElementMixin {

    @Inject(method = "of(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/createmod/catnip/gui/element/GuiGameElement$GuiRenderBuilder;", at = @At("HEAD"), cancellable = true)
    private static void ofBlockStateInject(final BlockState blockState, final CallbackInfoReturnable<GuiGameElement.GuiRenderBuilder> cir) {
        if (AllBlocks.ROPE_PULLEY.has(blockState)) {
            cir.setReturnValue(GuiGameElement.of(BnbPartialModels.ROPE_PULLEY_JEI));
        }
    }

}

