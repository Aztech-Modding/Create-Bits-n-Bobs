package com.kipti.bnb.registry.content.blocks.deco;

import com.cake.struts.content.StrutModelBuilder;
import com.cake.struts.content.block.StrutBlockItem;
import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.decoration.grating.GratingBlock;
import com.kipti.bnb.content.decoration.grating.GratingPanelBlock;
import com.kipti.bnb.content.decoration.grating.GratingPanelBlockItem;
import com.kipti.bnb.content.decoration.grating.GratingPanelCTBehaviour;
import com.kipti.bnb.content.decoration.strut.BnbStrutBlock;
import com.kipti.bnb.content.decoration.strut.CableStrutBlock;
import com.kipti.bnb.content.decoration.truss.TrussBlock;
import com.kipti.bnb.content.decoration.truss.TrussBlockItem;
import com.kipti.bnb.content.decoration.truss.TrussBlockStateGen;
import com.kipti.bnb.content.decoration.truss.TrussPipeBlock;
import com.kipti.bnb.content.decoration.truss.TrussEncasedShaftBlock;
import com.kipti.bnb.content.decoration.weathered_girder.WeatheredConnectedGirderModel;
import com.kipti.bnb.content.decoration.weathered_girder.WeatheredGirderBlock;
import com.kipti.bnb.content.decoration.weathered_girder.WeatheredGirderBlockStateGenerator;
import com.kipti.bnb.content.decoration.weathered_girder.WeatheredGirderEncasedShaftBlock;
import com.kipti.bnb.foundation.client.BnbBlockStateGen;
import com.kipti.bnb.registry.client.BnbSpriteShifts;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.content.fluids.PipeAttachmentModel;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.simibubi.create.foundation.item.ItemDescription;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import static com.kipti.bnb.CreateBitsnBobs.REGISTRATE;
import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class BnbDecorativeBlocks {

    public static final BlockEntry<WeatheredGirderBlock> WEATHERED_METAL_GIRDER = REGISTRATE.block(
                    "weathered_metal_girder",
                    WeatheredGirderBlock::new
            )
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
                    .loot((p, b) -> p.add(
                            b, p.createSingleItemTable(WEATHERED_METAL_GIRDER.get())
                                    .withPool(p.applyExplosionCondition(
                                            AllBlocks.SHAFT.get(), LootPool.lootPool()
                                                    .setRolls(ConstantValue.exactly(1.0F))
                                                    .add(LootItem.lootTableItem(AllBlocks.SHAFT.get()))
                                    ))
                    ))
                    .onRegister(CreateRegistrate.blockModel(() -> WeatheredConnectedGirderModel::new))
                    .register();

    public static final BlockEntry<BnbStrutBlock> WEATHERED_GIRDER_STRUT = REGISTRATE.block(
                    "weathered_girder_strut",
                    p -> new BnbStrutBlock(
                            p,
                            BnbStrutDefinitions.WEATHERED_MODEL
                    )
            )
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .properties(p -> p.noOcclusion())
            .blockstate((c, p) -> p.directionalBlock(
                    c.get(),
                    (state) -> p.models().getExistingFile(CreateBitsnBobs.asResource(
                            "block/girder_strut/weathered_girder_strut_attachment")
                    )
            ))
            .onRegister(CreateRegistrate.blockModel(() -> StrutModelBuilder::new))
            .onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "block.bits_n_bobs.girder_strut"))
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .item(StrutBlockItem::new)
            .model((c, p) ->
                           p.withExistingParent(
                                   c.getName(),
                                   CreateBitsnBobs.asResource("block/girder_strut/weathered_girder_item")
                           )
            )
            .build()
            .register();

    public static final BlockEntry<BnbStrutBlock> GIRDER_STRUT = REGISTRATE.block(
                    "girder_strut",
                    p -> new BnbStrutBlock(
                            p,
                            BnbStrutDefinitions.NORMAL_MODEL
                    )
            )
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .properties(p -> p.noOcclusion())
            .blockstate((c, p) -> p.directionalBlock(
                    c.get(),
                    (state) -> p.models().getExistingFile(CreateBitsnBobs.asResource(
                            "block/girder_strut/girder_strut_attachment")
                    )
            ))
            .onRegister(CreateRegistrate.blockModel(() -> StrutModelBuilder::new))
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .item(StrutBlockItem::new)
            .model((c, p) ->
                           p.withExistingParent(
                                   c.getName(),
                                   CreateBitsnBobs.asResource("block/girder_strut/girder_item")
                           )
            )
            .build()
            .register();

