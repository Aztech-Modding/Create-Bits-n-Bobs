package com.kipti.bnb.registry.content.blocks;

import com.simibubi.create.AllTags;
import com.simibubi.create.content.decoration.bracket.BracketBlock;
import com.simibubi.create.content.decoration.bracket.BracketBlockItem;
import com.simibubi.create.content.decoration.bracket.BracketGenerator;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.SoundType;

import static com.kipti.bnb.CreateBitsnBobs.REGISTRATE;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class BnbBracketBlocks {

    public static final BlockEntry<BracketBlock> WEATHERED_METAL_BRACKET = REGISTRATE.block("weathered_metal_bracket", BracketBlock::new)
            .blockstate(new BracketGenerator("weathered_metal")::generate)
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .transform(pickaxeOnly())
            .item(BracketBlockItem::new)
            .tag(AllTags.AllItemTags.INVALID_FOR_TRACK_PAVING.tag)
            .transform(BracketGenerator.itemModel("weathered_metal"))
            .register();

    public static void register() {
    }

}
