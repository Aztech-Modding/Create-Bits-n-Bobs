package com.kipti.bnb.content.kinetics.cogwheel_chain.block;

import com.simibubi.create.api.registry.SimpleRegistry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.world.level.block.Block;

public class GenericBlockEntityRenderModels {
    public static final SimpleRegistry<Block, PartialModel> REGISTRY = SimpleRegistry.create();

    public static <B extends Block> NonNullConsumer<? super B> model(PartialModel model) {
        return b -> REGISTRY.register(b, model);
    }
}
