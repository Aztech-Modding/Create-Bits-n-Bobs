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
        if (event.getRegistry() == Registries.BLOCK) {
            for (Map.Entry<ResourceLocation, ResourceLocation> entry : ChainIdMigrations.BLOCK_RENAMES.entrySet())
                event.getRegistry().addAlias(entry.getKey(), entry.getValue());
        }
        if (event.getRegistry() == Registries.BLOCK_ENTITY_TYPE) {
            for (Map.Entry<ResourceLocation, ResourceLocation> entry : ChainIdMigrations.BLOCK_ENTITY_RENAMES.entrySet())
                event.getRegistry().addAlias(entry.getKey(), entry.getValue());
            // cogwheel_chain is context-dependent (flanged vs. non-flanged) but addAlias can't
            // be context-aware. Schematics are handled correctly by StructureTemplateMigrationMixin.
            // For world saves, fall back to create:simple_kinetic; the placed block's own
            // getBlockEntityType() will still produce the right BE after the block alias is applied.
            event.getRegistry().addAlias(ChainIdMigrations.COGWHEEL_CHAIN_BE, ChainIdMigrations.CREATE_SIMPLE_KINETIC);
        }
    }

}
