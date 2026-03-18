package com.kipti.bnb.registry.content.blocks.encased;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.decoration.grating.GratingEncasedShaftBlock;
import com.kipti.bnb.content.decoration.grating.GratingPanelCTBehaviour;
import com.kipti.bnb.foundation.client.BnbBlockStateGen;
import com.kipti.bnb.registry.client.BnbSpriteShifts;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

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
            .blockstate((c, p) ->
                    BnbBlockStateGen.directionalMixedUvLockBlock(c, p,
                            p.models().getExistingFile(CreateBitsnBobs.asResource("block/industrial_grating/panel")),
                            p.models().getExistingFile(CreateBitsnBobs.asResource("block/industrial_grating/panel_side"))
                    ))
            .onRegister(connectedTextures(() -> new GratingPanelCTBehaviour(BnbSpriteShifts.INDUSTRIAL_GRATING)))
            .addLayer(() -> RenderType::cutout)
            .transform(EncasingRegistry.addVariantTo(AllBlocks.SHAFT))
            .register();

    public static void register() {
    }

}
