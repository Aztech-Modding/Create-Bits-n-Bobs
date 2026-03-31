package com.kipti.bnb.mixin.dyeable.fluid_tank;

import com.kipti.bnb.content.decoration.dyeable.DyeableTransitionHelper;
import com.kipti.bnb.content.decoration.dyeable.tanks.DyeableTankBehaviour;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(value = ConnectivityHandler.class, remap = false)
public class ConnectivityHandlerMixin {

    @Unique
    private static final ThreadLocal<Boolean> BNB_IN_FORMATION = new ThreadLocal<>();

    @Unique
    private static final ThreadLocal<DyeColor> BNB_FORMATION_COLOR = new ThreadLocal<>();

    @Inject(method = "formMulti(Lnet/minecraft/world/level/block/entity/BlockEntity;)V", at = @At("HEAD"))
    private static void bnb$captureFormationColor(final BlockEntity be, final CallbackInfo ci) {
        final DyeableTankBehaviour behaviour = BlockEntityBehaviour.get(
                be.getLevel(),
                be.getBlockPos(),
                DyeableTankBehaviour.TYPE
        );
        if (behaviour == null) {
            return;
        }
        BNB_IN_FORMATION.set(Boolean.TRUE);
        BNB_FORMATION_COLOR.set(bnb$getEffectiveTankColor(be));
    }

    @Inject(method = "formMulti(Lnet/minecraft/world/level/block/entity/BlockEntity;)V", at = @At("RETURN"))
    private static void bnb$clearFormationColor(final BlockEntity be, final CallbackInfo ci) {
        BNB_IN_FORMATION.remove();
        BNB_FORMATION_COLOR.remove();
    }

    @Inject(method = "partAt", at = @At("RETURN"), cancellable = true)
    private static void bnb$filterByDyeColor(
            final BlockEntityType<?> type,
            final BlockGetter level,
            final BlockPos pos,
            final CallbackInfoReturnable<BlockEntity> cir
    ) {
        if (!Boolean.TRUE.equals(BNB_IN_FORMATION.get())) {
            return;
        }
        final BlockEntity result = cir.getReturnValue();
        if (result == null) {
            return;
        }
        final @Nullable DyeColor candidateColor = bnb$getEffectiveTankColor(result);
        final @Nullable DyeColor formationColor = BNB_FORMATION_COLOR.get();
        if (!Objects.equals(candidateColor, formationColor)) {
            cir.setReturnValue(null);
        }
    }

    @Unique
    @Nullable
    private static DyeColor bnb$getEffectiveTankColor(final BlockEntity be) {
        final DyeableTankBehaviour behaviour = BlockEntityBehaviour.get(
                be.getLevel(),
                be.getBlockPos(),
                DyeableTankBehaviour.TYPE
        );
        if (behaviour != null && behaviour.getColor() != null) {
            return behaviour.getColor();
        }
        return DyeableTransitionHelper.getPendingPlacementColor(be.getLevel(), be.getBlockPos());
    }
}
