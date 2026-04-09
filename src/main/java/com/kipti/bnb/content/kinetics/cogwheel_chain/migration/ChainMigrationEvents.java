package com.kipti.bnb.content.kinetics.cogwheel_chain.migration;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Map;

@EventBusSubscriber
public class ChainMigrationEvents {

    @SubscribeEvent
    public static void onRegisterAliasEvent(final RegisterEvent event) {
        if (event.getRegistry().key() == Registries.BLOCK) {
            for (final Map.Entry<ResourceLocation, ResourceLocation> entry : ChainIdMigrations.BLOCK_RENAMES.entrySet())
                event.getRegistry().addAlias(entry.getKey(), entry.getValue());

        }
        if (event.getRegistry().key() == Registries.BLOCK_ENTITY_TYPE) {
            for (final Map.Entry<ResourceLocation, ResourceLocation> entry : ChainIdMigrations.BLOCK_ENTITY_RENAMES.entrySet())
                event.getRegistry().addAlias(entry.getKey(), entry.getValue());
        }
    }

}
