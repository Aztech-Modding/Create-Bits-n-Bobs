package com.kipti.bnb.mixin;

import com.kipti.bnb.content.kinetics.cogwheel_carriage.contraption.CogwheelChainCarriageContraptionEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

/**
 * Suppress position updates for contraption carriage entities since we want to do our own smarter position tracking specific to chains.
 *
 */
@Mixin(ServerEntity.class)
public class ServerEntityMixin {

    @Shadow
    @Final
    private Entity entity;

    @WrapOperation(method = "sendChanges", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 4))
    private void wrapCowInstanceofCheck(final Consumer<?> instance, final Object t, final Operation<Void> original) {
        if (this.entity instanceof CogwheelChainCarriageContraptionEntity) {
            return;
        }
        original.call(instance, t);
    }

}
