package com.kipti.bnb.content.decoration.weathered_girder;

import com.google.common.base.Predicates;
import com.kipti.bnb.registry.content.blocks.deco.BnbDecorativeBlocks;
import com.simibubi.create.content.decoration.girder.GirderPlacementHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public class WeatheredGirderPlacementHelper extends GirderPlacementHelper {

    @Override
    public Predicate<ItemStack> getItemPredicate() {
        return BnbDecorativeBlocks.WEATHERED_METAL_GIRDER::isIn;
    }

    @Override
    public Predicate<BlockState> getStatePredicate() {
        return Predicates.or(BnbDecorativeBlocks.WEATHERED_METAL_GIRDER::has, BnbDecorativeBlocks.WEATHERED_METAL_GIRDER::has);
    }

}

