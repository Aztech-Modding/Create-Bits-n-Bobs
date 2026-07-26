package com.kipti.bnb.content.trinkets.light.headlamp.rendering.pipeline.visual;

import com.simibubi.create.content.kinetics.base.RotatingInstance;
import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import net.createmod.catnip.render.SpriteShiftEntry;

import javax.annotation.Nullable;

/**
 * Adapted version of the {@link RotatingInstance}, with the sprite shift
 * behaviour of {@link ShiftTransformedInstance} added. This is used to shift the uv on the instance one time and that's
 * it. Assumes uniform scale and no offset needed.
 */
public class ShiftRotatingInstance extends RotatingInstance {

    public float diffU;
    public float diffV;

    public ShiftRotatingInstance(final InstanceType<? extends RotatingInstance> type, final InstanceHandle handle) {
        super(type, handle);
    }

    public ShiftRotatingInstance setSpriteShift(final @Nullable SpriteShiftEntry spriteShift) {
        if (spriteShift == null) {
            this.diffU = 0;
            this.diffV = 0;
            return this;
        }
        this.diffU = spriteShift.getTarget().getU0() - spriteShift.getOriginal().getU0();
        this.diffV = spriteShift.getTarget().getV0() - spriteShift.getOriginal().getV0();
        return this;
    }

}
