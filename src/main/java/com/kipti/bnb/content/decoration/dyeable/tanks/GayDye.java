package com.kipti.bnb.content.decoration.dyeable.tanks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;

/**
 * be whooo you arreeee for yourrr priddeeee
 */
public class GayDye {

    private static DyeColor[] reverse(final DyeColor[] colors) {
        final DyeColor[] reversed = new DyeColor[colors.length];
        for (int i = 0; i < colors.length; i++) {
            reversed[i] = colors[colors.length - 1 - i];
        }
        return reversed;
    }

    private static final DyeColor[] GAY =
            reverse(new DyeColor[]{
                    DyeColor.RED,
                    DyeColor.ORANGE,
                    DyeColor.YELLOW,
                    DyeColor.GREEN,
                    DyeColor.BLUE,
                    DyeColor.PURPLE,
            });
    private static final DyeColor[] TRANS_EVEN =
            reverse(new DyeColor[]{
                    DyeColor.WHITE,
                    DyeColor.LIGHT_BLUE,
                    DyeColor.PINK,
                    DyeColor.WHITE,
                    DyeColor.LIGHT_BLUE,
                    DyeColor.PINK,
            });
    private static final DyeColor[] MAYBE_SO_MUCH_AS_BISEXUAL =
            reverse(new DyeColor[]{
                    DyeColor.PURPLE,
                    DyeColor.PINK,
                    DyeColor.PINK,
                    DyeColor.PURPLE,
                    DyeColor.BLUE,
                    DyeColor.BLUE,
            });

    private int animationTick = 0;
    private final AnimationType animationType;
    private final PrideType prideType;

    public GayDye(final AnimationType animationType, final PrideType prideType) {
        this.animationType = animationType;
        this.prideType = prideType;
    }

    public void tick() {
        this.animationTick++;
    }

    public void write(final CompoundTag tag) {
        tag.putInt("GayDyeAnimationTick", this.animationTick);
        tag.putInt("GayDyeAnimationType", this.animationType.ordinal());
        tag.putInt("GayDyePrideType", this.prideType.ordinal());
    }

    public static GayDye read(final CompoundTag tag) {
        final int animationTick = tag.getInt("GayDyeAnimationTick");
        final int animationTypeOrdinal = tag.getInt("GayDyeAnimationType");
        final int prideTypeOrdinal = tag.getInt("GayDyePrideType");
        final GayDye gay = new GayDye(
                animationTypeOrdinal >= 0 && animationTypeOrdinal < AnimationType.values().length ?
                        AnimationType.values()[animationTypeOrdinal] : AnimationType.STATIC,
                prideTypeOrdinal >= 0 && prideTypeOrdinal < PrideType.values().length ?
                        PrideType.values()[prideTypeOrdinal] : PrideType.GAY
        );
        gay.animationTick = animationTick;
        return gay;
    }

    public DyeColor getDisplayedColor(final int localTankY) {
        if (this.animationType == AnimationType.SCROLLING) {
            return this.prideType.getColorWrapped(localTankY + Math.floorDiv(this.animationTick, 200));
        } else {
            return this.prideType.getColorWrapped(localTankY);
        }
    }

    public boolean needsTicking() {
        return this.animationType == AnimationType.SCROLLING;
    }

    public enum AnimationType {
        STATIC,
        SCROLLING
    }

    public enum PrideType {
        GAY(GayDye.GAY),
        TRANS(GayDye.TRANS_EVEN),
        BISEXUAL(GayDye.MAYBE_SO_MUCH_AS_BISEXUAL);

        private final DyeColor[] colors;

        PrideType(final DyeColor[] colors) {
            this.colors = colors;
        }

        public DyeColor getColorWrapped(final int i) {
            final int index = ((i % this.colors.length) + this.colors.length) % this.colors.length;
            return this.colors[index];
        }
    }
}
