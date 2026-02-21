package com.kipti.bnb.registry.worldgen;

import com.kipti.bnb.CreateBitsnBobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

public class BnbWorldPresets {

    public static final ResourceKey<WorldPreset> PONDER = ResourceKey.create(Registries.WORLD_PRESET, CreateBitsnBobs.asResource("ponderous_planes"));

}

