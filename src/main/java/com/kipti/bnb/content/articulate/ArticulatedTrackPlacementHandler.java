package com.kipti.bnb.content.articulate;

import com.kipti.bnb.mixin.articulate.TrackPlacementInfoAccessorMixin;
import com.kipti.bnb.network.packets.from_client.ArticulatedTrackTiltPacket;
import com.kipti.bnb.registry.core.BnbDataComponents;
import com.simibubi.create.content.trains.track.ITrackBlock;
import com.simibubi.create.content.trains.track.TrackPlacement;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public final class ArticulatedTrackPlacementHandler {

    private static final String TRACK_TILT_ACTIONBAR_KEY = "tooltip.bits_n_bobs.articulate.track_tilt";
    private static float currentTiltSelection = 0f;
    private static boolean isSelectingTilt = false;
    @Nullable
    private static InteractionHand currentTrackHand;

    private ArticulatedTrackPlacementHandler() {
    }

    public static boolean onScrolled(final double delta) {
        if (Double.compare(delta, 0d) == 0) {
            return false;
        }

        final Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) {
            return false;
        }

        final LocalPlayer player = mc.player;
        if (player == null || player.isSpectator()) {
            return false;
        }

        if (!Screen.hasControlDown()) {
            resetSelection();
            return false;
        }

        final InteractionHand trackHand = getHeldTrackHand(player);
        if (trackHand == null) {
            resetSelection();
            return false;
        }

        final ItemStack heldTrackStack = player.getItemInHand(trackHand);
        if (heldTrackStack == null) {
            resetSelection();
            return false;
        }

        final boolean suppressTiltActionbar = shouldSuppressTiltActionbar();
        isSelectingTilt = true;
        currentTiltSelection = ArticulatedTrackUtils.nextTilt(getSelectedTilt(heldTrackStack), delta > 0d);
        updateItemComponent(player, trackHand, heldTrackStack, currentTiltSelection);
        invalidatePlacementPreview();
        if (!suppressTiltActionbar) {
            displayTiltActionbar(player, currentTiltSelection);
        }
        return true;
    }

    public static void tick() {
        if (!isSelectingTilt) {
            return;
        }

        final Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) {
            resetSelection();
            return;
        }

        final LocalPlayer player = mc.player;
        if (player == null || player.isSpectator() || !Screen.hasControlDown()) {
            resetSelection();
            return;
        }

        final InteractionHand trackHand = getHeldTrackHand(player);
        if (trackHand == null) {
            resetSelection();
            return;
        }

        final ItemStack heldTrackStack = player.getItemInHand(trackHand);
        currentTiltSelection = getSelectedTilt(heldTrackStack);
    }

    public static void resetSelection() {
        currentTiltSelection = 0f;
        isSelectingTilt = false;
        currentTrackHand = null;
    }

    @Nullable
    public static ItemStack getHeldTrackStack(final LocalPlayer player) {
        final InteractionHand hand = getHeldTrackHand(player);
        return hand == null ? null : player.getItemInHand(hand);
    }

    @Nullable
    public static InteractionHand getHeldTrackHand(final LocalPlayer player) {
        final InteractionHand swingingHand = player.swingingArm;
        if (swingingHand != null && isTrackItem(player.getItemInHand(swingingHand))) {
            currentTrackHand = swingingHand;
            return swingingHand;
        }

        if (currentTrackHand != null && isTrackItem(player.getItemInHand(currentTrackHand))) {
            return currentTrackHand;
        }

        final ItemStack mainHand = player.getMainHandItem();
        if (isTrackItem(mainHand)) {
            currentTrackHand = InteractionHand.MAIN_HAND;
            return InteractionHand.MAIN_HAND;
        }

        final ItemStack offHand = player.getOffhandItem();
        if (isTrackItem(offHand)) {
            currentTrackHand = InteractionHand.OFF_HAND;
            return InteractionHand.OFF_HAND;
        }

        currentTrackHand = null;
        return null;
    }

    private static void updateItemComponent(final LocalPlayer player, final InteractionHand hand, final ItemStack stack, final float tiltDegrees) {
        if (Float.compare(tiltDegrees, 0f) == 0) {
            stack.remove(BnbDataComponents.TRACK_TILT);
        } else {
            stack.set(BnbDataComponents.TRACK_TILT, tiltDegrees);
        }

        CatnipServices.NETWORK.sendToServer(new ArticulatedTrackTiltPacket(tiltDegrees, getHeldTrackSlot(player, hand)));
    }

    private static void displayTiltActionbar(final LocalPlayer player, final float tiltDegrees) {
        player.displayClientMessage(Component.translatable(TRACK_TILT_ACTIONBAR_KEY, formatTiltDegrees(tiltDegrees)), true);
    }

    private static boolean shouldSuppressTiltActionbar() {
        final TrackPlacement.PlacementInfo cached = TrackPlacement.cached;
        if (cached == null) {
            return false;
        }

        final TrackPlacementInfoAccessorMixin access = (TrackPlacementInfoAccessorMixin) cached;
        return access.articulate$isValid() || access.articulate$getMessage() != null;
    }

    private static void invalidatePlacementPreview() {
        TrackPlacement.cached = null;
    }

    private static float getSelectedTilt(final ItemStack stack) {
        final float tiltDegrees = stack.getOrDefault(BnbDataComponents.TRACK_TILT, 0f);
        return ArticulatedTrackUtils.isValidTilt(tiltDegrees)
                ? tiltDegrees
                : ArticulatedTrackUtils.snapToNearest(tiltDegrees);
    }

    private static String formatTiltDegrees(final float tiltDegrees) {
        final String formatted = Float.toString(tiltDegrees);
        return formatted.endsWith(".0") ? formatted.substring(0, formatted.length() - 2) : formatted;
    }

    private static int getHeldTrackSlot(final LocalPlayer player, final InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return player.getInventory().selected;
        }

        return Inventory.SLOT_OFFHAND;
    }

    private static boolean isTrackItem(final ItemStack stack) {
        return !stack.isEmpty() && Block.byItem(stack.getItem()) instanceof ITrackBlock;
    }

}
