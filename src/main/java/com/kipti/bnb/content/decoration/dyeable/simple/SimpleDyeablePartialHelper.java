package com.kipti.bnb.content.decoration.dyeable.simple;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SimpleDyeablePartialHelper {

    public static SuperByteBuffer partial(
        final PartialModel partial,
        final BlockState referenceState,
        @Nullable final DyeColor color,
        final Map<DyeColor, SpriteShiftEntry> spriteShifts
    ) {
        return apply(CachedBuffers.partial(partial, referenceState), color, spriteShifts);
    }

    public static SuperByteBuffer partialFacing(
        final PartialModel partial,
        final BlockState referenceState,
        @Nullable final DyeColor color,
        final Map<DyeColor, SpriteShiftEntry> spriteShifts
    ) {
        return apply(CachedBuffers.partialFacing(partial, referenceState), color, spriteShifts);
    }

    public static SuperByteBuffer apply(
        final SuperByteBuffer buffer,
        @Nullable final DyeColor color,
        final Map<DyeColor, SpriteShiftEntry> spriteShifts
    ) {
        if (color == null) {
            return buffer;
        }

        return apply(buffer, spriteShifts.get(color));
    }

    public static SuperByteBuffer apply(
        final SuperByteBuffer buffer,
        @Nullable final SpriteShiftEntry spriteShift
    ) {
        if (spriteShift == null) {
            return buffer;
        }

        return buffer.shiftUV(spriteShift);
    }

}
