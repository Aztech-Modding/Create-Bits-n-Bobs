package com.kipti.bnb.registry.content.blocks;

import com.kipti.bnb.content.kinetics.encased_blocks.cogwheel_chain.BnbEncasedEmptyFlangedGearBlock;
import com.kipti.bnb.content.kinetics.encased_blocks.piston_pole.EncasedPistonExtensionPoleBlock;
import com.kipti.bnb.foundation.BnbBlockStateGen;
import com.kipti.bnb.foundation.BnbBuilderTransformers;
import com.kipti.bnb.foundation.EncasedBlockList;
import com.kipti.bnb.foundation.GenericBlockEntityRenderModels;
import com.kipti.bnb.registry.client.BnbPartialModels;
import com.kipti.bnb.registry.core.BnbTags;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.encasing.EncasableBlock;
import com.simibubi.create.content.decoration.encasing.EncasedCTBehaviour;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import static com.kipti.bnb.CreateBitsnBobs.REGISTRATE;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;

public class BnbEncasedBlocks {

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
            }, false))
            .loot((p, lb) -> p.add(lb, LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(lb)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(EncasedPistonExtensionPoleBlock.EMPTY, false)))
                            .add(LootItem.lootTableItem(AllBlocks.PISTON_EXTENSION_POLE.get().asItem())))
            ))
            .simpleItem()
            .register());

    public static final EncasedBlockList<BnbEncasedEmptyFlangedGearBlock> ENCASED_FLANGED_COGWHEEL = new EncasedBlockList<>(casing ->
            REGISTRATE.block(casing.asId("encased_flanged_cogwheel"), p -> new BnbEncasedEmptyFlangedGearBlock(p, false, casing.getMaterial()))
                    .properties(p -> p.mapColor(MapColor.PODZOL))
                    .transform(BnbBuilderTransformers.casingMaterialCogwheelBase(casing, BnbKineticBlocks.SMALL_EMPTY_FLANGED_COGWHEEL::get, false))
                    .transform(EncasingRegistry.addVariantTo(BnbKineticBlocks.SMALL_EMPTY_FLANGED_COGWHEEL))
                    .onRegister(GenericBlockEntityRenderModels.model(BnbPartialModels.ENCASED_FLANGED_COGWHEEL_BLOCK))
                    .tag(BnbTags.BnbBlockTags.COGWHEEL_CHAIN_NO_SMALL_OFFSET.tag)
                    .register());
    public static final EncasedBlockList<BnbEncasedEmptyFlangedGearBlock> ENCASED_LARGE_FLANGED_COGWHEEL = new EncasedBlockList<>(casing ->
            REGISTRATE.block(casing.asId("encased_large_flanged_cogwheel"), p -> new BnbEncasedEmptyFlangedGearBlock(p, true, casing.getMaterial()))
                    .properties(p -> p.mapColor(MapColor.PODZOL))
                    .transform(BnbBuilderTransformers.casingMaterialCogwheelBase(casing, BnbKineticBlocks.LARGE_EMPTY_FLANGED_COGWHEEL::get, true))
                    .transform(EncasingRegistry.addVariantTo(BnbKineticBlocks.LARGE_EMPTY_FLANGED_COGWHEEL))
                    .onRegister(GenericBlockEntityRenderModels.model(BnbPartialModels.ENCASED_LARGE_FLANGED_COGWHEEL_BLOCK))
                    .register());

    public static void register() {
    }

}
