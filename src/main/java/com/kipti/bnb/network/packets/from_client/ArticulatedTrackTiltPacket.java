package com.kipti.bnb.network.packets.from_client;

import com.kipti.bnb.content.articulate.ArticulatedTrackUtils;
import com.kipti.bnb.network.BnbPackets;
import com.kipti.bnb.registry.core.BnbDataComponents;
import com.simibubi.create.content.trains.track.ITrackBlock;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public record ArticulatedTrackTiltPacket(
        float tiltDegrees,
        int slot
) implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, ArticulatedTrackTiltPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT,
                    ArticulatedTrackTiltPacket::tiltDegrees,
                    ByteBufCodecs.VAR_INT,
                    ArticulatedTrackTiltPacket::slot,
                    ArticulatedTrackTiltPacket::new
            );

    @Override
    public void handle(final ServerPlayer player) {
        if (player.isSpectator() || !ArticulatedTrackUtils.isValidTilt(tiltDegrees)) {
            return;
        }

        if (slot != Inventory.SLOT_OFFHAND && player.getInventory().selected != slot) {
            return;
        }

        final ItemStack heldStack = slot == Inventory.SLOT_OFFHAND
                ? player.getOffhandItem()
                : player.getInventory().getItem(slot);
        if (!isTrackItem(heldStack)) {
            return;
        }

        if (Float.compare(tiltDegrees, 0f) == 0) {
            heldStack.remove(BnbDataComponents.TRACK_TILT);
        } else {
            heldStack.set(BnbDataComponents.TRACK_TILT, tiltDegrees);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return BnbPackets.SET_TRACK_TILT;
    }

    private static boolean isTrackItem(final ItemStack stack) {
        return !stack.isEmpty() && Block.byItem(stack.getItem()) instanceof ITrackBlock;
    }

}
