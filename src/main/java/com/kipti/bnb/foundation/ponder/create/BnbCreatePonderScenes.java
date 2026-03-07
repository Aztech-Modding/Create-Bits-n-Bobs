package com.kipti.bnb.foundation.ponder.create;

import com.kipti.bnb.registry.content.blocks.BnbKineticBlocks;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.simibubi.create.infrastructure.ponder.scenes.PulleyScenes;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class BnbCreatePonderScenes {

    public static void register(final PonderSceneRegistrationHelper<ResourceLocation> helper) {
        final PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.forComponents(BnbKineticBlocks.CHAIN_PULLEY)
                .addStoryBoard("rope_pulley/anchor", PulleyScenes::movement, AllCreatePonderTags.KINETIC_APPLIANCES, AllCreatePonderTags.MOVEMENT_ANCHOR)
                .addStoryBoard("rope_pulley/modes", PulleyScenes::movementModes)
                .addStoryBoard("rope_pulley/multi_rope", PulleyScenes::multiRope)
                .addStoryBoard("rope_pulley/attachment", PulleyScenes::attachment);
    }

}

