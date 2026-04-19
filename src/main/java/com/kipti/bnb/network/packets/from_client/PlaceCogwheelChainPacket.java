package com.kipti.bnb.network.packets.from_client;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainPathfinder;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PathedCogwheelNode;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.placement.ChainInteractionFailedException;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
import com.kipti.bnb.network.BnbPackets;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

import java.util.List;

public record PlaceCogwheelChainPacket(
        PlacingCogwheelChain worldSpacePartialChain,
        CogwheelChainType chainType,
        int priorityChainTakeHand,
        Holder<Item> chainItemType
) implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceCogwheelChainPacket> STREAM_CODEC =
            StreamCodec.composite(
                    PlacingCogwheelChain.STREAM_CODEC,
                    PlaceCogwheelChainPacket::worldSpacePartialChain,
                    CogwheelChainType.STREAM_CODEC,
                    PlaceCogwheelChainPacket::chainType,
                    ByteBufCodecs.INT,
                    PlaceCogwheelChainPacket::priorityChainTakeHand,
                    ByteBufCodecs.holderRegistry(Registries.ITEM),
                    PlaceCogwheelChainPacket::chainItemType,
                    PlaceCogwheelChainPacket::new
            );

    @Override
    public void handle(final ServerPlayer player) {
        //Server side validation of the chain
        if (this.worldSpacePartialChain.maxBounds() > PlacingCogwheelChain.MAX_CHAIN_BOUNDS)
            return;

        if (this.worldSpacePartialChain.checkMissingNodesInLevel(player.level(), this.chainType))
            return;

        final int chainsRequired = this.worldSpacePartialChain.getChainsRequiredInLoop(this.chainType);

        final boolean hasEnough = player.hasInfiniteMaterials() || ChainConveyorBlockEntity.getChainsFromInventory(
                player,
                this.chainItemType.value().getDefaultInstance(),
                chainsRequired,
                true
        );
        if (!hasEnough)
            return;
        if (!player.hasInfiniteMaterials())
            ChainConveyorBlockEntity.getChainsFromInventory(
                    player,
                    this.chainItemType.value().getDefaultInstance(),
                    chainsRequired,
                    false
            );

        final List<PathedCogwheelNode> chainGeometry;
        try {
            chainGeometry = CogwheelChainPathfinder.buildChainPath(this.worldSpacePartialChain);
        } catch (final
        ChainInteractionFailedException ignored) { //We assume the client has been notified if the path was invalid, anything else is tampering
            return;
        }
        if (chainGeometry == null)
            return;

        final CogwheelChain chain = new CogwheelChain(chainGeometry, this.chainType, this.chainItemType.value());

        chain.placeInLevel(player.level(), this.worldSpacePartialChain);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return BnbPackets.PLACE_COGWHEEL_CHAIN;
    }

}

