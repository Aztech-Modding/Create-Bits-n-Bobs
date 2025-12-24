package com.kipti.bnb.mixin;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.registry.BnbPaletteStoneTypes;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AllPaletteStoneTypes.class)
public class AllPaletteStoneTypesMixin {

    @Inject(method = "register", at = @org.spongepowered.asm.mixin.injection.At("TAIL"))
    private static void bnb$registerAdditionalStoneTypes(final CallbackInfo ci) {
        BnbPaletteStoneTypes.register(CreateBitsnBobs.REGISTRATE);
    }


}
