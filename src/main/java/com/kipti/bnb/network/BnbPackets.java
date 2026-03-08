package com.kipti.bnb.network;


import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.network.packets.from_client.ArticulatedTrackTiltPacket;
import com.kipti.bnb.network.packets.from_client.PlaceCogwheelChainPacket;
import com.kipti.bnb.network.packets.from_client.WrenchCogwheelChainPacket;
import com.kipti.bnb.network.packets.to_client.ApplyHeadlampQueuedOperationsPacket;
import com.kipti.bnb.network.packets.to_client.PeekCogwheelChainControllerHighlightPacket;
import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Locale;

public enum BnbPackets implements BasePacketPayload.PacketTypeProvider {
    // C2S
    PLACE_COGWHEEL_CHAIN(PlaceCogwheelChainPacket.class, PlaceCogwheelChainPacket.STREAM_CODEC),
    WRENCH_COGWHEEL_CHAIN(WrenchCogwheelChainPacket.class, WrenchCogwheelChainPacket.STREAM_CODEC),
    SET_TRACK_TILT(ArticulatedTrackTiltPacket.class, ArticulatedTrackTiltPacket.STREAM_CODEC),

    // S2C
    APPLY_HEADLAMP_QUEUED_OPERATIONS(ApplyHeadlampQueuedOperationsPacket.class, ApplyHeadlampQueuedOperationsPacket.STREAM_CODEC),
    PEEK_COGWHEEL_CHAIN_CONTROLLER_HIGHLIGHT(PeekCogwheelChainControllerHighlightPacket.class, PeekCogwheelChainControllerHighlightPacket.STREAM_CODEC),
    ;

    private final CatnipPacketRegistry.PacketType<?> type;

    <T extends BasePacketPayload> BnbPackets(final Class<T> clazz, final StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        final String name = this.name().toLowerCase(Locale.ROOT);
        this.type = new CatnipPacketRegistry.PacketType<>(
                new CustomPacketPayload.Type<>(CreateBitsnBobs.asResource(name)),
                clazz, codec
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
        return (CustomPacketPayload.Type<T>) this.type.type();
    }

    public static void register() {
        final CatnipPacketRegistry packetRegistry = new CatnipPacketRegistry(CreateBitsnBobs.MOD_ID, 1);
        for (final BnbPackets packet : BnbPackets.values()) {
            packetRegistry.registerPacket(packet.type);
        }
        packetRegistry.registerAllPackets();
    }
}

