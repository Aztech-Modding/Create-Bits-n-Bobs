package com.kipti.bnb.content.decoration.light.headlamp.rendering.pipeline.visual;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import net.createmod.catnip.render.SpriteShiftEntry;

/**
 * Adapted version of the {@link com.simibubi.create.content.processing.burner.ScrollTransformedInstance}, with the scrolling behaviour removed.
 * This is used to shift the uv on the instance one time and that's it.
 * Assumes uniform scale and no offset needed.
 */
public class ShiftTransformedInstance extends TransformedInstance {

    public float diffU;
    public float diffV;

    public ShiftTransformedInstance(final InstanceType<? extends TransformedInstance> type, final InstanceHandle handle) {
        super(type, handle);
    }

    public ShiftTransformedInstance setSpriteShift(final SpriteShiftEntry spriteShift) {
        if (spriteShift == null) {
            diffU = 0;
            diffV = 0;
            return this;
        }
        diffU = spriteShift.getTarget().getU0() - spriteShift.getOriginal().getU0();
        diffV = spriteShift.getTarget().getV0() - spriteShift.getOriginal().getV0();
        return this;
    }

}

