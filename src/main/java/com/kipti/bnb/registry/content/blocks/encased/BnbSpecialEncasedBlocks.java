package com.kipti.bnb.registry.content.blocks.encased;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.decoration.grating.GratingBlockStateGen;
import com.kipti.bnb.content.decoration.grating.GratingEncasedPipeBlock;
import com.kipti.bnb.content.decoration.grating.GratingEncasedShaftBlock;
import com.kipti.bnb.content.decoration.grating.GratingPanelCTBehaviour;
import com.kipti.bnb.registry.client.BnbSpriteShifts;
import com.kipti.bnb.registry.content.blocks.deco.BnbDecorativeBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.content.fluids.PipeAttachmentModel;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;

public class BnbSpecialEncasedBlocks {

    public static final BlockEntry<GratingEncasedShaftBlock> INDUSTRIAL_GRATING_PANEL = CreateBitsnBobs.REGISTRATE.block("industrial_grating_panel_encased_shaft", GratingEncasedShaftBlock::new)
            .properties(p -> p.mapColor(MapColor.METAL)
                    .noOcclusion()
                    .strength(0.1f, 6.0f)
                    .sound(SoundType.METAL)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
            )
            .transform(TagGen.pickaxeOnly())
            .blockstate(GratingBlockStateGen::gratingEncasedShaftBlock)
            .onRegister(connectedTextures(() -> new GratingPanelCTBehaviour(BnbSpriteShifts.INDUSTRIAL_GRATING)))
            .onRegister(connectedTextures(() -> new GratingPanelCTBehaviour(BnbSpriteShifts.INDUSTRIAL_GRATING_CUTOUT)))
            .addLayer(() -> RenderType::cutout)
            .transform(EncasingRegistry.addVariantTo(AllBlocks.SHAFT))
            .register();

    public static final BlockEntry<GratingEncasedPipeBlock> INDUSTRIAL_GRATING_PANEL_PIPE = CreateBitsnBobs.REGISTRATE.block("industrial_grating_panel_encased_pipe", GratingEncasedPipeBlock::new)
            .properties(p -> p.mapColor(MapColor.METAL)
                    .noOcclusion()
                    .strength(0.1f, 6.0f)
                    .sound(SoundType.METAL)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
            )
            .transform(TagGen.pickaxeOnly())
            .blockstate(GratingBlockStateGen::gratingEncasedPipeBlock)
            .onRegister(connectedTextures(() -> new GratingPanelCTBehaviour(BnbSpriteShifts.INDUSTRIAL_GRATING)))
            .onRegister(connectedTextures(() -> new GratingPanelCTBehaviour(BnbSpriteShifts.INDUSTRIAL_GRATING_PIPE_CUTOUT)))
            .onRegister(CreateRegistrate.blockModel(() -> PipeAttachmentModel::withAO))
            .loot((p, b) -> p.add(
                    b, p.createSingleItemTable(BnbDecorativeBlocks.INDUSTRIAL_GRATING_PANEL.get())
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
