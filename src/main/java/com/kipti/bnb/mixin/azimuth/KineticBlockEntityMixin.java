package com.kipti.bnb.mixin.azimuth;

import com.cake.azimuth.behaviour.CachedBehaviourExtensionAccess;
import com.cake.azimuth.behaviour.extensions.KineticBlockEntityBehaviourExtension;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(KineticBlockEntity.class)
public class KineticBlockEntityMixin {

    @Unique
    private final CachedBehaviourExtensionAccess<KineticBlockEntityBehaviourExtension> azimuth$kineticBehaviourCacheAccess =
            new CachedBehaviourExtensionAccess<>(() -> this, (e) -> e instanceof KineticBlockEntityBehaviourExtension);

    @Inject(method = "addPropagationLocations", at = @At("HEAD"))
    public void addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours, CallbackInfoReturnable<List<BlockPos>> cir) {
        for (KineticBlockEntityBehaviourExtension behaviour : azimuth$kineticBehaviourCacheAccess.get()) {
            behaviour.addExtraPropagationLocations(block, state, neighbours);
        }
    }

}
