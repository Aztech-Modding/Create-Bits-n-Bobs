package com.kipti.bnb.mixin.articulate;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SmartBlockEntity.class)
public interface SmartBlockEntityAccessorMixin {

    @Accessor("initialized")
    void articulate$setInitialized(boolean initialized);

}
