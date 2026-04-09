package com.kipti.bnb.registry.content.blocks;

import com.cake.azimuth.lang.IncludeLangDefaults;
import com.cake.azimuth.lang.LangDefault;
import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.kinetics.throttle_lever.ThrottleLeverBlock;
import com.kipti.bnb.content.trinkets.chair.ChairBlock;
import com.kipti.bnb.content.trinkets.light.founation.LightBlock;
import com.kipti.bnb.content.trinkets.light.headlamp.HeadlampBlock;
import com.kipti.bnb.content.trinkets.light.headlamp.HeadlampBlockItem;
import com.kipti.bnb.content.trinkets.light.lightbulb.LightbulbBlock;
import com.kipti.bnb.content.trinkets.nixie.foundation.DoubleOrientedBlockModel;
import com.kipti.bnb.content.trinkets.nixie.large_nixie_tube.LargeNixieTubeBlockNixie;
import com.kipti.bnb.content.trinkets.nixie.large_nixie_tube.LargeNixieTubeBlockStateGen;
import com.kipti.bnb.content.trinkets.nixie.nixie_board.NixieBoardBlockNixie;
import com.kipti.bnb.content.trinkets.nixie.nixie_board.NixieBoardBlockStateGen;
import com.kipti.bnb.registry.client.BnbDisplayTargets;
import com.kipti.bnb.registry.client.BnbShapes;
import com.kipti.bnb.registry.core.BnbFeatureFlag;
import com.kipti.bnb.registry.core.BnbTags;
import com.simibubi.create.AllDisplaySources;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.actors.seat.SeatInteractionBehaviour;
import com.simibubi.create.content.contraptions.actors.seat.SeatMovementBehaviour;
import com.simibubi.create.foundation.block.DyedBlockList;
import com.simibubi.create.foundation.block.ItemUseOverrides;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.utility.DyeHelper;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;

import static com.kipti.bnb.CreateBitsnBobs.REGISTRATE;
import static com.kipti.bnb.content.trinkets.chair.ChairBlockStateGen.dyedChair;
import static com.simibubi.create.api.behaviour.display.DisplaySource.displaySource;
import static com.simibubi.create.api.behaviour.display.DisplayTarget.displayTarget;
import static com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour.interactionBehaviour;
import static com.simibubi.create.api.behaviour.movement.MovementBehaviour.movementBehaviour;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.*;

