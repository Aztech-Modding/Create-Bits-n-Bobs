package com.kipti.bnb.content.decoration.light.headlamp.rendering;

import com.kipti.bnb.content.decoration.light.headlamp.HeadlampBlockEntity;
import com.kipti.bnb.content.decoration.light.headlamp.rendering.pipeline.block_entity.HeadlampBlockEntityRenderer;
import net.minecraft.world.item.DyeColor;

/**
 * Shared constants for headlamp render state encoding, placement layout, and dye color mapping.
 * <p>
 * The render state is packed into a {@code long} by {@link HeadlampBlockEntity#getRenderStateAsLong()}
 * and decoded by {@link HeadlampBlockEntityRenderer}.
 *
 * @see HeadlampBlockEntity#getRenderStateAsLong()
 */
public final class HeadlampConstants {

    /**
     * Number of headlamp placement slots in a single block.
     */
    public static final int PLACEMENT_COUNT = 9;

    /**
     * Number of bits used for the on/off state mask in the packed render state.
     */
    public static final int RENDER_STATE_ON_OFF_BITS = 4;

    /**
     * Number of bits per headlamp slot in the packed render state.
     */
    public static final int RENDER_STATE_SLOT_BITS = 5;

    /**
     * Bit mask for a single slot value (5 bits).
     */
    public static final long SLOT_VALUE_MASK = 0x1FL;

    /**
     * Offset applied to {@link DyeColor#ordinal()} when encoding dye color in placement values.
     * <ul>
     *     <li>0 = no headlamp present</li>
     *     <li>1 = undyed headlamp</li>
     *     <li>2+ = {@code DyeColor.ordinal() + DYE_COLOR_OFFSET}</li>
     * </ul>
     */
    public static final int DYE_COLOR_OFFSET = 2;
}

