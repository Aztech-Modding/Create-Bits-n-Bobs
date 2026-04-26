package com.kipti.bnb.content.decoration.dyeable;

import com.kipti.bnb.registry.content.BnbAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class DyeableBlockItemHelper {

    @Nullable
    public static DyeColor getOffhandDyeColor(final BlockPlaceContext context) {
        return DyeableBlockItemHelper.getOffhandDyeColor(context.getPlayer());
    }

    @Nullable
    public static DyeColor getOffhandDyeColor(@Nullable final Player player) {
        if (player == null) {
            return null;
        }
        final ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof final DyeItem dyeItem) {
            return dyeItem.getDyeColor();
        }
        return null;
    }

    public static void savePendingPlacementColor(
            final BlockPlaceContext context,
            final BlockPos pos,
            @Nullable final DyeColor color
    ) {
        if (color == null) {
            return;
        }
        BnbAdvancements.DYE_FLUID_COMPONENT.awardTo(context.getPlayer());
        DyeableTransitionHelper.savePendingPlacementColor(context.getLevel(), pos, color);
    }

    public static void consumePendingPlacementColor(final Level level, final BlockPos pos) {
        DyeableTransitionHelper.consumePendingPlacementColor(level, pos);
    }

    public static void applyColorClientOnly(
            final Level level,
            final BlockPos pos,
            final BehaviourType<? extends BaseDyeableBehaviour> type,
            @Nullable final DyeColor color
    ) {
        if (color == null || !level.isClientSide) {
            return;
        }
        final BaseDyeableBehaviour behaviour = BlockEntityBehaviour.get(level, pos, type);
        if (behaviour != null) {
            behaviour.applyColorClientOnly(color);
        }
    }

    public static void setColor(
            final Level level,
            final BlockPos pos,
            final BehaviourType<? extends BaseDyeableBehaviour> type,
            @Nullable final DyeColor color
    ) {
        if (level.isClientSide) {
            return;
        }
        final BaseDyeableBehaviour behaviour = BlockEntityBehaviour.get(level, pos, type);
        if (behaviour != null) {
            behaviour.setColor(color);
        }
    }

}
