package com.cake.azimuth;

import com.cake.azimuth.behaviour.AzimuthSmartBlockEntityExtension;
import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber
public class AzimuthEvents {

    @SubscribeEvent
    public static void onPlayerWillDestroy(final BlockEvent.BreakEvent destroyEvent) {
        //Check if there is an asbee block entity there. Call onBlockBroken on each.
        final BlockPos pos = destroyEvent.getPos();
        final BlockEntity blockEntity = destroyEvent.getLevel().getBlockEntity(pos);
        if (blockEntity instanceof final AzimuthSmartBlockEntityExtension asbee) {
            for (final SuperBlockEntityBehaviour behaviour : asbee.azimuth$getSuperBehaviours()) {
                behaviour.onBlockBroken(destroyEvent);
            }
        }
    }


}
