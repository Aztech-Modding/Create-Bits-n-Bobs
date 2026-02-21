package com.kipti.bnb.content.decoration.light.headlamp;

import net.neoforged.neoforge.common.util.TriState;
import org.joml.Vector2i;

public class CCLightAddressing {
    private volatile byte mask = 1 + 2 + 4 + 8;

    /**
     * Mask is the 4 bits that determine which of the 4 lights (by section) are being rendered. The bits are as follows:
     */
    public byte getMask() {
        return mask;
    }

    public void setMask(final byte mask) {
        this.mask = mask;
    }

    public void setLocalMaskValue(final Vector2i localMaskCoordinate, final boolean value) {
        if (value) {
            mask = (byte) (mask | (1 << (localMaskCoordinate.x() + localMaskCoordinate.y() * 2)));
        } else {
            mask = (byte) (mask & ~(1 << (localMaskCoordinate.x() + localMaskCoordinate.y() * 2)));
        }
    }

    public record View(byte mask) {
        public TriState getCCAddressingForIndex(HeadlampBlockEntity.HeadlampPlacement i) {
            final Vector2i localMaskCoordinate = getLocalMaskCoordinateForPlacement(i);
            final boolean maskValue = getMaskValue(mask, localMaskCoordinate);
            return maskValue ? TriState.TRUE : TriState.FALSE;
        }
    }

    public static Vector2i getLocalMaskCoordinateForPlacement(final HeadlampBlockEntity.HeadlampPlacement placement) {
        final int x = placement.horizontalAlignment() == HeadlampBlockEntity.HeadlampAlignment.RIGHT_OR_BOTTOM ? 1 : 0;
        final int y = placement.verticalAlignment() == HeadlampBlockEntity.HeadlampAlignment.RIGHT_OR_BOTTOM ? 1 : 0;
        return new Vector2i(x, y);
    }

    public boolean getMaskValue(final Vector2i localMaskCoordinate) {
        return (mask & (1 << (localMaskCoordinate.x() + localMaskCoordinate.y() * 2))) != 0;
    }

    public static boolean getMaskValue(final byte mask, final Vector2i localMaskCoordinate) {
        return (mask & (1 << (localMaskCoordinate.x() + localMaskCoordinate.y() * 2))) != 0;
    }

}

