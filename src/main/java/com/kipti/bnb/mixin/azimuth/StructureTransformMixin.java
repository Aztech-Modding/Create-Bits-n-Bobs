package com.kipti.bnb.mixin.azimuth;

import com.cake.azimuth.behaviour.AzimuthSmartBlockEntityExtension;
import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.simibubi.create.content.contraptions.StructureTransform;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureTransform.class)
public class StructureTransformMixin {

    @Inject(method = "apply(Lnet/minecraft/world/level/block/entity/BlockEntity;)V", at = @At("HEAD"))
    public void apply(BlockEntity be, CallbackInfo ci) {
        if (be instanceof AzimuthSmartBlockEntityExtension azebe) {
            for (SuperBlockEntityBehaviour extension : azebe.azimuth$getSuperBlockEntityBehaviours()) {
                extension.transform(be, ((StructureTransform) (Object) this));
            }
        }
    }


}
