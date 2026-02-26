package com.kipti.bnb.registry.content;

import com.kipti.bnb.registry.content.blocks.*;
import com.kipti.bnb.registry.content.blocks.deco.BnbDecorativeBlocks;

public class BnbBlocksBootstrap {

    public static void register() {
        //Item blocks
        BnbKineticBlocks.register();
        BnbTrinketBlocks.register();
        BnbBracketBlocks.register();

        //Non item blocks
        BnbEncasedBlocks.register();
        BnbSpecialEncasedBlocks.register();
        //Deco
        BnbDecorativeBlocks.register();
    }

}


