package com.kipti.bnb.content.trinkets.nixie.foundation;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.function.Consumer;

public enum ConfigurableDisplayOptionTransform {
    NONE(1, 1, 0.9f),
    ALWAYS_UP(1, 1, 0.9f),
    DOUBLE_CHAR(2, 1, 0.5f),
    DOUBLE_CHAR_DOUBLE_LINES(3, 2, 0.4f);

    public final int width;
    public final int lines;
    public final float scale;

    ConfigurableDisplayOptionTransform(final int width, final int lines, final float scale) {
        this.width = width;
        this.lines = lines;
        this.scale = scale;
    }

    public void transform(final PoseStack stack, final int x, final int y) {
        final int totalSize = 16;
        final float xStep = totalSize / (float) (width);
        final float yStep = totalSize / (float) (lines);
        final float xOffset = xStep / 2f + x * xStep;
        final float yOffset = yStep / 2f + y * yStep;

        stack.translate(xOffset - 8f, yOffset - 8f + lines * 2f, 0);
        stack.scale(scale, scale, 1f);
    }

    public void render(final PoseStack stack, final GenericNixieDisplayBlockEntity be, final Consumer<Character> consumer) {
        stack.pushPose();
        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < width; j++) {
                stack.pushPose();
                transform(stack, j, i);
                final char glyph = be.getRenderedText(i, j);
                consumer.accept(glyph);
                stack.popPose();
            }
        }
        stack.popPose();
    }

    public boolean isMoreThanOneCharacter() {
        return this.width > 1 || this.lines > 1;
    }
}

