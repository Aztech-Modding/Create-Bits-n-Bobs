package com.kipti.bnb.registry.compat;

import com.cake.azimuth.registration.CreateBlockEdits;
import com.kipti.bnb.content.decoration.dyeable.pipes.DyeablePipeBlockItem;
import com.kipti.bnb.content.decoration.dyeable.simple.SimpleDyeableBlockItem;
import com.kipti.bnb.content.decoration.dyeable.simple.SteamEngineModel;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class BnbCreateBlockEdits {

    public static final BooleanProperty GLOWING = BooleanProperty.create("glowing");

    @SuppressWarnings("unchecked")
    @CreateBlockEdits.Registrator
    public static void register() {
        CreateBlockEdits.forBlock(
                "belt", builder ->
                        builder.properties(p -> p.emissiveRendering((a, b, c) -> a.hasProperty(GLOWING) && a.getValue(
                                GLOWING)))
        );

        CreateBlockEdits.forBlockItem("fluid_pipe", DyeablePipeBlockItem::new);
        CreateBlockEdits.forBlockItem("mechanical_pump", SimpleDyeableBlockItem::new);
        CreateBlockEdits.forBlockItem("smart_fluid_pipe", SimpleDyeableBlockItem::new);
        CreateBlockEdits.forBlockItem("fluid_valve", SimpleDyeableBlockItem::new);
        CreateBlockEdits.forBlockItem("steam_engine", SimpleDyeableBlockItem::new);

        CreateBlockEdits.forBlock(
                "steam_engine",
                builder -> ((BlockBuilder<SteamEngineBlock, CreateRegistrate>) builder).onRegister(CreateRegistrate.blockModel(
                        () -> SteamEngineModel::new))
        );
    }

}

