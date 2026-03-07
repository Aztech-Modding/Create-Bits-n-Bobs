package com.kipti.bnb.registry.compat;

import com.cake.azimuth.registration.CreateBlockEdits;
import com.kipti.bnb.content.dyeable_pipes.DyeablePipeBlockItem;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class BnbCreateBlockEdits {

    public static final BooleanProperty GLOWING = BooleanProperty.create("glowing");

    @CreateBlockEdits.Registrator
    public static void register() {
        CreateBlockEdits.forBlock("belt", builder ->
                builder.properties(p -> p.emissiveRendering((a, b, c) -> a.hasProperty(GLOWING) && a.getValue(GLOWING)))
        );

        CreateBlockEdits.forBlockItem("fluid_pipe", DyeablePipeBlockItem::new);
    }

}

