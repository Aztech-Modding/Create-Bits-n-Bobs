package com.kipti.bnb.content.kinetics.cogwheel_chain.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class CogwheelChainBreakActions {

    private CogwheelChainBreakActions() {
    }

    public static boolean breakChain(final Level level, final BlockPos pos, @Nullable final Player player) {
        final BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof final CogwheelChainBlockEntity chainBE)) {
            return false;
        }

        final boolean infinite = player != null && player.hasInfiniteMaterials();
        final ItemStack drops = chainBE.destroyChain(player == null);

        if (player != null && !infinite && !drops.isEmpty()) {
            player.getInventory().placeItemBackInInventory(drops);
        }

        return true;
    }
}
