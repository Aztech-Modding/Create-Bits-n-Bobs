package com.kipti.bnb.registry.compat;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CStress;
import com.kipti.bnb.registry.content.blocks.BnbBlocksBootstrap;

public class BnbCreateStresses {

    public static void registerRedirects() {
        BlockStressValues.IMPACTS.registerProvider((p) -> {
            if (BnbBlocksBootstrap.CHAIN_PULLEY.is(p)) {
                final CStress stress = AllConfigs.server().kinetics.stressValues;
                return stress.getImpact(AllBlocks.ROPE_PULLEY.get());
            }
            return null;
        });
    }

}

