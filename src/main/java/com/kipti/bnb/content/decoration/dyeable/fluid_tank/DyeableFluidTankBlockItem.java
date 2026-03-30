package com.kipti.bnb.content.decoration.dyeable.fluid_tank;

import com.kipti.bnb.content.decoration.dyeable.DyeableTransitionHelper;
import com.simibubi.create.content.fluids.tank.FluidTankItem;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class DyeableFluidTankBlockItem extends FluidTankItem {

    public DyeableFluidTankBlockItem(final Block block, final Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public @NonNull InteractionResult place(final BlockPlaceContext context) {
        final @Nullable DyeColor offhandDye = getOffhandDyeColor(context);
        if (offhandDye != null) {
            DyeableTransitionHelper.savePendingPlacementColor(
                    context.getLevel(), context.getClickedPos(), offhandDye
            );
        }
        try {
            final InteractionResult result = super.place(context);
            if (result.consumesAction() && offhandDye != null && context.getLevel().isClientSide()) {
                final DyeableFluidTankBehaviour behaviour = BlockEntityBehaviour.get(
                        context.getLevel(), context.getClickedPos(), DyeableFluidTankBehaviour.TYPE
                );
                if (behaviour != null) {
                    behaviour.applyColorClientOnly(offhandDye);
                }
            }
            return result;
        } finally {
            if (offhandDye != null) {
                DyeableTransitionHelper.consumePendingPlacementColor(
                        context.getLevel(), context.getClickedPos()
                );
            }
        }
    }

    @Override
    protected boolean updateCustomBlockEntityTag(
            final BlockPos pos,
            final Level level,
            final @Nullable Player player,
            final ItemStack stack,
            final BlockState state
    ) {
        final boolean result = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        if (!level.isClientSide() && player != null) {
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
        return result;
    }

    @Nullable
    private static DyeColor getOffhandDyeColor(final BlockPlaceContext context) {
        if (context.getPlayer() == null) {
            return null;
        }
        final ItemStack offhand = context.getPlayer().getOffhandItem();
        if (offhand.getItem() instanceof final DyeItem dyeItem) {
            return dyeItem.getDyeColor();
        }
        return null;
    }
}
