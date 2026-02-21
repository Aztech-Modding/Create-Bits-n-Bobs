package com.kipti.bnb.registry.content.blocks;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.kinetics.chain_pulley.ChainPulleyBlock;
import com.kipti.bnb.content.kinetics.cogwheel_chain.block.EmptyFlangedGearBlock;
import com.kipti.bnb.content.kinetics.cogwheel_chain.block.GenericBlockEntityRenderModels;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainCandidateInfo;
import com.kipti.bnb.content.kinetics.flywheel_bearing.FlywheelBearingBlock;
import com.kipti.bnb.registry.client.BnbPartialModels;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.contraptions.pulley.PulleyBlock;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;

import static com.kipti.bnb.CreateBitsnBobs.REGISTRATE;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;

public class BnbKineticBlocks {

    public static final BlockEntry<EmptyFlangedGearBlock> SMALL_EMPTY_FLANGED_COGWHEEL = REGISTRATE.block("small_flanged_cogwheel", EmptyFlangedGearBlock::small)
            .lang("Flanged Cogwheel")
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.sound(SoundType.WOOD)
                    .mapColor(MapColor.DIRT))
            .transform(axeOrPickaxe())
            .blockstate((c, p) ->
                    BlockStateGen.axisBlock(c, p, (s) -> p.models().getExistingFile(CreateBitsnBobs.asResource("block/flanged_gear/small_cogwheel"))))
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), CreateBitsnBobs.asResource("block/flanged_gear/small_cogwheel")))
            .build()
            .onRegister(CogwheelChainCandidateInfo.candidate(new CogwheelChainCandidateInfo(false, false, BnbChainBlocks.SMALL_FLANGED_COGWHEEL_CHAIN)))
            .onRegister(GenericBlockEntityRenderModels.model(BnbPartialModels.SMALL_FLANGED_COGWHEEL_BLOCK))
            .register();

    public static final BlockEntry<EmptyFlangedGearBlock> LARGE_EMPTY_FLANGED_COGWHEEL = REGISTRATE.block("large_flanged_cogwheel", EmptyFlangedGearBlock::large)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.sound(SoundType.WOOD)
                    .mapColor(MapColor.DIRT))
            .transform(axeOrPickaxe())
            .blockstate((c, p) ->
                    BlockStateGen.axisBlock(c, p, (s) -> p.models().getExistingFile(CreateBitsnBobs.asResource("block/flanged_gear/large_cogwheel"))))
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), CreateBitsnBobs.asResource("block/flanged_gear/large_cogwheel")))
            .build()
            .onRegister(CogwheelChainCandidateInfo.candidate(new CogwheelChainCandidateInfo(true, false, BnbChainBlocks.LARGE_FLANGED_COGWHEEL_CHAIN)))
            .onRegister(GenericBlockEntityRenderModels.model(BnbPartialModels.LARGE_FLANGED_COGWHEEL_BLOCK))
            .register();

    public static final BlockEntry<ChainPulleyBlock> CHAIN_PULLEY = REGISTRATE.block("chain_pulley", ChainPulleyBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .properties(p -> p.noOcclusion())
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(axeOrPickaxe())
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .blockstate((ctx, prov) -> prov.getVariantBuilder(ctx.getEntry())
                    .forAllStates(state -> {
                        Direction.Axis axis = state.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                        return ConfiguredModel.builder()
                                .modelFile(axis == Direction.Axis.Z ? AssetLookup.partialBaseModel(ctx, prov, "z") : AssetLookup.partialBaseModel(ctx, prov))
                                .rotationY(axis == Direction.Axis.X ? 90 : 0)
                                .build();
                    }))
            .item()
            .transform(customItemModel())
            .register();
    public static final BlockEntry<PulleyBlock.RopeBlock> CHAIN_ROPE = REGISTRATE.block("chain_rope", PulleyBlock.RopeBlock::new)
            .properties(p -> p.sound(SoundType.CHAIN)
                    .mapColor(MapColor.COLOR_GRAY))
            .tag(AllTags.AllBlockTags.BRITTLE.tag)
            .addLayer(() -> RenderType::cutout)
            .tag(BlockTags.CLIMBABLE)
            .blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
                    .getExistingFile(p.modLoc("block/chain_pulley/" + c.getName()))))
            .register();
    public static final BlockEntry<PulleyBlock.MagnetBlock> CHAIN_PULLEY_MAGNET =
            REGISTRATE.block("chain_pulley_magnet", PulleyBlock.MagnetBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .tag(AllTags.AllBlockTags.BRITTLE.tag)
                    .tag(BlockTags.CLIMBABLE)
                    .addLayer(() -> RenderType::cutout)
                    .blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
                            .getExistingFile(p.modLoc("block/chain_pulley/" + c.getName()))))
                    .register();
    public static final BlockEntry<FlywheelBearingBlock> FLYWHEEL_BEARING =
            REGISTRATE.block("flywheel_bearing", FlywheelBearingBlock::new)
                    .transform(axeOrPickaxe())
                    .properties(p -> p.mapColor(MapColor.GOLD).noOcclusion())
                    .onRegister(BlockStressValues.setGeneratorSpeed(16, true))
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
                    .blockstate((c, p) -> p.directionalBlock(c.get(),
                            (state) -> p.models().getExistingFile(CreateBitsnBobs.asResource(
                                    "block/flywheel_bearing/block")
                            )))
                    .item()
                    .model((c, p) ->
                            p.withExistingParent(c.getName(), CreateBitsnBobs.asResource("block/flywheel_bearing/item"))
                    )
                    .build()
                    .register();

    public static void register() {
    }

}
