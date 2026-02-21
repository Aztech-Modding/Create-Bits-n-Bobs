package com.kipti.bnb.mixin.encasable_piston_poles;

import com.kipti.bnb.content.kinetics.encased_blocks.piston_pole.EncasedPistonExtensionPoleBlock;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.simibubi.create.content.contraptions.ContraptionCollider;
import com.simibubi.create.content.contraptions.TranslatingContraption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContraptionCollider.class)
public abstract class ContraptionColliderMixin {

    @Inject(method = "isCollidingWithWorld", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/api/registry/SimpleRegistry;get(Lnet/minecraft/world/level/block/state/StateHolder;)Ljava/lang/Object;"), cancellable = true)
    private static void isCollidingWithWorldAndNotAnEncasedPiston(final Level world,
                                                                  final TranslatingContraption contraption,
                                                                  final BlockPos anchor,
                                                                  final Direction movementDirection,
                                                                  final CallbackInfoReturnable<Boolean> cir,
                                                                  @Local(name = "collidedState") final BlockState collidedState,
                                                                  @Local(name = "emptyCollider") final LocalBooleanRef emptyCollider) {
        if (collidedState.getBlock() instanceof EncasedPistonExtensionPoleBlock) { //Improper but faster
            if (!collidedState.getValue(EncasedPistonExtensionPoleBlock.EMPTY))
                return;

            if (collidedState.getValue(EncasedPistonExtensionPoleBlock.FACING).getAxis() != movementDirection.getAxis())
                return;

            emptyCollider.set(true);
        }
    }


}
