package com.kipti.bnb.network.packets.to_client;

import com.kipti.bnb.compat.computercraft.implementation.peripherals.HeadlampQueuedOperationHandler;
import com.kipti.bnb.network.BnbPackets;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record ApplyHeadlampQueuedOperationsPacket(
        Long2ByteOpenHashMap queuedChanges
) implements ClientboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ApplyHeadlampQueuedOperationsPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeVarInt(packet.queuedChanges.size());
                packet.queuedChanges.long2ByteEntrySet().forEach(entry -> {
                    buf.writeLong(entry.getLongKey());
                    buf.writeByte(entry.getByteValue());
                });
            },
            buf -> {
                Long2ByteOpenHashMap map = new Long2ByteOpenHashMap();
                int size = buf.readVarInt();
                for (int i = 0; i < size; i++) {
                    long key = buf.readLong();
                    byte value = buf.readByte();
                    map.put(key, value);
                }
                return new ApplyHeadlampQueuedOperationsPacket(map);
            }
    );

    @Override
    public void handle(LocalPlayer player) {
        HeadlampQueuedOperationHandler.applyChangeSnapshot(player.level(), queuedChanges);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return BnbPackets.APPLY_HEADLAMP_QUEUED_OPERATIONS;
    }
}

