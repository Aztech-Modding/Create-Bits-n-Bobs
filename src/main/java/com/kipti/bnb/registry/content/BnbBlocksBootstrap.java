package com.kipti.bnb.registry.content;

import com.kipti.bnb.registry.content.blocks.BnbBracketBlocks;
import com.kipti.bnb.registry.content.blocks.BnbKineticBlocks;
import com.kipti.bnb.registry.content.blocks.BnbTrinketBlocks;
import com.kipti.bnb.registry.content.blocks.deco.BnbDecorativeBlocks;
import com.kipti.bnb.registry.content.blocks.encased.BnbEncasedBlockLists;
import com.kipti.bnb.registry.content.blocks.encased.BnbExtraEncasedBlocks;

public class BnbBlocksBootstrap {

    public static void register() {
        //Item blocks
        BnbKineticBlocks.register();
        BnbTrinketBlocks.register();
        BnbBracketBlocks.register();

        BnbDecorativeBlocks.register();

        //Non item blocks
        BnbEncasedBlockLists.register();
        BnbExtraEncasedBlocks.register();
    }

}


