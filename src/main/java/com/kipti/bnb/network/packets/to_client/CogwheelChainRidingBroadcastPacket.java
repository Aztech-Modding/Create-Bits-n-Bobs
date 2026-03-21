package com.kipti.bnb.network.packets.to_client;

import com.kipti.bnb.content.kinetics.cogwheel_chain.riding.CogwheelChainSkyhookRenderer;
import com.kipti.bnb.network.BnbPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public record CogwheelChainRidingBroadcastPacket(
        Collection<UUID> ridingPlayerUUIDs
) implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, CogwheelChainRidingBroadcastPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> {
                        buf.writeVarInt(packet.ridingPlayerUUIDs.size());
                        for (UUID uuid : packet.ridingPlayerUUIDs) {
                            buf.writeUUID(uuid);
                        }
                    },
                    buf -> {
                        int size = buf.readVarInt();
                        HashSet<UUID> uuids = new HashSet<>();
                        for (int i = 0; i < size; i++) {
                            uuids.add(buf.readUUID());
                        }
                        return new CogwheelChainRidingBroadcastPacket(uuids);
                    }
            );

    @Override
    public void handle(final LocalPlayer player) {
        CogwheelChainSkyhookRenderer.updatePlayerList(this.ridingPlayerUUIDs);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return BnbPackets.COGWHEEL_CHAIN_RIDING_BROADCAST;
    }
}
