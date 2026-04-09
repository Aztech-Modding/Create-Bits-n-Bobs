package com.kipti.bnb.mixin.dyeable.fluid_tank;

import com.kipti.bnb.content.decoration.dyeable.DyeableTransitionHelper;
import com.kipti.bnb.content.decoration.dyeable.tanks.DyeableTankBehaviour;
import com.kipti.bnb.registry.content.BnbAdvancements;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Injects dye-on-placement logic into {@link FluidTankItem} so that holding a dye in the offhand
 * applies color immediately when placing fluid tanks, including multi-placed tanks via {@code tryMultiPlace}.
 */
@Mixin(value = FluidTankItem.class, remap = false)
public class FluidTankItemMixin {

    @Unique
    private static final ThreadLocal<DyeColor> BNB$PLACEMENT_DYE = new ThreadLocal<>();

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

    @Unique
    private static @Nullable DyeColor bnb$getDyeOffhandOrSurface(final BlockPlaceContext ctx) {
        DyeColor dye = bnb$getOffhandDyeColor(ctx);

        if (ctx.getClickedFace().getAxis().isVertical()) {
            final BlockPos placedOnPos = ctx.getClickedPos().relative(ctx.getClickedFace().getOpposite());
            final DyeableTankBehaviour surfaceBehaviour = BlockEntityBehaviour.get(
                    ctx.getLevel(), placedOnPos, DyeableTankBehaviour.TYPE
            );
            if (surfaceBehaviour != null && surfaceBehaviour.getColor() != null) {
                dye = surfaceBehaviour.getColor();
            }
        }
        return dye;
    }

    @Inject(method = "place", at = @At("HEAD"))
    private void bnb$savePendingDyeColor(final BlockPlaceContext ctx,
                                         final CallbackInfoReturnable<InteractionResult> cir) {
        final DyeColor dye = bnb$getDyeOffhandOrSurface(ctx);
        BNB$PLACEMENT_DYE.set(dye);

        if (dye != null) {
            BnbAdvancements.DYE_FLUID_COMPONENT.awardTo(ctx.getPlayer());
            DyeableTransitionHelper.savePendingPlacementColor(
                    ctx.getLevel(), ctx.getClickedPos(), dye
            );
        }
    }

    @Inject(
            method = "place",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/BlockItem;place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;",
                    shift = At.Shift.AFTER)
    )
    private void bnb$applyEarlyClientColor(final BlockPlaceContext ctx,
                                           final CallbackInfoReturnable<InteractionResult> cir) {
        final DyeColor dye = BNB$PLACEMENT_DYE.get();
        if (dye != null && ctx.getLevel().isClientSide()) {
            final DyeableTankBehaviour behaviour = BlockEntityBehaviour.get(
                    ctx.getLevel(), ctx.getClickedPos(), DyeableTankBehaviour.TYPE
            );
            if (behaviour != null) {
                behaviour.applyColorClientOnly(dye);
            }
        }
    }

    @Inject(method = "place", at = @At("RETURN"))
    private void bnb$applyDyeAfterPlacement(final BlockPlaceContext ctx,
                                            final CallbackInfoReturnable<InteractionResult> cir) {
        final DyeColor dye = BNB$PLACEMENT_DYE.get();
        BNB$PLACEMENT_DYE.remove();

        try {
            if (dye != null && ctx.getLevel().isClientSide()) {
                final DyeableTankBehaviour behaviour = BlockEntityBehaviour.get(
                        ctx.getLevel(), ctx.getClickedPos(), DyeableTankBehaviour.TYPE
                );
                if (behaviour != null) {
                    behaviour.applyColorClientOnly(dye);
                }
            }
        } finally {
            if (dye != null) {
                DyeableTransitionHelper.consumePendingPlacementColor(
                        ctx.getLevel(), ctx.getClickedPos()
                );
            }
        }
    }

    @Redirect(
            method = "tryMultiPlace",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;"),
            remap = false
    )
    private InteractionResult bnb$wrapSubPlacement(final BlockItem instance, final BlockPlaceContext context) {
        final DyeColor dye = BNB$PLACEMENT_DYE.get();
        if (dye != null) {
            BnbAdvancements.DYE_FLUID_COMPONENT.awardTo(context.getPlayer());
            DyeableTransitionHelper.savePendingPlacementColor(context.getLevel(), context.getClickedPos(), dye);
        }
        try {
            final InteractionResult result = instance.place(context);
            if (dye != null && context.getLevel().isClientSide()) {
                final DyeableTankBehaviour behaviour = BlockEntityBehaviour.get(
                        context.getLevel(), context.getClickedPos(), DyeableTankBehaviour.TYPE
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

    @Inject(method = "updateCustomBlockEntityTag", at = @At("RETURN"))
    private void bnb$applyDyeToBlockEntity(
            final BlockPos pos, final Level level, @Nullable final Player player,
            final ItemStack stack, final BlockState state,
            final CallbackInfoReturnable<Boolean> cir
    ) {
        if (level.isClientSide() || player == null) {
            return;
        }
        final DyeColor dye = BNB$PLACEMENT_DYE.get();
        if (dye != null) {
            final DyeableTankBehaviour behaviour = BlockEntityBehaviour.get(
                    level, pos, DyeableTankBehaviour.TYPE
            );
            if (behaviour != null) {
                behaviour.setColor(dye);
            }
        }
    }

}
