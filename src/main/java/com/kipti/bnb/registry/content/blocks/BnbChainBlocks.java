package com.kipti.bnb.registry.content.blocks;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.kinetics.cogwheel_chain.block.CogwheelChainBlock;
import com.kipti.bnb.content.kinetics.cogwheel_chain.block.ConnectingCogwheelChainBlock;
import com.kipti.bnb.content.kinetics.cogwheel_chain.block.GenericBlockEntityRenderModels;
import com.kipti.bnb.registry.client.BnbPartialModels;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import static com.kipti.bnb.CreateBitsnBobs.REGISTRATE;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;

public class BnbChainBlocks {
    public static final BlockEntry<ConnectingCogwheelChainBlock> SMALL_COGWHEEL_CHAIN = REGISTRATE.block("small_cogwheel_chain", ConnectingCogwheelChainBlock::small)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.sound(SoundType.WOOD)
                    .mapColor(MapColor.DIRT))
            .transform(axeOrPickaxe())
            .blockstate((c, p) ->
                    BlockStateGen.axisBlock(c, p, (s) -> p.models().getExistingFile(CreateBitsnBobs.asResource("block/sprocket/small_cogwheel"))))
            .loot((lt, block) -> lt.dropOther(block, AllBlocks.COGWHEEL.get()))
            .onRegister(GenericBlockEntityRenderModels.model(BnbPartialModels.SMALL_SPROCKET_COGWHEEL_BLOCK))
            .register();
    public static final BlockEntry<ConnectingCogwheelChainBlock> LARGE_COGWHEEL_CHAIN = REGISTRATE.block("large_cogwheel_chain", ConnectingCogwheelChainBlock::large)
            .initialProperties(SharedProperties::wooden)
            .properties(p -> p.sound(SoundType.WOOD)
                    .mapColor(MapColor.DIRT))
            .transform(axeOrPickaxe())
            .blockstate((c, p) ->
                    BlockStateGen.axisBlock(c, p, (s) -> p.models().getExistingFile(CreateBitsnBobs.asResource("block/sprocket/large_cogwheel"))))
            .loot((lt, block) -> lt.dropOther(block, AllBlocks.LARGE_COGWHEEL.get()))
            .onRegister(GenericBlockEntityRenderModels.model(BnbPartialModels.LARGE_SPROCKET_COGWHEEL_BLOCK))
            .register();
    public static final BlockEntry<CogwheelChainBlock> SMALL_FLANGED_COGWHEEL_CHAIN = REGISTRATE.block("small_flanged_cogwheel_chain", CogwheelChainBlock::smallFlanged)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.sound(SoundType.WOOD)
                    .mapColor(MapColor.DIRT))
            .transform(axeOrPickaxe())
            .blockstate((c, p) ->
                    BlockStateGen.axisBlock(c, p, (s) -> p.models().getExistingFile(CreateBitsnBobs.asResource("block/flanged_gear/small_cogwheel"))))
            .loot((lt, block) -> lt.dropOther(block, BnbKineticBlocks.SMALL_EMPTY_FLANGED_COGWHEEL.get()))
            .onRegister(GenericBlockEntityRenderModels.model(BnbPartialModels.SMALL_FLANGED_COGWHEEL_BLOCK))
            .register();
    public static final BlockEntry<CogwheelChainBlock> LARGE_FLANGED_COGWHEEL_CHAIN = REGISTRATE.block("large_flanged_cogwheel_chain", CogwheelChainBlock::largeFlanged)
            .initialProperties(SharedProperties::wooden)
            .properties(p -> p.sound(SoundType.WOOD)
                    .mapColor(MapColor.DIRT))
            .transform(axeOrPickaxe())
            .blockstate((c, p) ->
                    BlockStateGen.axisBlock(c, p, (s) -> p.models().getExistingFile(CreateBitsnBobs.asResource("block/flanged_gear/large_cogwheel"))))
            .loot((lt, block) -> lt.dropOther(block, BnbKineticBlocks.LARGE_EMPTY_FLANGED_COGWHEEL.get()))
            .onRegister(GenericBlockEntityRenderModels.model(BnbPartialModels.LARGE_FLANGED_COGWHEEL_BLOCK))
            .register();

    public static void register() {
    }
}
