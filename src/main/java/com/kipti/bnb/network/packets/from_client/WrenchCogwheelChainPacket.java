package com.kipti.bnb.network.packets.from_client;

import com.kipti.bnb.content.kinetics.cogwheel_chain.block.CogwheelChainBlockEntity;
import com.kipti.bnb.network.BnbPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

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
        final BlockPos pos = controllerPos;
        final BlockEntity be = player.level().getBlockEntity(pos);
        if (!(be instanceof final CogwheelChainBlockEntity chainBE)) {
            return;
        }

        final boolean infinite = player.hasInfiniteMaterials();
        final var drops = chainBE.destroyChain(!infinite);

        if (!infinite && !drops.isEmpty()) {
            final Inventory inv = player.getInventory();
            inv.placeItemBackInInventory(drops);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return BnbPackets.WRENCH_COGWHEEL_CHAIN;
    }
}

