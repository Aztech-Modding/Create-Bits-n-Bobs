package com.kipti.bnb.network.packets.from_client;

import com.kipti.bnb.content.kinetics.cogwheel_chain.riding.ServerCogwheelChainRidingHandler;
import com.kipti.bnb.mixin.ServerGamePacketListenerImplAccessor;
import com.kipti.bnb.network.BnbPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public record CogwheelChainRidingPacket(
        BlockPos controllerPos,
        boolean stop
) implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, CogwheelChainRidingPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, CogwheelChainRidingPacket::controllerPos,
                    ByteBufCodecs.BOOL, CogwheelChainRidingPacket::stop,
                    CogwheelChainRidingPacket::new
            );

    @Override
    public void handle(final ServerPlayer player) {
        player.fallDistance = 0;
        ((ServerGamePacketListenerImplAccessor) player.connection).bits_n_bobs$setAboveGroundTickCount(0);
        ((ServerGamePacketListenerImplAccessor) player.connection).bits_n_bobs$setAboveGroundVehicleTickCount(0);

        if (this.stop) {
            ServerCogwheelChainRidingHandler.handleStopRidingPacket(player);
        } else {
            ServerCogwheelChainRidingHandler.handleTTLPacket(player);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return BnbPackets.COGWHEEL_CHAIN_RIDING;
    }
}
