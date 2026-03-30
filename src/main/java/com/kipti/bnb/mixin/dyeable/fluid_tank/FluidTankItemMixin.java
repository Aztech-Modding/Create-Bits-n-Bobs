package com.kipti.bnb.mixin.dyeable.fluid_tank;

import com.kipti.bnb.content.decoration.dyeable.DyeableTransitionHelper;
import com.kipti.bnb.content.decoration.dyeable.fluid_tank.DyeableFluidTankBehaviour;
import com.simibubi.create.content.fluids.tank.FluidTankItem;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Injects dye-on-placement logic into {@link FluidTankItem} so that holding a dye in the offhand
 * applies color immediately when placing fluid tanks, including multi-placed tanks via {@code tryMultiPlace}.
 */
@Mixin(value = FluidTankItem.class, remap = false)
public class FluidTankItemMixin {

    @Unique
    @Nullable
    private static DyeColor bnb$getOffhandDyeColor(final BlockPlaceContext context) {
        if (context.getPlayer() == null) {
            return null;
        }
        final ItemStack offhand = context.getPlayer().getOffhandItem();
        if (offhand.getItem() instanceof final DyeItem dyeItem) {
            return dyeItem.getDyeColor();
        }
        return null;
    }

    @Inject(method = "place", at = @At("HEAD"))
    private void bnb$savePendingDyeColor(final BlockPlaceContext context, final CallbackInfoReturnable<InteractionResult> cir) {
        final DyeColor offhandDye = bnb$getOffhandDyeColor(context);
        if (offhandDye != null) {
            DyeableTransitionHelper.savePendingPlacementColor(
                    context.getLevel(), context.getClickedPos(), offhandDye
            );
        }
    }

    @Inject(method = "place", at = @At("RETURN"))
    private void bnb$applyDyeAfterPlacement(final BlockPlaceContext context, final CallbackInfoReturnable<InteractionResult> cir) {
        final DyeColor offhandDye = bnb$getOffhandDyeColor(context);
        if (offhandDye == null) {
            return;
        }
        try {
            final InteractionResult result = cir.getReturnValue();
            if (result.consumesAction() && context.getLevel().isClientSide()) {
                final DyeableFluidTankBehaviour behaviour = BlockEntityBehaviour.get(
                        context.getLevel(), context.getClickedPos(), DyeableFluidTankBehaviour.TYPE
                );
                if (behaviour != null) {
                    behaviour.applyColorClientOnly(offhandDye);
                }
            }
        } finally {
            DyeableTransitionHelper.consumePendingPlacementColor(
                    context.getLevel(), context.getClickedPos()
            );
        }
    }

    @Inject(method = "updateCustomBlockEntityTag", at = @At("RETURN"))
    private void bnb$applyDyeToBlockEntity(
            final BlockPos pos, final Level level, @Nullable final Player player,
            final ItemStack stack, final BlockState state,
            final CallbackInfoReturnable<Boolean> cir
    ) {
        if (level.isClientSide() || player == null) {
            return;
        }
        final ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof final DyeItem dyeItem) {
            final DyeableFluidTankBehaviour behaviour = BlockEntityBehaviour.get(
                    level, pos, DyeableFluidTankBehaviour.TYPE
            );
            if (behaviour != null) {
                behaviour.setColor(dyeItem.getDyeColor());
            }
        }
    }
}
