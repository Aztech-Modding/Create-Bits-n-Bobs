package com.kipti.bnb.content.kinetics.cogwheel_carriage.block;

import com.kipti.bnb.content.kinetics.cogwheel_carriage.contraption.CogwheelChainCarriageContraptionEntity;
import com.kipti.bnb.network.packets.from_client.CogwheelChainCarriageQueueDisassemblyPacket;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CogwheelChainCarriageMovingInteraction extends MovingInteractionBehaviour {

    @Override
    public boolean handlePlayerInteraction(final Player player,
                                           final InteractionHand activeHand,
                                           final BlockPos localPos,
                                           final AbstractContraptionEntity contraptionEntity) {
        if (!(contraptionEntity instanceof final CogwheelChainCarriageContraptionEntity cccce)) {
            return false;
        }

        final ItemStack itemInHand = player.getItemInHand(activeHand);

        if (AllItems.WRENCH.isIn(itemInHand)) {
            CatnipServices.NETWORK.sendToServer(new CogwheelChainCarriageQueueDisassemblyPacket(cccce.getId()));
            return true;
        }
        return false;
    }

}
