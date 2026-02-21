package com.kipti.bnb.mixin.presets;

import com.kipti.bnb.foundation.generation.PonderflatLevelSource;
import com.kipti.bnb.registry.worldgen.BnbWorldPresets;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(WorldPresets.class)
public class WorldPresetsMixin {

    @Inject(method = "lambda$fromSettings$0", at = @At("RETURN"), cancellable = true)
    private static void bnb$fromSettings$0(final LevelStem p_344665_, final CallbackInfoReturnable<Optional> cir) {
        if (cir.getReturnValue().isEmpty()) {
            if (p_344665_.generator() instanceof PonderflatLevelSource) {
                cir.setReturnValue(Optional.of(BnbWorldPresets.PONDER));
            }
        }
    }

}

