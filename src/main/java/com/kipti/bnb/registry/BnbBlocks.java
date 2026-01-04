package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.decoration.chair.ChairBlock;
import com.kipti.bnb.content.decoration.girder_strut.GirderStrutBlock;
import com.kipti.bnb.content.decoration.girder_strut.GirderStrutBlockItem;
import com.kipti.bnb.content.decoration.girder_strut.GirderStrutModelBuilder;
import com.kipti.bnb.content.decoration.light.founation.LightBlock;
import com.kipti.bnb.content.decoration.light.headlamp.HeadlampBlock;
import com.kipti.bnb.content.decoration.light.headlamp.HeadlampBlockItem;
import com.kipti.bnb.content.decoration.light.headlamp.HeadlampModelBuilder;
import com.kipti.bnb.content.decoration.light.lightbulb.LightbulbBlock;
import com.kipti.bnb.content.decoration.nixie.foundation.DoubleOrientedBlockModel;
import com.kipti.bnb.content.decoration.nixie.large_nixie_tube.LargeNixieTubeBlockNixie;
import com.kipti.bnb.content.decoration.nixie.large_nixie_tube.LargeNixieTubeBlockStateGen;
import com.kipti.bnb.content.decoration.nixie.nixie_board.NixieBoardBlockNixie;
import com.kipti.bnb.content.decoration.nixie.nixie_board.NixieBoardBlockStateGen;
import com.kipti.bnb.content.decoration.weathered_girder.WeatheredConnectedGirderModel;
import com.kipti.bnb.content.decoration.weathered_girder.WeatheredGirderBlock;
import com.kipti.bnb.content.decoration.weathered_girder.WeatheredGirderBlockStateGenerator;
import com.kipti.bnb.content.decoration.weathered_girder.WeatheredGirderEncasedShaftBlock;
import com.kipti.bnb.content.kinetics.chain_pulley.ChainPulleyBlock;
import com.kipti.bnb.content.kinetics.cogwheel_chain.block.CogwheelChainBlock;
import com.kipti.bnb.content.kinetics.cogwheel_chain.block.ConnectingCogwheelChainBlock;
import com.kipti.bnb.content.kinetics.cogwheel_chain.block.EmptyFlangedGearBlock;
import com.kipti.bnb.content.kinetics.encased_blocks.BnbEncasedCogwheelBlock;
import com.kipti.bnb.content.kinetics.encased_blocks.BnbEncasedShaftBlock;
import com.kipti.bnb.content.kinetics.encased_blocks.piston_pole.EncasedPistonExtensionPoleBlock;
import com.kipti.bnb.content.kinetics.flywheel_bearing.FlywheelBearingBlock;
import com.kipti.bnb.foundation.BnbBlockStateGen;
import com.kipti.bnb.foundation.BnbBuilderTransformers;
import com.kipti.bnb.foundation.EncasedBlockList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDisplaySources;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.contraptions.actors.seat.SeatInteractionBehaviour;
import com.simibubi.create.content.contraptions.actors.seat.SeatMovementBehaviour;
import com.simibubi.create.content.contraptions.pulley.PulleyBlock;
import com.simibubi.create.content.decoration.encasing.EncasableBlock;
import com.simibubi.create.content.decoration.encasing.EncasedCTBehaviour;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.content.decoration.bracket.BracketBlock;
import com.simibubi.create.content.decoration.bracket.BracketBlockItem;
import com.simibubi.create.content.decoration.bracket.BracketGenerator;
import com.simibubi.create.foundation.block.DyedBlockList;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.utility.DyeHelper;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;

import static com.kipti.bnb.CreateBitsnBobs.REGISTRATE;
import static com.kipti.bnb.content.decoration.chair.ChairBlockStateGen.dyedChair;
import static com.simibubi.create.api.behaviour.display.DisplaySource.displaySource;
import static com.simibubi.create.api.behaviour.display.DisplayTarget.displayTarget;
import static com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour.interactionBehaviour;
import static com.simibubi.create.api.behaviour.movement.MovementBehaviour.movementBehaviour;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.*;

