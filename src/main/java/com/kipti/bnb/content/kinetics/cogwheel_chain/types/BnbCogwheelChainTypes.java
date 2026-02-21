package com.kipti.bnb.content.kinetics.cogwheel_chain.types;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.registry.content.BnbBlocksBootstrap;
import com.kipti.bnb.registry.core.BnbRegistries;
import com.simibubi.create.AllItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.level.block.Blocks;

public class BnbCogwheelChainTypes {

    public static final DeferredRegister<CogwheelChainType> REGISTRY = DeferredRegister.create(BnbRegistries.COGWHEEL_CHAIN_TYPES, CreateBitsnBobs.MOD_ID);

    public static final DeferredHolder<CogwheelChainType, CogwheelChainType> CHAIN = REGISTRY
            .register("chain", () -> new CogwheelChainType.Builder()
                    .breakEffectsBlock(() -> Blocks.CHAIN)
                    .build());

    public static final DeferredHolder<CogwheelChainType, CogwheelChainType> BELT_CHAIN = REGISTRY
            .register("belt", () -> new CogwheelChainType.Builder()
                    .relatedItem(AllItems.BELT_CONNECTOR::get)
                    .renderType(CogwheelChainType.ChainRenderInfo.BELT)
                    .renderTexture(CreateBitsnBobs.asResource("textures/block/chain_belt.png"))
                    .breakEffectsBlock(() -> Blocks.CHAIN)
                    .setCogwheelPredicate((block) -> BnbBlocksBootstrap.LARGE_EMPTY_FLANGED_COGWHEEL.get() == block || BnbBlocksBootstrap.SMALL_EMPTY_FLANGED_COGWHEEL.get() == block) //TODO: tag
                    .permitsAxisChange(false)
                    .build());

    public static final DeferredHolder<CogwheelChainType, CogwheelChainType> ROPE_CHAIN = REGISTRY
            .register("rope", () -> new CogwheelChainType.Builder()
                    .relatedTag(Tags.Items.ROPES)
                    .renderType(CogwheelChainType.ChainRenderInfo.ROPE)
                    .renderTexture(CreateBitsnBobs.asResource("textures/block/chain_rope.png"))
                    .breakEffectsBlock(() -> Blocks.CHAIN)
                    .setCogwheelPredicate((block) -> BnbBlocksBootstrap.LARGE_EMPTY_FLANGED_COGWHEEL.get() == block || BnbBlocksBootstrap.SMALL_EMPTY_FLANGED_COGWHEEL.get() == block) //TODO: tag
                    .build());


    public static void register(final IEventBus bus) {
        REGISTRY.register(bus);
    }

}

