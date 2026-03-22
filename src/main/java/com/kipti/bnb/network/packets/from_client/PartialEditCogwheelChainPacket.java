package com.kipti.bnb.network.packets.from_client;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour.CogwheelChainBehaviour;
import com.kipti.bnb.content.kinetics.cogwheel_chain.edit.CogwheelChainPartialEditContext;
import com.kipti.bnb.content.kinetics.cogwheel_chain.edit.CogwheelChainPartialEditInsertionPlan;
import com.kipti.bnb.content.kinetics.cogwheel_chain.edit.CogwheelChainPartialEditInsertionPlanner;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainPathfinder;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PathedCogwheelNode;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelNode;
import com.kipti.bnb.content.kinetics.cogwheel_chain.placement.ChainInteractionFailedException;
import com.kipti.bnb.content.kinetics.cogwheel_chain.segment.CogwheelChainSegment;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
import com.kipti.bnb.network.BnbPackets;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Sent from client to server when a player confirms a partial chain edit.
 * The server places the cogwheel block, then inserts the new node into the chain.
 */
public record PartialEditCogwheelChainPacket(
        BlockPos controllerPos,
        BlockPos newCogwheelPos,
        Direction hitDirection,
        int hand,
        float chainPosition,
        int startNodeIndex,
        int endNodeIndex,
        PlacingCogwheelNode startNode,
        PlacingCogwheelNode endNode,
        CogwheelChainType chainType,
        Holder<Item> chainItemType
) implements ServerboundPacketPayload {

    private static final float SEGMENT_POSITION_EPSILON = 1.0E-3f;

    public PartialEditCogwheelChainPacket {
        controllerPos = controllerPos.immutable();
        newCogwheelPos = newCogwheelPos.immutable();
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, PartialEditCogwheelChainPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                BlockPos.STREAM_CODEC.encode(buf, packet.controllerPos);
                BlockPos.STREAM_CODEC.encode(buf, packet.newCogwheelPos);
                ByteBufCodecs.INT.encode(buf, packet.hitDirection.ordinal());
                ByteBufCodecs.INT.encode(buf, packet.hand);
                ByteBufCodecs.FLOAT.encode(buf, packet.chainPosition);
                ByteBufCodecs.INT.encode(buf, packet.startNodeIndex);
                ByteBufCodecs.INT.encode(buf, packet.endNodeIndex);
                PlacingCogwheelNode.STREAM_CODEC.encode(buf, packet.startNode);
                PlacingCogwheelNode.STREAM_CODEC.encode(buf, packet.endNode);
                CogwheelChainType.STREAM_CODEC.encode(buf, packet.chainType);
                ByteBufCodecs.holderRegistry(Registries.ITEM).encode(buf, packet.chainItemType);
            },
            buf -> new PartialEditCogwheelChainPacket(
                    BlockPos.STREAM_CODEC.decode(buf),
                    BlockPos.STREAM_CODEC.decode(buf),
                    Direction.values()[ByteBufCodecs.INT.decode(buf)],
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    PlacingCogwheelNode.STREAM_CODEC.decode(buf),
                    PlacingCogwheelNode.STREAM_CODEC.decode(buf),
                    CogwheelChainType.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.holderRegistry(Registries.ITEM).decode(buf)
            )
    );

    @Override
    public void handle(final ServerPlayer player) {
        final Level level = player.level();
        final CogwheelChainBehaviour behaviour = SuperBlockEntityBehaviour.get(level, this.controllerPos, CogwheelChainBehaviour.TYPE);
        if (behaviour == null || !behaviour.isController())
            return;

        final CogwheelChain existingChain = behaviour.getControlledChain();
        if (existingChain == null || existingChain.getReturnedItem() != this.chainItemType.value())
            return;

        final InteractionHand interactionHand = InteractionHand.values()[Math.min(this.hand, InteractionHand.values().length - 1)];
        final ItemStack heldStack = player.getItemInHand(interactionHand);
        if (!(heldStack.getItem() instanceof final BlockItem blockItem))
            return;

        final BlockState originalState = level.getBlockState(this.newCogwheelPos);
        if (!originalState.canBeReplaced())
            return;

        final BlockState placementState = this.resolvePlacementState(level, player, interactionHand, heldStack, blockItem);
        if (placementState == null)
            return;

        level.setBlock(this.newCogwheelPos, placementState, Block.UPDATE_ALL);

        final CogwheelChainPartialEditContext editContext = this.resolveEditContext(existingChain);
        if (editContext == null) {
            level.setBlock(this.newCogwheelPos, originalState, Block.UPDATE_ALL);
            return;
        }

        final CogwheelChainPartialEditInsertionPlan insertionPlan = CogwheelChainPartialEditInsertionPlanner.plan(
                existingChain,
                editContext,
                this.newCogwheelPos,
                placementState
        );
        if (insertionPlan == null) {
            level.setBlock(this.newCogwheelPos, originalState, Block.UPDATE_ALL);
            return;
        }

        final PlacingCogwheelChain rebuiltChain = insertionPlan.rebuiltChain();
        if (rebuiltChain.checkMissingNodesInLevel(level, existingChain.getChainType())) {
            level.setBlock(this.newCogwheelPos, originalState, Block.UPDATE_ALL);
            return;
        }

        final List<PathedCogwheelNode> chainGeometry = this.buildChainGeometry(rebuiltChain);
        if (chainGeometry == null) {
            level.setBlock(this.newCogwheelPos, originalState, Block.UPDATE_ALL);
            return;
        }

        final int addedCost = insertionPlan.addedCost();
        if (!player.hasInfiniteMaterials() && !this.tryConsumeChains(player, existingChain.getReturnedItem(), addedCost)) {
            level.setBlock(this.newCogwheelPos, originalState, Block.UPDATE_ALL);
            return;
        }

        final PlacingCogwheelChain existingWorldChain = CogwheelChainPartialEditInsertionPlanner.toWorldChain(existingChain, this.controllerPos);
        final CogwheelChain rebuiltCogwheelChain = new CogwheelChain(
                chainGeometry,
                existingChain.getChainType(),
                existingChain.getReturnedItem()
        );

        try {
            this.replaceChain(level, behaviour, rebuiltCogwheelChain, rebuiltChain, existingChain, existingWorldChain);
        } catch (final RuntimeException exception) {
            level.setBlock(this.newCogwheelPos, originalState, Block.UPDATE_ALL);
            if (!player.hasInfiniteMaterials()) {
                this.refundChains(player, existingChain.getReturnedItem(), addedCost);
            }
            CreateBitsnBobs.LOGGER.error("Failed to apply partial cogwheel-chain edit at {}", this.controllerPos, exception);
            return;
        }

        if (!player.hasInfiniteMaterials()) {
            heldStack.shrink(1);
            this.refundChains(player, existingChain.getReturnedItem(), insertionPlan.refundedCost());
        }
    }

    private @Nullable BlockState resolvePlacementState(final Level level, final ServerPlayer player,
                                                       final InteractionHand interactionHand,
                                                       final ItemStack heldStack, final BlockItem blockItem) {
        final BlockHitResult fakeHit = new BlockHitResult(
                Vec3.atCenterOf(this.newCogwheelPos),
                this.hitDirection,
                this.newCogwheelPos,
                false
        );
        final BlockPlaceContext placeContext = new BlockPlaceContext(
                new UseOnContext(level, player, interactionHand, heldStack, fakeHit));
        return blockItem.getBlock().getStateForPlacement(placeContext);
    }

    private @Nullable CogwheelChainPartialEditContext resolveEditContext(final CogwheelChain existingChain) {
        final CogwheelChainSegment authoritativeSegment = CogwheelChainPartialEditInsertionPlanner.resolveBetweenNodesSegment(
                existingChain,
                this.startNodeIndex
        );
        if (authoritativeSegment == null || !this.isWithinSelectedSegment(authoritativeSegment))
            return null;

        return new CogwheelChainPartialEditContext(
                this.controllerPos,
                this.chainPosition,
                authoritativeSegment,
                this.startNodeIndex,
                this.endNodeIndex,
                this.startNode,
                this.endNode,
                this.chainType,
                this.chainItemType.value()
        );
    }

    private boolean isWithinSelectedSegment(final CogwheelChainSegment authoritativeSegment) {
        return this.chainPosition + SEGMENT_POSITION_EPSILON >= authoritativeSegment.startDist()
                && this.chainPosition - SEGMENT_POSITION_EPSILON <= authoritativeSegment.endDist();
    }

    private @Nullable List<PathedCogwheelNode> buildChainGeometry(final PlacingCogwheelChain rebuiltChain) {
        try {
            return CogwheelChainPathfinder.buildChainPath(rebuiltChain);
        } catch (final ChainInteractionFailedException ignored) {
            return null;
        }
    }

    private boolean tryConsumeChains(final ServerPlayer player, final Item chainItem, final int addedCost) {
        if (addedCost <= 0)
            return true;

        final boolean hasEnough = ChainConveyorBlockEntity.getChainsFromInventory(
                player,
                chainItem.getDefaultInstance(),
                addedCost,
                true
        );
        if (!hasEnough)
            return false;

        ChainConveyorBlockEntity.getChainsFromInventory(
                player,
                chainItem.getDefaultInstance(),
                addedCost,
                false
        );
        return true;
    }

    private void refundChains(final ServerPlayer player, final Item chainItem, final int chainsToRefund) {
        if (chainsToRefund <= 0)
            return;

        player.getInventory().placeItemBackInInventory(
                chainItem.getDefaultInstance().copyWithCount(chainsToRefund)
        );
    }

    private void replaceChain(final Level level,
                              final CogwheelChainBehaviour behaviour,
                              final CogwheelChain rebuiltCogwheelChain,
                              final PlacingCogwheelChain rebuiltChain,
                              final CogwheelChain existingChain,
                              final PlacingCogwheelChain existingWorldChain) {
        behaviour.clearStoredChains();
        behaviour.destroyChain(false, false);

        try {
            rebuiltCogwheelChain.placeInLevel(level, rebuiltChain);
        } catch (final RuntimeException exception) {
            this.restoreChain(level, existingChain, existingWorldChain);
            throw exception;
        }
    }

    private void restoreChain(final Level level,
                              final CogwheelChain existingChain,
                              final PlacingCogwheelChain existingWorldChain) {
        try {
            existingChain.placeInLevel(level, existingWorldChain);
        } catch (final RuntimeException restoreException) {
            CreateBitsnBobs.LOGGER.error("Failed to restore cogwheel chain after partial edit failure at {}", this.controllerPos, restoreException);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return BnbPackets.PARTIAL_EDIT_COGWHEEL_CHAIN;
    }
}