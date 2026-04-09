package com.kipti.bnb.mixin;

import com.kipti.bnb.foundation.ponder.create.BnbCreatePonderScenes;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderScenes;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AllCreatePonderScenes.class)
public class AllCreatePonderScenesMixin {

    @Inject(method = "register", at = @At("TAIL"))
    private static void register(final PonderSceneRegistrationHelper<ResourceLocation> helper, final CallbackInfo ci) {
        BnbCreatePonderScenes.register(helper);
    }

}

