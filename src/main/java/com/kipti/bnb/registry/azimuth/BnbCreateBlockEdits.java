package com.kipti.bnb.registry.azimuth;

import com.cake.azimuth.foundation.preconstruct.AzPreConstructEventListener;
import com.cake.azimuth.registration.event.RegisterCreateBlockEditsEvent;
import com.kipti.bnb.content.decoration.dyeable.pipes.DyeablePipeBlockItem;
import com.kipti.bnb.content.decoration.dyeable.simple.SimpleDyeableBlockItem;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

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

        if (FMLEnvironment.dist == Dist.CLIENT) {
            BnbCreateBlockEditsClient.register(event);
        }
    }

}

