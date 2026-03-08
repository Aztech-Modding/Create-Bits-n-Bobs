package com.kipti.bnb.foundation.ponder;

import com.kipti.bnb.foundation.ponder.scenes.CogwheelChainScenes;
import com.kipti.bnb.foundation.ponder.scenes.DyedPipeScenes;
import com.kipti.bnb.foundation.ponder.scenes.NixieDisplayScenes;
import com.kipti.bnb.registry.content.blocks.BnbKineticBlocks;
import com.kipti.bnb.registry.content.blocks.BnbTrinketBlocks;
import com.simibubi.create.AllBlocks;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class BnbPonderScenes {

    public static void register(final PonderSceneRegistrationHelper<ResourceLocation> helper) {
        final PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        helper.addStoryBoard(BuiltInRegistries.ITEM.getKey(Items.CHAIN), "chain_cog/flat", CogwheelChainScenes::flatCogwheelChain);
        helper.addStoryBoard(BuiltInRegistries.ITEM.getKey(Items.CHAIN), "chain_cog/axis_change", CogwheelChainScenes::changingAxisCogwheelChain);
//        helper.addStoryBoard(BuiltInRegistries.ITEM.getKey(Items.CHAIN), "chain_cog/pathing_behaviour", CogwheelChainScenes::cogwheelChainPathingBehaviour);

        HELPER.forComponents(AllBlocks.COGWHEEL, AllBlocks.LARGE_COGWHEEL, BnbKineticBlocks.SMALL_FLANGED_COGWHEEL, BnbKineticBlocks.LARGE_FLANGED_COGWHEEL)
                .addStoryBoard("chain_cog/flat", CogwheelChainScenes::flatCogwheelChain)
                .addStoryBoard("chain_cog/axis_change", CogwheelChainScenes::changingAxisCogwheelChain);
//                .addStoryBoard("chain_cog/pathing_behaviour", CogwheelChainScenes::cogwheelChainPathingBehaviour);
        HELPER.forComponents(AllBlocks.FLUID_PIPE)
                .addStoryBoard("dyed_pipes/dyed_pipes", DyedPipeScenes::dyedPipes);

        HELPER.forComponents(BnbTrinketBlocks.NIXIE_BOARD)
                .addStoryBoard("nixie/nixie_board", NixieDisplayScenes::nixieBoard);

        HELPER.forComponents(BnbTrinketBlocks.LARGE_NIXIE_TUBE)
                .addStoryBoard("nixie/large_nixie_tube", NixieDisplayScenes::largeNixieTube);
    }
}

