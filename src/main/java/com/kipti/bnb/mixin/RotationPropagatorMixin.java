package com.kipti.bnb.mixin;

import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.IRotate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Enable functionality for large cogwheels with a facing value instead of an axis value.
 */
@Mixin(RotationPropagator.class)
public class RotationPropagatorMixin {

    @Redirect(method = "isLargeToLargeGear", at = @At(ordinal = 0, value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
    private static Comparable bits_n_bobs$getValueFrom(final BlockState instance, final Property property) {
        return ((IRotate) instance.getBlock()).getRotationAxis(instance);
    }

    @Redirect(method = "isLargeToLargeGear", at = @At(ordinal = 1, value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
    private static Comparable bits_n_bobs$getValueTo(final BlockState instance, final Property property) {
        return ((IRotate) instance.getBlock()).getRotationAxis(instance);
    }

    @Redirect(method = "isLargeToSmallCog", at = @At(ordinal = 0, value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
    private static Comparable bits_n_bobs$getValue(final BlockState instance, final Property property) {
        return ((IRotate) instance.getBlock()).getRotationAxis(instance);
    }

    @Redirect(method = "getRotationSpeedModifier", at = @At(ordinal = 0, value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
    private static Comparable bits_n_bobs$getValueSource(final BlockState instance, final Property property) {
        return ((IRotate) instance.getBlock()).getRotationAxis(instance);
    }

    @Redirect(method = "getRotationSpeedModifier", at = @At(ordinal = 1, value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
    private static Comparable bits_n_bobs$getValueTarget(final BlockState instance, final Property property) {
        return ((IRotate) instance.getBlock()).getRotationAxis(instance);
    }


}