//    public static final BlockEntry<BnbStrutBlock> WOODEN_GIRDER_STRUT = REGISTRATE.block(
//                    "wooden_girder_strut",
//                    p -> new BnbStrutBlock(
//                            p,
//                            BnbStrutDefinitions.WOODEN_MODEL
//                    )
//            )
//            .initialProperties(SharedProperties::wooden)
//            .transform(axeOnly())
//            .properties(p -> p.noOcclusion()
//                    .sound(SoundType.WOOD))
//            .blockstate((c, p) -> p.directionalBlock(
//                    c.get(),
//                    (state) -> p.models().getExistingFile(CreateBitsnBobs.asResource(
//                            "block/girder_strut/wooden_girder_strut_attachment")
//                    )
//            ))
//            .onRegister(CreateRegistrate.blockModel(() -> StrutModelBuilder::new))
//            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
//            .item(StrutBlockItem::new)
//            .model((c, p) ->
//                           p.withExistingParent(
//                                   c.getName(),
//                                   CreateBitsnBobs.asResource("block/girder_strut/wooden_girder_item")
//                           )
//            )
//            .build()
//            .register();

    public static final BlockEntry<CableStrutBlock> CABLE_GIRDER_STRUT = REGISTRATE.block(
                    "cable_girder_strut",
                    p -> new CableStrutBlock(
                            p,
                            BnbStrutDefinitions.CABLE_MODEL,
                            BnbStrutDefinitions.CABLE_INFO
                    )
            )
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .properties(p -> p.noOcclusion()
                    .sound(SoundType.CHAIN))
            .blockstate((c, p) -> p.directionalBlock(
                    c.get(),
                    (state) -> p.models().getExistingFile(CreateBitsnBobs.asResource(
                            "block/girder_strut/cable_girder_strut_attachment")
                    )
            ))
            .onRegister(CreateRegistrate.blockModel(() -> StrutModelBuilder::new))
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .item(StrutBlockItem::new)
            .model((c, p) ->
                           p.withExistingParent(
                                   c.getName(),
                                   CreateBitsnBobs.asResource("block/girder_strut/cable_girder_item")
                           )
            )
            .build()
            .register();

    public static final BlockEntry<GratingBlock> INDUSTRIAL_GRATING = CreateBitsnBobs.REGISTRATE.block(
                    "industrial_grating",
                    GratingBlock::new
            )
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

    public static final BlockEntry<GratingPanelBlock> INDUSTRIAL_GRATING_PANEL = CreateBitsnBobs.REGISTRATE.block(
                    "industrial_grating_panel",
                    GratingPanelBlock::new
            )
            .properties(p -> p.mapColor(MapColor.METAL)
                    .noOcclusion()
                    .strength(0.1f, 6.0f)
                    .sound(SoundType.METAL)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
            )
            .transform(TagGen.pickaxeOnly())
            .blockstate((c, p) ->
                                BnbBlockStateGen.directionalMixedUvLockBlock(
                                        c, p,
                                        p.models().getExistingFile(CreateBitsnBobs.asResource(
                                                "block/industrial_grating/panel")),
                                        p.models().getExistingFile(CreateBitsnBobs.asResource(
                                                "block/industrial_grating/panel_side"))
                                ))
            .onRegister(connectedTextures(() -> new GratingPanelCTBehaviour(BnbSpriteShifts.INDUSTRIAL_GRATING)))
            .addLayer(() -> RenderType::cutout)
            .item(GratingPanelBlockItem::new)
            .model((c, p) ->
                           p.withExistingParent(
                                   c.getName(),
                                   CreateBitsnBobs.asResource("block/industrial_grating/item")
                           )
            )
            .build()
            .register();

    public static final BlockEntry<TrussBlock> INDUSTRIAL_TRUSS = CreateBitsnBobs.REGISTRATE.block(
                    "industrial_truss",
                    TrussBlock::new
            )
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
            .item(TrussBlockItem::new)
            .model((c, p) ->
                           p.withExistingParent(
                                   c.getName(),
                                   CreateBitsnBobs.asResource("block/industrial_truss/industrial_truss")
                           )
            )
            .build()
            .register();

    public static final BlockEntry<TrussEncasedShaftBlock> INDUSTRIAL_TRUSS_ENCASED_SHAFT = CreateBitsnBobs.REGISTRATE.block(
                    "industrial_truss_encased_shaft",
                    TrussEncasedShaftBlock::new
            )
            .properties(p -> p.mapColor(MapColor.METAL)
                    .strength(0.1f, 6.0f)
                    .sound(SoundType.METAL)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
                    .noOcclusion()
            )
            .transform(TagGen.pickaxeOnly())
            .blockstate(TrussBlockStateGen::trussEncasedShaftModel)
            .loot((p, b) -> p.add(
                    b, p.createSingleItemTable(INDUSTRIAL_TRUSS.get())
                            .withPool(p.applyExplosionCondition(
                                    AllBlocks.SHAFT.get(), LootPool.lootPool()
                                            .setRolls(ConstantValue.exactly(1.0F))
                                            .add(LootItem.lootTableItem(AllBlocks.SHAFT.get()))
                            ))
            ))
            .addLayer(() -> RenderType::cutout)
            .register();

    public static final BlockEntry<TrussPipeBlock> INDUSTRIAL_TRUSS_ENCASED_PIPE = CreateBitsnBobs.REGISTRATE.block(
                    "industrial_truss_encased_pipe",
                    TrussPipeBlock::new
            )
            .properties(p -> p.mapColor(MapColor.METAL)
                    .strength(0.1f, 6.0f)
                    .sound(SoundType.METAL)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
                    .noOcclusion()
            )
            .transform(TagGen.pickaxeOnly())
            .blockstate(TrussBlockStateGen::trussEncasedPipeModel)
            .onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::withAO))
            .loot((p, b) -> p.add(
                    b, p.createSingleItemTable(INDUSTRIAL_TRUSS.get())
                            .withPool(p.applyExplosionCondition(
                                    AllBlocks.FLUID_PIPE.get(), LootPool.lootPool()
                                            .setRolls(ConstantValue.exactly(1.0F))
                                            .add(LootItem.lootTableItem(AllBlocks.FLUID_PIPE.get()))
                            ))
            ))
            .addLayer(() -> RenderType::cutout)
            .transform(EncasingRegistry.addVariantTo(AllBlocks.FLUID_PIPE))
            .register();

    public static void register() {
    }

}


