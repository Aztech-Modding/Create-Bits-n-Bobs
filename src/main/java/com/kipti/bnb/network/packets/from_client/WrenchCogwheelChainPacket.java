package com.kipti.bnb.network.packets.from_client;

import com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour.CogwheelChainBehaviour;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelChain;
import com.kipti.bnb.network.BnbPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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
        if (player.distanceToSqr(
                this.controllerPos.getX() + 0.5,
                this.controllerPos.getY() + 0.5,
                this.controllerPos.getZ() + 0.5
        ) > PlacingCogwheelChain.MAX_CHAIN_INTERACTION_DISTANCE_SQ)
            return;

        final Level level = player.level();
        CogwheelChainBehaviour.breakChain(level, this.controllerPos, player);
        final CogwheelChainBehaviour behaviour = CogwheelChainBehaviour.get(
                level,
                this.controllerPos,
                CogwheelChainBehaviour.TYPE
        );
        if (behaviour == null)
            return;

        final boolean infinite = player.hasInfiniteMaterials();
        final ItemStack drops = behaviour.destroyChain(false, true);

        if (!infinite && !drops.isEmpty()) {
            player.getInventory().placeItemBackInInventory(drops);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return BnbPackets.WRENCH_COGWHEEL_CHAIN;
    }
}


