package com.kipti.bnb.mixin.dyeable.fluid_tank;

import com.kipti.bnb.content.decoration.dyeable.DyeableTransitionHelper;
import com.kipti.bnb.content.decoration.dyeable.fluid_tank.DyeableFluidTankBehaviour;
import com.simibubi.create.content.fluids.tank.FluidTankItem;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Injects dye-on-placement logic into {@link FluidTankItem} so that holding a dye in the offhand
 * applies color immediately when placing fluid tanks, including multi-placed tanks via {@code tryMultiPlace}.
 */
@Mixin(value = FluidTankItem.class, remap = false)
public class FluidTankItemMixin {

    @Unique
    private static final ThreadLocal<DyeColor> BNB_MULTI_PLACE_DYE = new ThreadLocal<>();
    @Unique
    private static final ThreadLocal<DyeColor> BNB_SINGLE_PLACE_DYE = new ThreadLocal<>();


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
    private void bnb$savePendingDyeColor(final BlockPlaceContext ctx,
                                         final CallbackInfoReturnable<InteractionResult> cir) {
        final DyeColor dye = bnb$getDyeOffhandOrSurface(ctx);

        if (dye != null) {
            DyeableTransitionHelper.savePendingPlacementColor(
                    ctx.getLevel(), ctx.getClickedPos(), dye
            );
            BNB_SINGLE_PLACE_DYE.set(dye);
        }
    }

    @Inject(method = "place", at = @At("RETURN"))
    private void bnb$clearPendingDyeColor(final BlockPlaceContext ctx,
                                          final CallbackInfoReturnable<InteractionResult> cir) {
        BNB_SINGLE_PLACE_DYE.remove();
    }


    @Inject(method = "place", at = @At("RETURN"))
    private void bnb$applyDyeAfterPlacement(final BlockPlaceContext ctx,
                                            final CallbackInfoReturnable<InteractionResult> cir) {
        final DyeColor dye = BNB_SINGLE_PLACE_DYE.get();

        try {
            final InteractionResult result = cir.getReturnValue();
            if (result.consumesAction() && ctx.getLevel().isClientSide()) {
                final DyeableFluidTankBehaviour behaviour = BlockEntityBehaviour.get(
                        ctx.getLevel(), ctx.getClickedPos(), DyeableFluidTankBehaviour.TYPE
                );
                if (behaviour != null) {
                    behaviour.applyColorClientOnly(dye);
                }
            }
        } finally {
            DyeableTransitionHelper.consumePendingPlacementColor(
                    ctx.getLevel(), ctx.getClickedPos()
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
        final DyeColor dye = BNB_MULTI_PLACE_DYE.get() != null ? BNB_MULTI_PLACE_DYE.get() : BNB_SINGLE_PLACE_DYE.get();
        if (dye != null) {
            final DyeableFluidTankBehaviour behaviour = BlockEntityBehaviour.get(
                    level, pos, DyeableFluidTankBehaviour.TYPE
            );
            if (behaviour != null) {
                behaviour.setColor(dye);
            }
        }
    }

    @Inject(method = "tryMultiPlace", at = @At("HEAD"))
    private void bnb$captureMultiPlaceDye(final BlockPlaceContext ctx, final CallbackInfo ci) {
        final DyeColor dye = bnb$getDyeOffhandOrSurface(ctx);
        if (dye != null) {
            BNB_MULTI_PLACE_DYE.set(dye);
        }
    }

    @Inject(method = "tryMultiPlace", at = @At("RETURN"))
    private void bnb$clearMultiPlaceDye(final BlockPlaceContext ctx, final CallbackInfo ci) {
        BNB_MULTI_PLACE_DYE.remove();
    }

    @Unique
    private static @Nullable DyeColor bnb$getDyeOffhandOrSurface(final BlockPlaceContext ctx) {
        DyeColor dye = bnb$getOffhandDyeColor(ctx);

        if (ctx.getClickedFace().getAxis().isVertical()) {
            final BlockPos placedOnPos = ctx.getClickedPos().relative(ctx.getClickedFace().getOpposite());
            final DyeableFluidTankBehaviour surfaceBehaviour = BlockEntityBehaviour.get(
                    ctx.getLevel(), placedOnPos, DyeableFluidTankBehaviour.TYPE
            );
            if (surfaceBehaviour != null && surfaceBehaviour.getColor() != null) {
                dye = surfaceBehaviour.getColor();
            }
        }
        return dye;
    }

    @Redirect(
            method = "tryMultiPlace",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;"),
            remap = false
    )
    private InteractionResult bnb$wrapSubPlacement(final BlockItem instance, final BlockPlaceContext context) {
        final DyeColor dye = BNB_MULTI_PLACE_DYE.get();
        if (dye != null) {
            DyeableTransitionHelper.savePendingPlacementColor(context.getLevel(), context.getClickedPos(), dye);
        }
        try {
            final InteractionResult result = instance.place(context);
            if (result.consumesAction() && dye != null && context.getLevel().isClientSide()) {
                final DyeableFluidTankBehaviour behaviour = BlockEntityBehaviour.get(
                        context.getLevel(), context.getClickedPos(), DyeableFluidTankBehaviour.TYPE
                );
                if (behaviour != null) {
                    behaviour.applyColorClientOnly(dye);
                }
            }
            return result;
        } finally {
            if (dye != null) {
                DyeableTransitionHelper.consumePendingPlacementColor(context.getLevel(), context.getClickedPos());
            }
        }
    }
}