@IncludeLangDefaults({
        @LangDefault(key = "block.bits_n_bobs.headlamp.tooltip.summary", value = "Can be dyed and placed _multiple times in same block_. Useful for trains or fancy signage too!"),
        @LangDefault(key = "block.bits_n_bobs.headlamp.tooltip.condition1", value = "When R-Clicked with empty hand"),
        @LangDefault(key = "block.bits_n_bobs.headlamp.tooltip.behaviour1", value = "Toggles if the lightbulb should be _always on_, irregardless of redstone power."),

        @LangDefault(key = "block.bits_n_bobs.brass_lamp.tooltip.summary", value = "_It's not just a lightbulb_, this one's got a fancy brass casing."),
        @LangDefault(key = "block.bits_n_bobs.brass_lamp.tooltip.condition1", value = "When R-Clicked with empty hand"),
        @LangDefault(key = "block.bits_n_bobs.brass_lamp.tooltip.behaviour1", value = "Toggles if the lightbulb should be _always on_, irregardless of redstone power."),

        @LangDefault(key = "block.bits_n_bobs.lightbulb.tooltip.summary", value = "_It's just a lightbulb_, what do you expect."),
        @LangDefault(key = "block.bits_n_bobs.lightbulb.tooltip.condition1", value = "When R-Clicked with Wrench"),
        @LangDefault(key = "block.bits_n_bobs.lightbulb.tooltip.behaviour1", value = "Toggles the lightbulb _cage variant_."),
        @LangDefault(key = "block.bits_n_bobs.lightbulb.tooltip.condition2", value = "When R-Clicked with empty hand"),
        @LangDefault(key = "block.bits_n_bobs.lightbulb.tooltip.behaviour2", value = "Toggles if the lightbulb should be _always on_, irregardless of redstone power."),

        @LangDefault(key = "block.bits_n_bobs.chair.tooltip.summary", value = "Sit yourself down and enjoy the ride! Will anchor a player onto a moving _contraption_. Even _fancier than a seat_ for static furniture too! Comes in a variety of colours. Will form _corners_ and _flat backs_ when placed against other chairs and blocks accordingly"),
        @LangDefault(key = "block.bits_n_bobs.chair.tooltip.condition1", value = "Right click on Chair"),
        @LangDefault(key = "block.bits_n_bobs.chair.tooltip.behaviour1", value = "Sits the player on the _Chair_. Press L-shift to leave the _Chair_."),
})
public class BnbTrinketBlocks {
    public static final BlockEntry<NixieBoardBlockNixie> NIXIE_BOARD = REGISTRATE.block(
                    "nixie_board",
                    p -> new NixieBoardBlockNixie(
                            p,
                            null
                    )
            )
            .transform(nixieBoard())
            .item()
            .model((c, p) -> p.withExistingParent(
                    c.getName(),
                    CreateBitsnBobs.asResource("block/nixie_board/nixie_board_single")
            ))
            .build()
            .register();
    public static final DyedBlockList<NixieBoardBlockNixie> DYED_NIXIE_BOARD = new DyedBlockList<>(colour -> {
        String colourName = colour.getSerializedName();
        return REGISTRATE.block(colourName + "_nixie_board", p -> new NixieBoardBlockNixie(p, colour))
                .transform(nixieBoard())
                .register();
    });
    public static final BlockEntry<LargeNixieTubeBlockNixie> LARGE_NIXIE_TUBE = REGISTRATE.block(
                    "large_nixie_tube",
                    p -> new LargeNixieTubeBlockNixie(
                            p,
                            null
                    )
            )
            .transform(largeNixieTube())
            .item()
            .model((c, p) -> p.withExistingParent(
                    c.getName(),
                    CreateBitsnBobs.asResource("block/large_nixie_tube/large_nixie_tube")
            ))
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
            .blockstate((c, p) -> p.directionalBlock(
                    c.get(),
                    (state) -> p.models().getExistingFile(CreateBitsnBobs.asResource(
                            "block/lightbulb/lightbulb" + (state.getValue(LightbulbBlock.CAGE) ? "" : "_uncaged") + (LightBlock.shouldUseOnLightModel(
                                    state) ? "_on" : "")
                    ))
            ))
            .properties(p -> p
                    .noOcclusion()
                    .lightLevel(LightBlock::getLightLevel)
                    .emissiveRendering((state, level, pos) -> state.getValue(LightBlock.POWER) > 0)
                    .forceSolidOn())
            .addLayer(() -> RenderType::translucent)
            .item()
            .model((c, p) -> p.withExistingParent(
                    c.getName(),
                    CreateBitsnBobs.asResource("block/lightbulb/lightbulb_uncaged")
            ))
            .build()
            .register();
    public static final BlockEntry<HeadlampBlock> HEADLAMP = REGISTRATE.block("headlamp", HeadlampBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.simpleBlock(
                    c.get(),
                    p.models().getExistingFile(CreateBitsnBobs.asResource(
                            "block/headlamp/headlight_block"
                    ))
            ))
//            .onRegister(CreateRegistrate.blockModel(() -> HeadlampModelBuilder::new))
            .properties(p -> p
                    .noOcclusion()
                    .lightLevel(LightBlock::getLightLevel)
                    .emissiveRendering(LightBlock::isEmissive)
                    .mapColor(DyeColor.ORANGE)
                    .forceSolidOn())
            .addLayer(() -> RenderType::translucent)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .item(HeadlampBlockItem::new)
            .model((c, p) -> p.withExistingParent(c.getName(), CreateBitsnBobs.asResource("block/headlamp/headlight")))
            .build()
            .register();
    public static final BlockEntry<LightBlock> BRASS_LAMP = REGISTRATE.block(
                    "brass_lamp",
                    (p) -> new LightBlock(
                            p,
                            BnbShapes.BRASS_LAMP_SHAPE,
                            true
                    )
            )
            .initialProperties(SharedProperties::softMetal)
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.directionalBlock(
                    c.get(),
                    (state) -> p.models().getExistingFile(CreateBitsnBobs.asResource(
                            "block/brass_lamp/brass_lamp" + (LightBlock.shouldUseOnLightModel(state) ? "_on" : "")
                    ))
            ))
            .properties(p -> p
                    .noOcclusion()
                    .lightLevel(LightBlock::getLightLevel)
                    .emissiveRendering((state, level, pos) -> state.getValue(LightBlock.POWER) > 0)
                    .mapColor(DyeColor.ORANGE)
                    .forceSolidOn())
            .addLayer(() -> RenderType::translucent)
            .item()
            .model((c, p) -> p.withExistingParent(
                    c.getName(),
                    CreateBitsnBobs.asResource("block/brass_lamp/brass_lamp")
            ))
            .build()
            .register();
    public static final BlockEntry<ThrottleLeverBlock> THROTTLE_LEVER =
            REGISTRATE.block("throttle_lever", ThrottleLeverBlock::new)
                    .initialProperties(() -> Blocks.LEVER)
                    .transform(axeOrPickaxe())
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
                    .blockstate((c, p) -> {
                        p.getVariantBuilder(c.get()).forAllStatesExcept(
                                state -> {
                                    AttachFace face = state.getValue(ThrottleLeverBlock.FACE);
                                    Direction facing = state.getValue(ThrottleLeverBlock.FACING);

                                    ModelFile model = AssetLookup.partialBaseModel(c, p);

                                    int xRot = face == AttachFace.FLOOR ? 0 : face == AttachFace.WALL ? 90 : 180;
                                    int yRot = (int) facing.toYRot();
                                    if (face == AttachFace.CEILING) {
                                        yRot = (yRot + 180) % 360;
                                    }

                                    return ConfiguredModel.builder()
                                            .modelFile(model)
                                            .rotationX(xRot)
                                            .rotationY(yRot)
                                            .build();
                                }, BlockStateProperties.POWER
                        );
                    })
                    .onRegister(ItemUseOverrides::addBlock)
                    .item()
                    .transform(customItemModel())
                    .addLayer(() -> RenderType::cutout)
                    .register();

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
                            .save(
                                    p.withConditions(BnbFeatureFlag.CHAIRS.getDataCondition()),
                                    CreateBitsnBobs.asResource("crafting/" + c.getName())
                            );
                    ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, c.get())
                            .requires(colour.getTag())
                            .requires(BnbTags.BnbItemTags.CHAIRS.tag)
                            .unlockedBy("has_seat", RegistrateRecipeProvider.has(BnbTags.BnbItemTags.CHAIRS.tag))
                            .save(
                                    p.withConditions((BnbFeatureFlag.CHAIRS.getDataCondition())),
                                    CreateBitsnBobs.asResource("crafting/" + c.getName() + "_from_other_chair")
                            );
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

    public static void register() {
    }

}
