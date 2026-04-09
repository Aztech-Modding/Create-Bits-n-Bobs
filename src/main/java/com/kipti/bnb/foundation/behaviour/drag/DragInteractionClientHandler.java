package com.kipti.bnb.foundation.behaviour.drag;

import com.kipti.bnb.CreateBitsnBobs;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Opens a {@link DragInteractionScreen} when a block with {@link DragInteractionBehaviour} is right-clicked. */
@EventBusSubscriber(value = Dist.CLIENT, modid = CreateBitsnBobs.MOD_ID)
public class DragInteractionClientHandler {

    @SubscribeEvent
    public static void onBlockActivated(final PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide) {
            return;
        }

        final BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());
        if (!(blockEntity instanceof final SmartBlockEntity sbe)) {
            return;
        }

        final DragInteractionBehaviour behaviour = sbe.getBehaviour(DragInteractionBehaviour.TYPE);
        if (behaviour == null) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        Minecraft.getInstance().setScreen(new DragInteractionScreen(
                event.getPos(),
                behaviour.getValue(),
                behaviour.getMin(),
                behaviour.getMax()
        ));
    }
}
