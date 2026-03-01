package com.kipti.bnb.content.decoration.girder_strut.structure;

import net.minecraft.world.level.block.Block;

/**
 * Structure block for the in-between segments of the girder strut.
 * This block is invisible but acts as a collision box.
 * The collision box has to be generated see {@link GirderStrutStructureShapes} for more details.
 * Whether this block exists or not is determined by the {@link GirderStrutStructureShapes} as it tracks all girder struts
 * (and this block may represent multiple girder struts).
 * Replaceable, so the player can build right through, but breaking this should break the connections within this block.
 */
public class GirderStrutStructureBlock extends Block {

    public GirderStrutStructureBlock(final Properties properties) {
        super(properties);
    }

}