public class BnbBlocks {

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

    public static final BlockEntry<WeatheredGirderBlock> WEATHERED_METAL_GIRDER = REGISTRATE.block("weathered_metal_girder", WeatheredGirderBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY)
                    .sound(SoundType.NETHERITE_BLOCK))
            .transform(pickaxeOnly())
            .blockstate(WeatheredGirderBlockStateGenerator::blockState)
            .onRegister(CreateRegistrate.blockModel(() -> WeatheredConnectedGirderModel::new))
            .item()
            .transform(customItemModel())
            .register();

    public static final BlockEntry<WeatheredGirderEncasedShaftBlock> WEATHERED_METAL_GIRDER_ENCASED_SHAFT =
            REGISTRATE.block("weathered_metal_girder_encased_shaft", WeatheredGirderEncasedShaftBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY)
                            .sound(SoundType.NETHERITE_BLOCK))
                    .transform(pickaxeOnly())
                    .blockstate(WeatheredGirderBlockStateGenerator::blockStateWithShaft)
                    .loot((p, b) -> p.add(b, p.createSingleItemTable(WEATHERED_METAL_GIRDER.get())
                            .withPool(p.applyExplosionCondition(AllBlocks.SHAFT.get(), LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1.0F))
                                    .add(LootItem.lootTableItem(AllBlocks.SHAFT.get()))))))
                    .onRegister(CreateRegistrate.blockModel(() -> WeatheredConnectedGirderModel::new))
                    .register();

    public static final BlockEntry<GirderStrutBlock> WEATHERED_GIRDER_STRUT = REGISTRATE.block("weathered_girder_strut", GirderStrutBlock.weathered())
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .properties(p -> p.noOcclusion())
            .blockstate((c, p) -> p.directionalBlock(c.get(),
                    (state) -> p.models().getExistingFile(CreateBitsnBobs.asResource(
                            "block/girder_strut/weathered_girder_strut_attachment")
                    )))
            .onRegister(CreateRegistrate.blockModel(() -> GirderStrutModelBuilder::new))
            .onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "block.bits_n_bobs.girder_strut"))
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .item(GirderStrutBlockItem::new)
            .model((c, p) ->
                    p.withExistingParent(c.getName(), CreateBitsnBobs.asResource("block/girder_strut/weathered_girder_item"))
            )
            .build()
            .register();

    public static final BlockEntry<GirderStrutBlock> GIRDER_STRUT = REGISTRATE.block("girder_strut", GirderStrutBlock.normal())
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .properties(p -> p.noOcclusion())
            .blockstate((c, p) -> p.directionalBlock(c.get(),
                    (state) -> p.models().getExistingFile(CreateBitsnBobs.asResource(
                            "block/girder_strut/girder_strut_attachment")
                    )))
            .onRegister(CreateRegistrate.blockModel(() -> GirderStrutModelBuilder::new))
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .item(GirderStrutBlockItem::new)
            .model((c, p) ->
                    p.withExistingParent(c.getName(), CreateBitsnBobs.asResource("block/girder_strut/girder_item"))
            )
            .build()
            .register();

    public static final BlockEntry<NixieBoardBlockNixie> NIXIE_BOARD = REGISTRATE.block("nixie_board", p -> new NixieBoardBlockNixie(p, null))
            .transform(nixieBoard())
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), CreateBitsnBobs.asResource("block/nixie_board/nixie_board_single")))
            .build()
            .register();

    public static final DyedBlockList<NixieBoardBlockNixie> DYED_NIXIE_BOARD = new DyedBlockList<>(colour -> {
        String colourName = colour.getSerializedName();
        return REGISTRATE.block(colourName + "_nixie_board", p -> new NixieBoardBlockNixie(p, colour))
                .transform(nixieBoard())
                .register();
    });

    public static final BlockEntry<LargeNixieTubeBlockNixie> LARGE_NIXIE_TUBE = REGISTRATE.block("large_nixie_tube", p -> new LargeNixieTubeBlockNixie(p, null))
            .transform(largeNixieTube())
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), CreateBitsnBobs.asResource("block/large_nixie_tube/large_nixie_tube")))
            .build()
            .register();

    public static final DyedBlockList<LargeNixieTubeBlockNixie> DYED_LARGE_NIXIE_TUBE = new DyedBlockList<>(colour -> {
        String colourName = colour.getSerializedName();
        return REGISTRATE.block(colourName + "_large_nixie_tube", p -> new LargeNixieTubeBlockNixie(p, colour))
                .transform(largeNixieTube())
                .register();
    });

    public static final BlockEntry<LightbulbBlock> LIGHTBULB = REGISTRATE.block("lightbulb", LightbulbBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.directionalBlock(c.get(),
                    (state) -> p.models().getExistingFile(CreateBitsnBobs.asResource(
                            "block/lightbulb/lightbulb" + (state.getValue(LightbulbBlock.CAGE) ? "" : "_uncaged") + (LightBlock.shouldUseOnLightModel(state) ? "_on" : "")
                    ))))
            .properties(p -> p
                    .noOcclusion()
                    .lightLevel(LightBlock::getLightLevel)
                    .emissiveRendering((state, level, pos) -> state.getValue(LightBlock.POWER) > 0)
                    .forceSolidOn())
            .addLayer(() -> RenderType::translucent)
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), CreateBitsnBobs.asResource("block/lightbulb/lightbulb_uncaged")))
            .build()
            .register();

    public static final BlockEntry<HeadlampBlock> HEADLAMP = REGISTRATE.block("headlamp", HeadlampBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.simpleBlock(c.get(),
                    p.models().getExistingFile(CreateBitsnBobs.asResource(
                            "block/headlamp/headlight_block"
                    ))
            ))
            .onRegister(CreateRegistrate.blockModel(() -> HeadlampModelBuilder::new))
            .properties(p -> p
                    .noOcclusion()
                    .lightLevel(LightBlock::getLightLevel)
                    .emissiveRendering((state, level, pos) -> state.getValue(LightBlock.POWER) > 0)
                    .mapColor(DyeColor.ORANGE)
                    .forceSolidOn())
            .addLayer(() -> RenderType::translucent)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .item(HeadlampBlockItem::new)
            .model((c, p) -> p.withExistingParent(c.getName(), CreateBitsnBobs.asResource("block/headlamp/headlight")))
            .build()
            .register();

    public static final BlockEntry<LightBlock> BRASS_LAMP = REGISTRATE.block("brass_lamp", (p) -> new LightBlock(p, BnbShapes.BRASS_LAMP_SHAPE, true))
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.directionalBlock(c.get(),
                    (state) -> p.models().getExistingFile(CreateBitsnBobs.asResource(
                            "block/brass_lamp/brass_lamp" + (LightBlock.shouldUseOnLightModel(state) ? "_on" : "")
                    ))))
            .properties(p -> p
                    .noOcclusion()
                    .lightLevel(LightBlock::getLightLevel)
                    .emissiveRendering((state, level, pos) -> state.getValue(LightBlock.POWER) > 0)
                    .mapColor(DyeColor.ORANGE)
                    .forceSolidOn())
            .addLayer(() -> RenderType::translucent)
            .item()
            .model((c, p) -> p.withExistingParent(c.getName(), CreateBitsnBobs.asResource("block/brass_lamp/brass_lamp")))
            .build()
            .register();

    public static final BlockEntry<ConnectingCogwheelChainBlock> SMALL_SPROCKET_COGWHEEL_CHAIN = REGISTRATE.block("small_cogwheel_chain", ConnectingCogwheelChainBlock::small)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.sound(SoundType.WOOD)
                    .mapColor(MapColor.DIRT))
            .transform(axeOrPickaxe())
            .blockstate((c, p) ->
                    BlockStateGen.axisBlock(c, p, (s) -> p.models().getExistingFile(CreateBitsnBobs.asResource("block/sprocket/small_cogwheel"))))
            .loot((lt, block) -> lt.dropOther(block, AllBlocks.COGWHEEL.get()))
            .register();

    public static final BlockEntry<ConnectingCogwheelChainBlock> LARGE_SPROCKET_COGWHEEL_CHAIN = REGISTRATE.block("large_cogwheel_chain", ConnectingCogwheelChainBlock::large)
            .initialProperties(SharedProperties::wooden)
            .properties(p -> p.sound(SoundType.WOOD)
                    .mapColor(MapColor.DIRT))
            .transform(axeOrPickaxe())
            .blockstate((c, p) ->
                    BlockStateGen.axisBlock(c, p, (s) -> p.models().getExistingFile(CreateBitsnBobs.asResource("block/sprocket/large_cogwheel"))))
            .loot((lt, block) -> lt.dropOther(block, AllBlocks.LARGE_COGWHEEL.get()))
            .register();

    public static final BlockEntry<CogwheelChainBlock> SMALL_FLANGED_COGWHEEL_CHAIN = REGISTRATE.block("small_flanged_cogwheel_chain", CogwheelChainBlock::smallFlanged)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.sound(SoundType.WOOD)
                    .mapColor(MapColor.DIRT))
            .transform(axeOrPickaxe())
            .blockstate((c, p) ->
                    BlockStateGen.axisBlock(c, p, (s) -> p.models().getExistingFile(CreateBitsnBobs.asResource("block/flanged_gear/small_cogwheel"))))
            .loot((lt, block) -> lt.dropOther(block, BnbBlocks.SMALL_EMPTY_FLANGED_COGWHEEL.get()))
            .register();

    public static final BlockEntry<CogwheelChainBlock> LARGE_FLANGED_COGWHEEL_CHAIN = REGISTRATE.block("large_flanged_cogwheel_chain", CogwheelChainBlock::largeFlanged)
            .initialProperties(SharedProperties::wooden)
            .properties(p -> p.sound(SoundType.WOOD)
                    .mapColor(MapColor.DIRT))
            .transform(axeOrPickaxe())
            .blockstate((c, p) ->
                    BlockStateGen.axisBlock(c, p, (s) -> p.models().getExistingFile(CreateBitsnBobs.asResource("block/flanged_gear/large_cogwheel"))))
            .loot((lt, block) -> lt.dropOther(block, BnbBlocks.LARGE_EMPTY_FLANGED_COGWHEEL.get()))
            .register();

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
            .register();

    public static <T extends NixieBoardBlockNixie, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> nixieBoard() {
        return b -> b
                .initialProperties(SharedProperties::softMetal)
                .transform(displayTarget(BnbDisplayTargets.GENERIC_NIXIE_TARGET))
                .transform(pickaxeOnly())
                .blockstate(NixieBoardBlockStateGen::nixieBoard)
                .onRegister(CreateRegistrate.blockModel(() -> DoubleOrientedBlockModel::new))
                .properties(p -> p
                        .noOcclusion()
                        .mapColor(DyeColor.ORANGE)
                        .forceSolidOn())
                .addLayer(() -> RenderType::translucent);
    }

    public static <T extends LargeNixieTubeBlockNixie, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> largeNixieTube() {
        return b -> b
                .initialProperties(SharedProperties::softMetal)
                .transform(displayTarget(BnbDisplayTargets.GENERIC_NIXIE_TARGET))
                .transform(pickaxeOnly())
                .blockstate(LargeNixieTubeBlockStateGen::nixieTube)
                .onRegister(CreateRegistrate.blockModel(() -> DoubleOrientedBlockModel::new))
                .properties(p -> p
                        .noOcclusion()
                        .mapColor(DyeColor.ORANGE)
                        .forceSolidOn())
                .addLayer(() -> RenderType::translucent);
    }

    public static final DyedBlockList<ChairBlock> CHAIRS = new DyedBlockList<>(colour -> {
        String colourName = colour.getSerializedName();
        SeatMovementBehaviour movementBehaviour = new SeatMovementBehaviour();
        SeatInteractionBehaviour interactionBehaviour = new SeatInteractionBehaviour();
        return REGISTRATE.block(colourName + "_chair", p -> new ChairBlock(p, colour))
                .initialProperties(SharedProperties::wooden)
                .properties(p -> p.mapColor(colour))
                .properties(BlockBehaviour.Properties::noOcclusion)
                .transform(axeOnly())
                .onRegister(movementBehaviour(movementBehaviour))
                .onRegister(interactionBehaviour(interactionBehaviour))
                .transform(displaySource(AllDisplaySources.ENTITY_NAME))
//            .onRegister(CreateRegistrate.blockModel(() -> ChairModelBuilder::new))
                .blockstate(dyedChair(colourName))
                .addLayer(() -> RenderType::cutoutMipped)
                .recipe((c, p) -> {
                    ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, c.get())
                            .requires(DyeHelper.getWoolOfDye(colour))
                            .requires(ItemTags.WOODEN_STAIRS)
                            .unlockedBy("has_wool", RegistrateRecipeProvider.has(ItemTags.WOOL))
                            .save(p.withConditions(BnbFeatureFlag.CHAIRS.getDataCondition()), CreateBitsnBobs.asResource("crafting/" + c.getName()));
                    ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, c.get())
                            .requires(colour.getTag())
                            .requires(BnbTags.BnbItemTags.CHAIRS.tag)
                            .unlockedBy("has_seat", RegistrateRecipeProvider.has(BnbTags.BnbItemTags.CHAIRS.tag))
                            .save(p.withConditions((BnbFeatureFlag.CHAIRS.getDataCondition())), CreateBitsnBobs.asResource("crafting/" + c.getName() + "_from_other_chair"));
                })
                .onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "block.bits_n_bobs.chair"))
                .tag(BnbTags.BnbBlockTags.CHAIRS.tag)
                .item()
                .model((c, p) ->
                        p.withExistingParent("item/" + colourName + "_chair", p.modLoc("block/chair/item"))
                                .texture("2", p.modLoc("block/chair/chair_" + colourName)))
                .tag(BnbTags.BnbItemTags.CHAIRS.tag)
                .build()
                .register();
    });

    //Base encasing extensions

    public static final BlockEntry<BracketBlock> WEATHERED_METAL_BRACKET = REGISTRATE.block("weathered_metal_bracket", BracketBlock::new)
            .blockstate(new BracketGenerator("weathered_metal")::generate)
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .transform(pickaxeOnly())
            .item(BracketBlockItem::new)
            .tag(AllTags.AllItemTags.INVALID_FOR_TRACK_PAVING.tag)
            .transform(BracketGenerator.itemModel("weathered_metal"))
            .register();

    public static final BlockEntry<BnbEncasedShaftBlock> INDUSTRIAL_IRON_ENCASED_SHAFT = REGISTRATE
            .block("industrial_iron_encased_shaft", p -> new BnbEncasedShaftBlock(p, AllBlocks.INDUSTRIAL_IRON_BLOCK))
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .transform(BnbBuilderTransformers.encasedShaftWithoutCT("industrial_iron"))
            .transform(EncasingRegistry.addVariantTo(AllBlocks.SHAFT))
            .transform(axeOrPickaxe())
            .register();

    public static final BlockEntry<BnbEncasedShaftBlock> WEATHERED_IRON_ENCASED_SHAFT = REGISTRATE
            .block("weathered_iron_encased_shaft", p -> new BnbEncasedShaftBlock(p, AllBlocks.WEATHERED_IRON_BLOCK))
            .properties(p -> p.mapColor(MapColor.COLOR_BROWN))
            .transform(BnbBuilderTransformers.encasedShaftWithoutCT("weathered_iron"))
            .transform(EncasingRegistry.addVariantTo(AllBlocks.SHAFT))
            .transform(axeOrPickaxe())
            .register();

    public static final BlockEntry<BnbEncasedCogwheelBlock> INDUSTRIAL_IRON_ENCASED_COGWHEEL = REGISTRATE
            .block("industrial_iron_encased_cogwheel", p -> new BnbEncasedCogwheelBlock(p, false, AllBlocks.INDUSTRIAL_IRON_BLOCK))
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .transform(BnbBuilderTransformers.encasedCogwheelWithoutCT("industrial_iron"))
            .transform(EncasingRegistry.addVariantTo(AllBlocks.COGWHEEL))
            .transform(axeOrPickaxe())
            .register();

    public static final BlockEntry<BnbEncasedCogwheelBlock> WEATHERED_IRON_ENCASED_COGWHEEL = REGISTRATE
            .block("weathered_iron_encased_cogwheel", p -> new BnbEncasedCogwheelBlock(p, false, AllBlocks.WEATHERED_IRON_BLOCK))
            .properties(p -> p.mapColor(MapColor.COLOR_BROWN))
            .transform(BnbBuilderTransformers.encasedCogwheelWithoutCT("weathered_iron"))
            .transform(EncasingRegistry.addVariantTo(AllBlocks.COGWHEEL))
            .transform(axeOrPickaxe())
            .register();

    public static final BlockEntry<BnbEncasedCogwheelBlock> INDUSTRIAL_IRON_ENCASED_LARGE_COGWHEEL = REGISTRATE
            .block("industrial_iron_encased_large_cogwheel", p -> new BnbEncasedCogwheelBlock(p, true, AllBlocks.INDUSTRIAL_IRON_BLOCK))
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(BnbBuilderTransformers.encasedLargeCogwheelWithoutCT("industrial_iron"))
            .transform(EncasingRegistry.addVariantTo(AllBlocks.LARGE_COGWHEEL))
            .transform(axeOrPickaxe())
            .register();

    public static final BlockEntry<BnbEncasedCogwheelBlock> WEATHERED_IRON_ENCASED_LARGE_COGWHEEL = REGISTRATE
            .block("weathered_iron_encased_large_cogwheel", p -> new BnbEncasedCogwheelBlock(p, true, AllBlocks.WEATHERED_IRON_BLOCK))
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
            .transform(BnbBuilderTransformers.encasedLargeCogwheelWithoutCT("weathered_iron"))
            .transform(EncasingRegistry.addVariantTo(AllBlocks.LARGE_COGWHEEL))
            .transform(axeOrPickaxe())
            .register();

    // New encasing types

    public static final EncasedBlockList<EncasedPistonExtensionPoleBlock> ENCASED_PISTON_EXTENSION_POLE = new EncasedBlockList<>((casing) -> REGISTRATE
            .block(casing.asId("encased_piston_extension_pole"), (p) -> new EncasedPistonExtensionPoleBlock(p, casing.getMaterial()))
            .initialProperties(() -> Blocks.PISTON_HEAD)
            .properties(p -> p.sound(SoundType.SCAFFOLDING)
                    .mapColor(MapColor.DIRT)
                    .forceSolidOn())
            .transform(EncasingRegistry.addVariantTo(() -> ((Block & EncasableBlock) AllBlocks.PISTON_EXTENSION_POLE.get())))
            .transform(axeOrPickaxe())
            .transform(casing.withCT(
                    (builder, ct) -> builder
                            .onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(ct)))
                            .onRegister(CreateRegistrate.casingConnectivity((block, cc) -> cc.make(block, ct,
                                    (s, f) -> f.getAxis() != s.getValue(EncasedPistonExtensionPoleBlock.FACING).getAxis())))
            ))
            .blockstate((c, p) -> BnbBlockStateGen.directionalBlockIgnoresWaterlogged(c, p, (blockState) -> {
                final String suffix = blockState.getValue(EncasedPistonExtensionPoleBlock.EMPTY) ? "_empty" : "";
                final String modelName = c.getName() + suffix;
                return p.models()
                        .withExistingParent(modelName, p.modLoc("block/encased_piston_pole/block" + suffix))
                        .texture("casing", casing.getSurfaceTexture())
                        .texture("opening", casing.getGearboxTexture());
            }, true))
            .loot((p, lb) -> p.add(lb, LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(lb)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(EncasedPistonExtensionPoleBlock.EMPTY, false)))
                            .add(LootItem.lootTableItem(AllBlocks.PISTON_EXTENSION_POLE.get().asItem())))
            ))
            .simpleItem()
            .register());

    public static void register() {
    }

}
