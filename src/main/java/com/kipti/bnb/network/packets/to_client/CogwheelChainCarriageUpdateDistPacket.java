package com.kipti.bnb.network.packets.to_client;

import com.kipti.bnb.content.kinetics.cogwheel_carriage.contraption.CogwheelChainCarriageContraptionEntity;
import com.kipti.bnb.network.BnbPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public record CogwheelChainCarriageUpdateDistPacket(
        int entityId,
        float dist
) implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, CogwheelChainCarriageUpdateDistPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.entityId);
                buf.writeFloat(packet.dist);
            },
            buf -> new CogwheelChainCarriageUpdateDistPacket(buf.readInt(), buf.readFloat())
    );

    @Override
    public void handle(final LocalPlayer player) {
        final Level level = player.level();

        final Entity entity = level.getEntity(this.entityId);
        if (!(entity instanceof final CogwheelChainCarriageContraptionEntity cccce)) {
            return;
        }
        cccce.setDistFromServer(this.dist);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return BnbPackets.COGWHEEL_CHAIN_CARRIAGE_UPDATE_DIST;
    }
}
