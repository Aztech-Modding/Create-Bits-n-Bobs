package com.kipti.bnb.mixin.azimuth;

import com.cake.azimuth.behaviour.AzimuthSmartBlockEntityExtension;
import com.cake.azimuth.behaviour.extensions.ItemRequirementBlockEntityBehaviourExtension;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemRequirement.class)
public class ItemRequirementMixin {

    @Inject(method = "of(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;)Lcom/simibubi/create/content/schematics/requirement/ItemRequirement;", at = @At("HEAD"), cancellable = true)
    private static void of(BlockState state, BlockEntity be, CallbackInfoReturnable<ItemRequirement> cir) {
        if (be instanceof AzimuthSmartBlockEntityExtension azebe) {
            for (ItemRequirementBlockEntityBehaviourExtension itemRequirementBehaviour : azebe.azimuth$getItemRequirementExtensionCache().get()) {
                ItemRequirement behaviourRequirements = itemRequirementBehaviour.getRequiredItems(state);
                if (behaviourRequirements != null) {
                    cir.setReturnValue(behaviourRequirements.union(cir.getReturnValue()));
                    return;
                }
            }

        }
    }
}
