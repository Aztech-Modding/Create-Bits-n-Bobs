package com.kipti.bnb.registry.azimuth;

import com.cake.azimuth.registration.event.RegisterCreateBlockEditsEvent;
import com.kipti.bnb.content.decoration.dyeable.simple.SimpleDyeableModelWrapper;
import com.kipti.bnb.registry.client.BnbSpriteShifts;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;

final class BnbCreateBlockEditsClient {

    private BnbCreateBlockEditsClient() {
    }

    static void register(final RegisterCreateBlockEditsEvent event) {
        event.forBlock(
                "steam_engine",
                builder -> ((BlockBuilder<SteamEngineBlock, CreateRegistrate>) builder).onRegister(CreateRegistrate.blockModel(
                        () -> model -> new SimpleDyeableModelWrapper(model, BnbSpriteShifts.DYED_STEAM_ENGINE)))
        );
    }

}
