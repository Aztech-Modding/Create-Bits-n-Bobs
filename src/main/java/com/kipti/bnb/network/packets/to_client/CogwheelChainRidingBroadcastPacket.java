package com.kipti.bnb.network.packets.to_client;

import com.kipti.bnb.content.kinetics.cogwheel_chain.riding.PlayerSkyhookRendererBridge;
import com.kipti.bnb.network.BnbPackets;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public record CogwheelChainRidingBroadcastPacket(
        Collection<UUID> ridingPlayerUUIDs
) implements ClientboundPacketPayload {

    public static final StreamCodec<ByteBuf, CogwheelChainRidingBroadcastPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(HashSet::new, UUIDUtil.STREAM_CODEC),
                    CogwheelChainRidingBroadcastPacket::ridingPlayerUUIDs,
                    CogwheelChainRidingBroadcastPacket::new
            );

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handle(final LocalPlayer player) {
        PlayerSkyhookRendererBridge.updateBnbHangingPlayers(this.ridingPlayerUUIDs);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return BnbPackets.COGWHEEL_CHAIN_RIDING_BROADCAST;
    }
}
