package com.kipti.bnb.mixin.azimuth;

import com.cake.azimuth.advancement.CreateAdvancementIdAccessor;
import com.simibubi.create.foundation.advancement.CreateAdvancement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = CreateAdvancement.class, remap = false)
public abstract class CreateAdvancementIdMixin implements CreateAdvancementIdAccessor {

    @Shadow
    private String id;

    @Override
    public String azimuth$getId() {
        return id;
    }
}
