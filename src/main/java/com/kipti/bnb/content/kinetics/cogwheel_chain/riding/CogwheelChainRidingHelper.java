package com.kipti.bnb.content.kinetics.cogwheel_chain.riding;

import com.kipti.bnb.content.kinetics.cogwheel_chain.attachment.CogwheelChainAttachment;
import com.kipti.bnb.network.packets.from_client.CogwheelChainRidingPacket;
import com.simibubi.create.AllTags.AllItemTags;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

/**
 * Client-side handler that simulates player movement along a cogwheel chain,
 * emulating the chain conveyor riding behavior from base Create.
 *
 * <p>All state is static and only meaningful on the logical client.
 * Call {@link #clientTick(LocalPlayer)} from client tick events.</p>
 */
public class CogwheelChainRidingHelper {

    private static CogwheelChainAttachment currentAttachment;
    private static boolean isRiding;
    private static int catchingUp;

    private static final double MAX_DRIFT_DISTANCE = 3.0;
    private static final double MAX_DROP_DISTANCE = 1.0;
    private static final int CATCH_UP_TICKS = 20;

    /**
     * Begins riding the given chain attachment. Sets up tracking state,
     * displays the dismount hint, and plays a mounting sound.
     */
    public static void embark(final CogwheelChainAttachment attachment) {
        currentAttachment = attachment;
        isRiding = true;
        catchingUp = CATCH_UP_TICKS;

        final Minecraft mc = Minecraft.getInstance();
        final Component hint = Component.translatable(
                "mount.onboard", mc.options.keyShift.getTranslatedKeyMessage());
        mc.gui.setOverlayMessage(hint, false);
        mc.getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.CHAIN_HIT, 1f, 0.5f));
    }

    /**
     * Stops riding and clears all tracking state.
     */
    public static void disembark() {
        if (currentAttachment != null) {
            CatnipServices.NETWORK.sendToServer(
                    new CogwheelChainRidingPacket(currentAttachment.getControllerPos(), true));
        }
        currentAttachment = null;
        isRiding = false;
        catchingUp = 0;

        Minecraft.getInstance()
                .getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.CHAIN_HIT, 0.75f, 0.35f));
    }

    /**
     * Per-tick update called from a client tick event. Advances the attachment
     * along the chain, moves the player to follow, and checks disembark conditions.
     */
    public static void clientTick(final LocalPlayer player) {
        if (!isRiding || currentAttachment == null) return;

        final Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused()) return;

        if (!player.isHolding(AllItemTags.CHAIN_RIDEABLE::matches)) {
            disembark();
            return;
        }

        if (player.isShiftKeyDown()) {
            disembark();
            return;
        }

        if (!currentAttachment.isValid(mc.level)) {
            disembark();
            return;
        }

        currentAttachment.tick(mc.level);

        final Vec3 targetPosition = currentAttachment.getCurrentPosition(mc.level);
        if (targetPosition.equals(Vec3.ZERO)) {
            disembark();
            return;
        }

        applyMovement(player, targetPosition);

        if (AnimationTickHolder.getTicks() % 10 == 0) {
            CatnipServices.NETWORK.sendToServer(
                    new CogwheelChainRidingPacket(currentAttachment.getControllerPos(), false));
        }
    }

    /**
     * Returns {@code true} while the player is riding a chain,
     * signalling that normal movement input should be suppressed.
     */
    public static boolean shouldPreventMovement() {
        return isRiding;
    }

    public static boolean isRiding() {
        return isRiding;
    }

    private static void applyMovement(final LocalPlayer player, final Vec3 targetPosition) {
        final Vec3 playerHangPosition = computeHangPosition(player);
        final Vec3 diff = targetPosition.subtract(playerHangPosition);

        if (catchingUp > 0) {
            catchingUp--;
        }

        if (catchingUp == 0 && isOutOfRange(diff)) {
            disembark();
            return;
        }

        player.setDeltaMovement(player.getDeltaMovement()
                                        .scale(0.75)
                                        .add(diff.scale(0.25)));
    }

    private static Vec3 computeHangPosition(final LocalPlayer player) {
        final double chainYOffset = 0.5 * player.getScale();
        return player.position()
                .add(0, player.getBoundingBox().getYsize() + chainYOffset, 0);
    }

    private static boolean isOutOfRange(final Vec3 diff) {
        return diff.length() > MAX_DRIFT_DISTANCE || diff.y < -MAX_DROP_DISTANCE;
    }
}
