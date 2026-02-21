package com.kipti.bnb.mixin.presets;

import com.kipti.bnb.foundation.generation.PonderflatLevelSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldDimensions.class)
public class WorldDimensionsMixin {

    @Inject(method = "lambda$specialWorldProperty$2", at = @At("RETURN"), cancellable = true)
    private static void modifyPonderWorldFlatProperty(final LevelStem p_251481_, final CallbackInfoReturnable<PrimaryLevelData.SpecialWorldProperty> cir) {
        final ChunkGenerator chunkgenerator = p_251481_.generator();
        if (chunkgenerator instanceof PonderflatLevelSource) {
            cir.setReturnValue(PrimaryLevelData.SpecialWorldProperty.FLAT);
        }
    }

}
