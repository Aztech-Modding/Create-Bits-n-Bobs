package com.kipti.bnb.network.packets.from_client;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.*;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
import com.kipti.bnb.network.BnbPackets;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

import java.util.List;

public record PlaceCogwheelChainPacket(
        PlacingCogwheelChain worldSpacePartialChain,
        CogwheelChainType chainType,
        int priorityChainTakeHand
) implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceCogwheelChainPacket> STREAM_CODEC =
            StreamCodec.composite(
                    PlacingCogwheelChain.STREAM_CODEC,
                    PlaceCogwheelChainPacket::worldSpacePartialChain,
                    CogwheelChainType.STEAM_CODEC,
                    PlaceCogwheelChainPacket::chainType,
                    ByteBufCodecs.INT,
                    PlaceCogwheelChainPacket::priorityChainTakeHand,
                    PlaceCogwheelChainPacket::new
            );

    @Override
    public void handle(ServerPlayer player) {
        //Server side validation of the chain
        if (worldSpacePartialChain.maxBounds() > PlacingCogwheelChain.MAX_CHAIN_BOUNDS)
            return;

        if (worldSpacePartialChain.checkMissingNodesInLevel(player.level(), chainType))
            return;

        final int chainsRequired = worldSpacePartialChain.getChainsRequiredInLoop();

        final boolean hasEnough = player.hasInfiniteMaterials() || ChainConveyorBlockEntity.getChainsFromInventory(player, Items.CHAIN.getDefaultInstance(), chainsRequired, true);
        if (!hasEnough)
            return;
        if (!player.hasInfiniteMaterials())
            ChainConveyorBlockEntity.getChainsFromInventory(player, Items.CHAIN.getDefaultInstance(), chainsRequired, false);

        final List<PathedCogwheelNode> chainGeometry;
        try {
            chainGeometry = CogwheelChainPathfinder.buildChainPath(worldSpacePartialChain);
        } catch (final
        ChainInteractionFailedException ignored) { //We assume the client has been notified if the path was invalid, anything else is tampering
            return;
        }
        if (chainGeometry == null)
            return;

        final CogwheelChain chain = new CogwheelChain(chainGeometry, chainType);

        chain.placeInLevel(player.level(), worldSpacePartialChain);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return BnbPackets.PLACE_COGWHEEL_CHAIN;
    }

}
