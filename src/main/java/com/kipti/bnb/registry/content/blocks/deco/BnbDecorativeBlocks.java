package com.kipti.bnb.registry.content.blocks.deco;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.decoration.chair.ChairBlock;
import com.kipti.bnb.content.decoration.girder_strut.GirderStrutBlock;
import com.kipti.bnb.content.decoration.girder_strut.GirderStrutBlockItem;
import com.kipti.bnb.content.decoration.girder_strut.GirderStrutModelBuilder;
import com.kipti.bnb.content.decoration.grating.GratingBlock;
import com.kipti.bnb.content.decoration.grating.GratingPanelBlock;
import com.kipti.bnb.content.decoration.grating.GratingPanelCTBehaviour;
import com.kipti.bnb.content.decoration.truss.AlternatingTrussBlock;
import com.kipti.bnb.content.decoration.weathered_girder.WeatheredConnectedGirderModel;
import com.kipti.bnb.content.decoration.weathered_girder.WeatheredGirderBlock;
import com.kipti.bnb.content.decoration.weathered_girder.WeatheredGirderBlockStateGenerator;
import com.kipti.bnb.content.decoration.weathered_girder.WeatheredGirderEncasedShaftBlock;
import com.kipti.bnb.foundation.BnbBlockStateGen;
import com.kipti.bnb.registry.datagen.BnbCreativeTabs;
import com.kipti.bnb.registry.core.BnbFeatureFlag;
import com.kipti.bnb.registry.client.BnbSpriteShifts;
import com.kipti.bnb.registry.core.BnbTags;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDisplaySources;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.actors.seat.SeatInteractionBehaviour;
import com.simibubi.create.content.contraptions.actors.seat.SeatMovementBehaviour;
import com.simibubi.create.foundation.block.DyedBlockList;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.utility.DyeHelper;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import static com.kipti.bnb.CreateBitsnBobs.REGISTRATE;
import static com.kipti.bnb.content.decoration.chair.ChairBlockStateGen.dyedChair;
import static com.simibubi.create.api.behaviour.display.DisplaySource.displaySource;
import static com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour.interactionBehaviour;
import static com.simibubi.create.api.behaviour.movement.MovementBehaviour.movementBehaviour;
import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOnly;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class BnbDecorativeBlocks {

    static {
        CreateBitsnBobs.REGISTRATE.setCreativeTab(BnbCreativeTabs.DECO_CREATIVE_TAB);
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
            
    public static final BlockEntry<GirderStrutBlock> WOODEN_GIRDER_STRUT = REGISTRATE.block("wooden_girder_strut", GirderStrutBlock.wooden())
            .initialProperties(SharedProperties::wooden)
            .transform(axeOnly())
            .properties(p -> p.noOcclusion()
                    .sound(SoundType.WOOD))
            .blockstate((c, p) -> p.directionalBlock(c.get(),
                    (state) -> p.models().getExistingFile(CreateBitsnBobs.asResource(
                            "block/girder_strut/wooden_girder_strut_attachment")
                    )))
            .onRegister(CreateRegistrate.blockModel(() -> GirderStrutModelBuilder::new))
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .item(GirderStrutBlockItem::new)
            .model((c, p) ->
                    p.withExistingParent(c.getName(), CreateBitsnBobs.asResource("block/girder_strut/wooden_girder_item"))
            )
            .build()
            .register();
            
    public static final BlockEntry<GirderStrutBlock> CABLE_GIRDER_STRUT = REGISTRATE.block("cable_girder_strut", GirderStrutBlock.cable())
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .properties(p -> p.noOcclusion()
                    .sound(SoundType.CHAIN))
            .blockstate((c, p) -> p.directionalBlock(c.get(),
                    (state) -> p.models().getExistingFile(CreateBitsnBobs.asResource(
                            "block/girder_strut/cable_girder_strut_attachment")
                    )))
            .onRegister(CreateRegistrate.blockModel(() -> GirderStrutModelBuilder::new))
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .item(GirderStrutBlockItem::new)
            .model((c, p) ->
                    p.withExistingParent(c.getName(), CreateBitsnBobs.asResource("block/girder_strut/cable_girder_item"))
            )
            .build()
            .register();


//    public static final BlockEntry<ColoredFallingBlock> CLINKER = REGISTRATE.block("clinker", (p) -> new ColoredFallingBlock(new ColorRGBA(0xd2d5d6), p))
//            .properties(p -> p.mapColor(MapColor.COLOR_GRAY)
//                    .sound(SoundType.GRAVEL))
//            .transform(b -> b.tag(BlockTags.MINEABLE_WITH_SHOVEL))
//            .simpleItem()
//            .register();

    public static final BlockEntry<GratingBlock> INDUSTRIAL_GRATING = CreateBitsnBobs.REGISTRATE.block("industrial_grating", GratingBlock::new)
            .properties(p -> p.mapColor(MapColor.METAL)
                    .strength(0.1f, 6.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
            )
            .transform(TagGen.pickaxeOnly())
            .blockstate((c, p) -> p.simpleBlock(c.get()))
            .onRegister(connectedTextures(() -> new SimpleCTBehaviour(BnbSpriteShifts.INDUSTRIAL_GRATING)))
            .addLayer(() -> RenderType::cutout)
            .simpleItem()
            .register();

    public static final BlockEntry<GratingPanelBlock> INDUSTRIAL_GRATING_PANEL = CreateBitsnBobs.REGISTRATE.block("industrial_grating_panel", GratingPanelBlock::new)
            .properties(p -> p.mapColor(MapColor.METAL)
                    .strength(0.1f, 6.0f)
                    .sound(SoundType.METAL)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
            )
            .transform(TagGen.pickaxeOnly())
            .blockstate((c, p) -> BnbBlockStateGen.directionalUvLockBlock(c, p, (state) -> p.models()
                    .withExistingParent(c.getName(), CreateBitsnBobs.asResource("block/grating_panel"))
                    .texture("panel", CreateBitsnBobs.asResource("block/industrial_grating"))
            ))
            .onRegister(connectedTextures(() -> new GratingPanelCTBehaviour(BnbSpriteShifts.INDUSTRIAL_GRATING)))
            .addLayer(() -> RenderType::cutout)
            .simpleItem()
            .register();

    public static final BlockEntry<AlternatingTrussBlock> INDUSTRIAL_TRUSS = CreateBitsnBobs.REGISTRATE.block("industrial_truss", AlternatingTrussBlock::new)
            .properties(p -> p.mapColor(MapColor.METAL)
                    .strength(0.1f, 6.0f)
                    .sound(SoundType.METAL)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
                    .noOcclusion()
            )
            .transform(TagGen.pickaxeOnly())
            .blockstate(BnbBlockStateGen::alternatingTrussModel)
            .onRegister(connectedTextures(() -> new GratingPanelCTBehaviour(BnbSpriteShifts.INDUSTRIAL_GRATING)))
            .addLayer(() -> RenderType::cutout)
            .simpleItem()
            .register();


    static {
        CreateBitsnBobs.REGISTRATE.setCreativeTab(BnbCreativeTabs.BASE_CREATIVE_TAB);
    }

    public static void register() {
    }

}


