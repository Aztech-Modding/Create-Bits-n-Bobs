package com.kipti.bnb.network.packets.from_client;

import com.kipti.bnb.content.kinetics.cogwheel_chain.shape.CogwheelChainBreakerHelper;
import com.kipti.bnb.network.BnbPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public record WrenchCogwheelChainPacket(
        BlockPos controllerPos,
        float chainPosition
) implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, WrenchCogwheelChainPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    WrenchCogwheelChainPacket::controllerPos,
                    ByteBufCodecs.FLOAT,
                    WrenchCogwheelChainPacket::chainPosition,
                    WrenchCogwheelChainPacket::new
            );

    @Override
    public void handle(final ServerPlayer player) {
        CogwheelChainBreakerHelper.breakChain(player.level(), controllerPos, player);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return BnbPackets.WRENCH_COGWHEEL_CHAIN;
    }
}


