package com.kipti.bnb.foundation.behaviour.drag;

import com.kipti.bnb.network.packets.from_client.DragInteractionUpdatePacket;
import com.mojang.blaze3d.platform.Window;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.lwjgl.glfw.GLFW;

/**
 * Transparent overlay that captures mouse Y movement while RMB is held to drive a {@link DragInteractionBehaviour}.
 */
public class DragInteractionScreen extends Screen {

    private final BlockPos targetPos;
    private final int initialValue;
    private final int min;
    private final int max;

    private double lastMouseY;
    private double accumulatedDelta;
    private int currentValue;

    public DragInteractionScreen(final BlockPos blockPos, final int initialValue, final int min, final int max) {
        super(Component.empty());
        this.targetPos = blockPos;
        this.initialValue = initialValue;
        this.min = min;
        this.max = max;
        this.lastMouseY = -1;
        this.accumulatedDelta = 0;
        this.currentValue = initialValue;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
                                 final float partialTick) {
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        final double dragZone = this.height;
        final double sensitivity = (dragZone > 0) ? (this.max - this.min) / dragZone : 0;

        if (this.lastMouseY >= 0) {
            final double delta = this.lastMouseY - mouseY;
            this.accumulatedDelta += delta * sensitivity;

            this.accumulatedDelta = Mth.clamp(
                    this.initialValue + this.accumulatedDelta, this.min, this.max
            ) - this.initialValue;

            final int newValue = Mth.clamp(
                    this.initialValue + (int) Math.round(this.accumulatedDelta), this.min, this.max
            );
            if (newValue != this.currentValue) {
                this.currentValue = newValue;
                this.sendValueUpdate();
            }
        }
        final Window window = this.minecraft.getWindow();
        this.lastMouseY = window.getGuiScaledHeight() / 2.0;
        GLFW.glfwSetCursorPos(
                window.getWindow(),
                window.getScreenWidth() / 2.0, window.getScreenHeight() / 2.0
        );
        GLFW.glfwSetInputMode(window.getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);

    }

    @Override
    public void tick() {
        super.tick();
        if (GLFW.glfwGetMouseButton(
                this.minecraft.getWindow().getWindow(),
                GLFW.GLFW_MOUSE_BUTTON_RIGHT
        ) != GLFW.GLFW_PRESS) {
            this.onClose();
            return;
        }

        if (this.minecraft.player != null
                && this.minecraft.player.blockPosition().distSqr(this.targetPos) > 36) {
            this.onClose();
        }
    }

    private void sendValueUpdate() {
        CatnipServices.NETWORK.sendToServer(new DragInteractionUpdatePacket(this.targetPos, this.currentValue));

        if (this.minecraft == null || this.minecraft.level == null) {
            return;
        }
        final BlockEntity be = this.minecraft.level.getBlockEntity(this.targetPos);
        if (be instanceof final SmartBlockEntity sbe) {
            final DragInteractionBehaviour behaviour = sbe.getBehaviour(DragInteractionBehaviour.TYPE);
            if (behaviour != null) {
                behaviour.updateTargetValue(this.currentValue);
            }
        }
    }
}
