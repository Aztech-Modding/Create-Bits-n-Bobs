package com.kipti.bnb.content.decoration.nixie.large_nixie_tube;

import com.kipti.bnb.CreateBitsnBobs;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;

public class LargeNixieTubeBlockStateGen {

    public static <T extends LargeNixieTubeBlockNixie> void nixieTube(DataGenContext<Block, T> c, RegistrateBlockstateProvider p) {
        p.getVariantBuilder(c.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(p.models().getExistingFile(CreateBitsnBobs.asResource("block/large_nixie_tube/large_nixie_tube")))
                        .build());

    }

}
