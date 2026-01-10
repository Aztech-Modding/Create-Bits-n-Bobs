package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.decoration.grating.GratingBlock;
import com.kipti.bnb.content.decoration.grating.GratingPanelBlock;
import com.kipti.bnb.content.decoration.grating.GratingPanelCTBehaviour;
import com.kipti.bnb.content.decoration.truss.AlternatingTrussBlock;
import com.kipti.bnb.content.decoration.truss.TrussBlock;
import com.kipti.bnb.foundation.BnbBlockStateGen;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;

public class BnbDecoBlocks {

    static {
        CreateBitsnBobs.REGISTRATE.setCreativeTab(BnbCreativeTabs.DECO_CREATIVE_TAB);
    }

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

    public static final BlockEntry<TrussBlock> INDUSTRIAL_TRUSS = CreateBitsnBobs.REGISTRATE.block("industrial_truss", TrussBlock::new)
            .properties(p -> p.mapColor(MapColor.METAL)
                    .strength(0.1f, 6.0f)
                    .sound(SoundType.METAL)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
                    .noOcclusion()
            )
            .transform(TagGen.pickaxeOnly())
            .blockstate((c, p) -> BnbBlockStateGen.axisModel(c, p))
            .onRegister(connectedTextures(() -> new GratingPanelCTBehaviour(BnbSpriteShifts.INDUSTRIAL_GRATING)))
            .addLayer(() -> RenderType::cutout)
            .simpleItem()
            .register();

    public static final BlockEntry<AlternatingTrussBlock> ALTERNATING_INDUSTRIAL_TRUSS = CreateBitsnBobs.REGISTRATE.block("alternating_industrial_truss", AlternatingTrussBlock::new)
            .properties(p -> p.mapColor(MapColor.METAL)
                    .strength(0.1f, 6.0f)
                    .sound(SoundType.METAL)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)
                    .noOcclusion()
            )
            .transform(TagGen.pickaxeOnly())
            .blockstate((c, p) -> BnbBlockStateGen.alternatingTrussModel(c, p))
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
