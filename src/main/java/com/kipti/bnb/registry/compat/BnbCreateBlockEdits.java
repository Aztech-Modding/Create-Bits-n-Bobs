package com.kipti.bnb.registry.compat;

import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BnbCreateBlockEdits {

    //Static components

    public static final BooleanProperty GLOWING = BooleanProperty.create("glowing");

    //

    private static final Map<String, Consumer<BlockBuilder<?, CreateRegistrate>>> EDITS_BY_ID = new HashMap<>();

    private static <T extends Block> void forBlock(final String location, final Class<T> blockClass, final Consumer<BlockBuilder<?, CreateRegistrate>> edit) {
        EDITS_BY_ID.put(location, edit);
    }

    static {
        forBlock("belt", BeltBlock.class, builder ->
                builder.properties(p -> p.emissiveRendering((a, b, c) -> a.hasProperty(GLOWING) && a.getValue(GLOWING)))
        );
    }

    public static Consumer<BlockBuilder<?, CreateRegistrate>> getEditForId(final String id) {
        return EDITS_BY_ID.get(id);
    }

}

