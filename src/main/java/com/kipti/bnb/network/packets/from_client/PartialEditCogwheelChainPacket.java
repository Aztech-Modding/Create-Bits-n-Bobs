package com.kipti.bnb.network.packets.from_client;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour.CogwheelChainBehaviour;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainCandidate;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainPathfinder;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PathedCogwheelNode;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelNode;
import com.kipti.bnb.content.kinetics.cogwheel_chain.placement.ChainInteractionFailedException;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
import com.kipti.bnb.network.BnbPackets;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Sent from client to server when a player confirms a partial chain edit,
 * adding a new cogwheel to an existing chain.
 */
public record PartialEditCogwheelChainPacket(
        BlockPos controllerPos,
        BlockPos newCogwheelPos,
        CogwheelChainType chainType,
        Holder<Item> chainItemType
) implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, PartialEditCogwheelChainPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    PartialEditCogwheelChainPacket::controllerPos,
                    BlockPos.STREAM_CODEC,
                    PartialEditCogwheelChainPacket::newCogwheelPos,
                    CogwheelChainType.STREAM_CODEC,
                    PartialEditCogwheelChainPacket::chainType,
                    ByteBufCodecs.holderRegistry(Registries.ITEM),
                    PartialEditCogwheelChainPacket::chainItemType,
                    PartialEditCogwheelChainPacket::new
            );

    @Override
    public void handle(final ServerPlayer player) {
        final CogwheelChainBehaviour behaviour = SuperBlockEntityBehaviour.get(player.level(), this.controllerPos, CogwheelChainBehaviour.TYPE);
        if (behaviour == null || !behaviour.isController())
            return;

        final CogwheelChain existingChain = behaviour.getControlledChain();
        if (existingChain == null)
            return;

        final BlockState newBlockState = player.level().getBlockState(this.newCogwheelPos);
        final CogwheelChainCandidate candidate = CogwheelChainCandidate.getForBlock(newBlockState);
        if (candidate == null)
            return;

        if (!this.chainType.getCogwheelPredicate().test(newBlockState.getBlock()))
            return;

        final PlacingCogwheelChain newPlacingChain = buildExtendedPlacingChain(existingChain, candidate);
        if (newPlacingChain == null)
            return;

        if (newPlacingChain.maxBounds() > PlacingCogwheelChain.MAX_CHAIN_BOUNDS)
            return;

        if (newPlacingChain.checkMissingNodesInLevel(player.level(), this.chainType))
            return;

        final int newCost = newPlacingChain.getChainsRequiredInLoop();
        final int oldCost = existingChain.getChainsRequired();
        final int costDifference = Math.max(newCost - oldCost, 0);

        if (costDifference > 0 && !player.hasInfiniteMaterials()) {
            final boolean hasEnough = ChainConveyorBlockEntity.getChainsFromInventory(
                    player, this.chainItemType.value().getDefaultInstance(), costDifference, true
            );
            if (!hasEnough)
                return;
            ChainConveyorBlockEntity.getChainsFromInventory(
                    player, this.chainItemType.value().getDefaultInstance(), costDifference, false
            );
        }

        final List<PathedCogwheelNode> chainGeometry;
        try {
            chainGeometry = CogwheelChainPathfinder.buildChainPath(newPlacingChain);
        } catch (final ChainInteractionFailedException ignored) {
            return;
        }
        if (chainGeometry == null)
            return;

        behaviour.destroyChain(false, false);

        final CogwheelChain newChain = new CogwheelChain(chainGeometry, this.chainType, this.chainItemType.value());
        newChain.placeInLevel(player.level(), newPlacingChain);
    }

    private PlacingCogwheelChain buildExtendedPlacingChain(final CogwheelChain existingChain,
                                                           final CogwheelChainCandidate newCandidate) {
        final List<PathedCogwheelNode> existingNodes = existingChain.getChainPathCogwheelNodes();
        final BlockPos origin = this.controllerPos;

        final List<PlacingCogwheelNode> worldNodes = new ArrayList<>();
        for (final PathedCogwheelNode pathed : existingNodes) {
            final BlockPos worldPos = origin.offset(pathed.localPos());
            worldNodes.add(new PlacingCogwheelNode(worldPos, pathed.rotationAxis(), pathed.isLarge(), pathed.hasSmallCogwheelOffset()));
        }

        int bestInsertIndex = -1;
        double bestDistance = Double.MAX_VALUE;

        for (int i = 0; i < worldNodes.size(); i++) {
            final PlacingCogwheelNode nodeA = worldNodes.get(i);
            final PlacingCogwheelNode nodeB = worldNodes.get((i + 1) % worldNodes.size());

            final boolean canConnectToA = canConnect(nodeA, newCandidate);
            final boolean canConnectToB = canConnect(newCandidate, nodeB);

            if (canConnectToA && canConnectToB) {
                final double distance = nodeA.center().distanceTo(this.newCogwheelPos.getCenter())
                        + this.newCogwheelPos.getCenter().distanceTo(nodeB.center());
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestInsertIndex = i + 1;
                }
            }
        }

        if (bestInsertIndex == -1)
            return null;

        final PlacingCogwheelNode newNode = new PlacingCogwheelNode(
                this.newCogwheelPos,
                newCandidate.axis(),
                newCandidate.isLarge(),
                newCandidate.hasSmallCogwheelOffset()
        );

        final List<PlacingCogwheelNode> extendedNodes = new ArrayList<>(worldNodes);
        extendedNodes.add(bestInsertIndex, newNode);

        return new PlacingCogwheelChain(extendedNodes);
    }

    private boolean canConnect(final PlacingCogwheelNode from,
                               final CogwheelChainCandidate toCandidate) {
        final PlacingCogwheelNode toNode = new PlacingCogwheelNode(
                this.newCogwheelPos,
                toCandidate.axis(),
                toCandidate.isLarge(),
                toCandidate.hasSmallCogwheelOffset()
        );
        return !CogwheelChainPathfinder.getValidPathSteps(from, toNode).isEmpty();
    }

    private boolean canConnect(final CogwheelChainCandidate fromCandidate,
                               final PlacingCogwheelNode to) {
        final PlacingCogwheelNode fromNode = new PlacingCogwheelNode(
                this.newCogwheelPos,
                fromCandidate.axis(),
                fromCandidate.isLarge(),
                fromCandidate.hasSmallCogwheelOffset()
        );
        return !CogwheelChainPathfinder.getValidPathSteps(fromNode, to).isEmpty();
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return BnbPackets.PARTIAL_EDIT_COGWHEEL_CHAIN;
    }
}
