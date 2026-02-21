package com.kipti.bnb.mixin;

import com.kipti.bnb.registry.content.blocks.BnbBlocksBootstrap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.contraptions.ContraptionCollider;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ContraptionCollider.class)
public class ContraptionColliderMixin {

    @WrapOperation(method = "isCollidingWithWorld", at = @At(value = "INVOKE", target = "Lcom/tterrag/registrate/util/entry/BlockEntry;has(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private static boolean has(final BlockEntry<?> instance, final BlockState state, final Operation<Boolean> original) {
        return original.call(instance, state) || BnbBlocksBootstrap.CHAIN_PULLEY_MAGNET.has(state);
    }

}

