package com.kipti.bnb.mixin.chair;

import com.kipti.bnb.registry.core.BnbTags;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Contraption.class)
public class ContraptionMixin {

    @WrapOperation(method = "moveBlock", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/AllTags$AllBlockTags;matches(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean bits_n_bobs$matchesSeatForContraption(final AllTags.AllBlockTags instance,
                                                          final BlockState state,
                                                          final Operation<Boolean> original) {
        return BnbTags.BnbBlockTags.CHAIRS.matches(state) || original.call(instance, state);
    }

}
