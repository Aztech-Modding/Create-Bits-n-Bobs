package com.kipti.bnb.registry.content;

import com.kipti.bnb.registry.content.blocks.BnbBracketBlocks;
import com.kipti.bnb.registry.content.blocks.BnbKineticBlocks;
import com.kipti.bnb.registry.content.blocks.BnbTrinketBlocks;
import com.kipti.bnb.registry.content.blocks.deco.BnbDecorativeBlocks;
import com.kipti.bnb.registry.content.blocks.encased.BnbEncasedListBlocks;
import com.kipti.bnb.registry.content.blocks.encased.BnbExtraEncasedBlocks;
import com.kipti.bnb.registry.content.blocks.encased.BnbSpecialEncasedBlocks;

public class BnbBlocksBootstrap {

    public static void register() {
        //Item blocks
        BnbKineticBlocks.register();
        BnbTrinketBlocks.register();
        BnbBracketBlocks.register();

        BnbDecorativeBlocks.register();

        //Non item blocks
        BnbEncasedListBlocks.register();
        BnbExtraEncasedBlocks.register();
        BnbSpecialEncasedBlocks.register();
    }

}


