package com.kipti.bnb.mixin.presets;

import com.kipti.bnb.foundation.generation.PonderflatGeneratorSettings;
import com.kipti.bnb.foundation.generation.PonderflatLevelSource;
import com.kipti.bnb.registry.BnbWorldPresets;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.level.levelgen.presets.WorldPresets$Bootstrap")
public abstract class WorldPresetsBootstrapMixin {

    @Shadow
    protected abstract LevelStem makeOverworld(ChunkGenerator generator);

    @Shadow
    protected abstract void registerCustomOverworldPreset(ResourceKey<WorldPreset> dimensionKey, LevelStem levelStem);

    @Shadow
    @Final
    private HolderGetter<Biome> biomes;

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private void onBootstrap(final CallbackInfo ci) {
        final Holder.Reference<Biome> voidBiome = biomes.getOrThrow(Biomes.THE_VOID);
        this.registerCustomOverworldPreset(BnbWorldPresets.PONDER, this.makeOverworld(new PonderflatLevelSource(voidBiome, new PonderflatGeneratorSettings())));
    }

}
