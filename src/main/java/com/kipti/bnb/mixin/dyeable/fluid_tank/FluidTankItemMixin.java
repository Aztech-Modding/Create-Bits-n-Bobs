package com.kipti.bnb.mixin.dyeable.fluid_tank;

import com.kipti.bnb.content.decoration.dyeable.DyeableBlockItemHelper;
import com.kipti.bnb.content.decoration.dyeable.tanks.DyeableTankBehaviour;
import com.simibubi.create.content.fluids.tank.FluidTankItem;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
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

@Mixin(value = FluidTankItem.class, remap = false)
public class FluidTankItemMixin {

    @Unique
    private static final ThreadLocal<DyeColor> BNB$PLACEMENT_DYE = new ThreadLocal<>();

    @Unique
    private static @Nullable DyeColor bnb$getDyeOffhandOrSurface(final BlockPlaceContext ctx) {
        DyeColor dye = DyeableBlockItemHelper.getOffhandDyeColor(ctx);

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
            DyeableBlockItemHelper.savePendingPlacementColor(ctx, ctx.getClickedPos(), dye);
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
        DyeableBlockItemHelper.applyColorClientOnly(ctx.getLevel(), ctx.getClickedPos(), DyeableTankBehaviour.TYPE, dye);
    }

    @Inject(method = "place", at = @At("RETURN"))
    private void bnb$applyDyeAfterPlacement(final BlockPlaceContext ctx,
                                            final CallbackInfoReturnable<InteractionResult> cir) {
        final DyeColor dye = BNB$PLACEMENT_DYE.get();
        BNB$PLACEMENT_DYE.remove();

        try {
            DyeableBlockItemHelper.applyColorClientOnly(ctx.getLevel(), ctx.getClickedPos(), DyeableTankBehaviour.TYPE, dye);
        } finally {
            if (dye != null) {
                DyeableBlockItemHelper.consumePendingPlacementColor(ctx.getLevel(), ctx.getClickedPos());
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
            DyeableBlockItemHelper.savePendingPlacementColor(context, context.getClickedPos(), dye);
        }
        try {
            final InteractionResult result = instance.place(context);
            DyeableBlockItemHelper.applyColorClientOnly(
                    context.getLevel(),
                    context.getClickedPos(),
                    DyeableTankBehaviour.TYPE,
                    dye
            );
            return result;
        } finally {
            if (dye != null) {
                DyeableBlockItemHelper.consumePendingPlacementColor(context.getLevel(), context.getClickedPos());
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
            DyeableBlockItemHelper.setColor(level, pos, DyeableTankBehaviour.TYPE, dye);
        }
    }

}
