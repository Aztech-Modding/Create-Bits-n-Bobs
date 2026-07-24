package com.kipti.bnb.registry.azimuth;

import com.cake.azimuth.foundation.preconstruct.AzPreConstructEventListener;
import com.cake.azimuth.registration.event.RegisterCreateBlockEditsEvent;
import com.kipti.bnb.content.decoration.dyeable.pipes.DyeablePipeBlockItem;
import com.kipti.bnb.content.decoration.dyeable.simple.SimpleDyeableBlockItem;
import com.kipti.bnb.content.decoration.dyeable.simple.SimpleDyeableModelWrapper;
import com.kipti.bnb.registry.client.BnbSpriteShifts;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class BnbCreateBlockEdits {

    public static final BooleanProperty GLOWING = BooleanProperty.create("glowing");

    @AzPreConstructEventListener
    public static void register(final RegisterCreateBlockEditsEvent event) {
        event.forBlock(
            "belt", builder ->
                builder.properties(p -> p.emissiveRendering((a, b, c) -> a.hasProperty(GLOWING) && a.getValue(
                    GLOWING)))
        );

        event.forBlockItem("fluid_pipe", DyeablePipeBlockItem::new);
        event.forBlockItem("mechanical_pump", SimpleDyeableBlockItem::new);
        event.forBlockItem("smart_fluid_pipe", SimpleDyeableBlockItem::new);
        event.forBlockItem("fluid_valve", SimpleDyeableBlockItem::new);
        event.forBlockItem("steam_engine", SimpleDyeableBlockItem::new);

        event.forBlock(
            "steam_engine",
            builder -> ((BlockBuilder<SteamEngineBlock, CreateRegistrate>) builder).onRegister(CreateRegistrate.blockModel(
                () -> (m) -> new SimpleDyeableModelWrapper(m, BnbSpriteShifts.DYED_STEAM_ENGINE)))
        );
    }

}

