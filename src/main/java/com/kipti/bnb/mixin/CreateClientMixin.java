package com.kipti.bnb.mixin;

import com.kipti.bnb.CreateBitsnBobsClient;
import com.simibubi.create.CreateClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateClient.class)
public class CreateClientMixin {

    @Inject(method = "invalidateRenderers", at = @At("HEAD"))
    private static void onInvalidateRenderers(final CallbackInfo ci) {
        CreateBitsnBobsClient.invalidateRenderers();
    }

}
