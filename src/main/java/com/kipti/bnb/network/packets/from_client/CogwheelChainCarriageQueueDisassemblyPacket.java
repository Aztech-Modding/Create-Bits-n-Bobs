package com.kipti.bnb.network.packets.from_client;

import com.kipti.bnb.content.kinetics.cogwheel_carriage.contraption.CogwheelChainCarriageContraptionEntity;
import com.kipti.bnb.network.BnbPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public record CogwheelChainCarriageQueueDisassemblyPacket(int entityId) implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, CogwheelChainCarriageQueueDisassemblyPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.entityId);
            },
            buf -> new CogwheelChainCarriageQueueDisassemblyPacket(buf.readInt())
    );

    @Override
    public void handle(final ServerPlayer player) {
        final Level level = player.level();

        final Entity entity = level.getEntity(this.entityId);
        if (!(entity instanceof final CogwheelChainCarriageContraptionEntity cccce)) {
            return;
        }
        cccce.disassembleNextTick();
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return BnbPackets.COGWHEEL_CHAIN_CARRIAGE_QUEUE_DISASSEMBLE;
    }

}
