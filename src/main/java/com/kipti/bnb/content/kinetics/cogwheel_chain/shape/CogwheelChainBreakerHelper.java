package com.kipti.bnb.content.kinetics.cogwheel_chain.shape;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour.CogwheelChainBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CogwheelChainBreakerHelper {

    public static void breakChain(final Level level, final BlockPos pos, @Nullable final Player player) {
        final CogwheelChainBehaviour behaviour = SuperBlockEntityBehaviour.get(level, pos, CogwheelChainBehaviour.TYPE);
        if (behaviour == null)
            return;

        final boolean infinite = player != null && player.hasInfiniteMaterials();
        final ItemStack drops = behaviour.destroyChain(player == null, true);

        if (player != null && !infinite && !drops.isEmpty()) {
            player.getInventory().placeItemBackInInventory(drops);
        }
    }
}

