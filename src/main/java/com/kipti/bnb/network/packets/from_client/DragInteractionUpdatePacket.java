package com.kipti.bnb.network.packets.from_client;

import com.kipti.bnb.foundation.behaviour.drag.DragInteractionBehaviour;
import com.kipti.bnb.network.BnbPackets;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public record DragInteractionUpdatePacket(
        BlockPos pos,
        int value
) implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, DragInteractionUpdatePacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, DragInteractionUpdatePacket::pos,
                    ByteBufCodecs.VAR_INT, DragInteractionUpdatePacket::value,
                    DragInteractionUpdatePacket::new
            );

    @Override
    public void handle(ServerPlayer player) {
        if (player.distanceToSqr(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) > 100) {
            return;
        }

        BlockEntity blockEntity = player.level().getBlockEntity(this.pos);
        if (!(blockEntity instanceof SmartBlockEntity sbe)) {
            return;
        }

        DragInteractionBehaviour behaviour = sbe.getBehaviour(DragInteractionBehaviour.TYPE);
        if (behaviour != null) {
            behaviour.updateTargetValue(this.value);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return BnbPackets.DRAG_INTERACTION_UPDATE;
    }
}
