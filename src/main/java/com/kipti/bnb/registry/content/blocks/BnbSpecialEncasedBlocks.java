package com.kipti.bnb.registry.content.blocks;

import com.kipti.bnb.content.kinetics.encased_blocks.BnbEncasedCogwheelBlock;
import com.kipti.bnb.content.kinetics.encased_blocks.BnbEncasedShaftBlock;
import com.kipti.bnb.foundation.BnbBuilderTransformers;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.material.MapColor;

import static com.kipti.bnb.CreateBitsnBobs.REGISTRATE;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;

public class BnbSpecialEncasedBlocks {

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

    public static void register() {
    }

}
