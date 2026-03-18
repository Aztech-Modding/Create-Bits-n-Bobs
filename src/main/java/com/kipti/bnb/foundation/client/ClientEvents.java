package com.kipti.bnb.foundation.client;

import com.cake.struts.content.StrutPlacementEffects;
import com.kipti.bnb.content.decoration.weathered_girder.WeatheredGirderWrenchBehaviour;
import com.kipti.bnb.content.kinetics.cogwheel_chain.placement.CogwheelChainPlacementEffect;
import com.kipti.bnb.content.trinkets.light.headlamp.rendering.pipeline.block_entity.HeadlampVertexBufferCache;
import com.kipti.bnb.foundation.generation.PonderflatGeneratorSettings;
import com.kipti.bnb.foundation.generation.PonderflatLevelSource;
import com.kipti.bnb.foundation.generation.editor.PonderflatEditor;
import com.kipti.bnb.registry.worldgen.BnbWorldPresets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterPresetEditorsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onTickPost(final ClientTickEvent.Post event) {
        WeatheredGirderWrenchBehaviour.tick();
        HeadlampVertexBufferCache.tick();
    }

    @SubscribeEvent
    public static void onTickPre(final ClientTickEvent.Pre event) {
        //If in a level, there is a player, and the player is holding a girder strut block item, update the preview
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            StrutPlacementEffects.tick(mc.player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onTickPre(final ClientTickEvent.Post event) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            CogwheelChainPlacementEffect.tick(mc.player);
        }
    }

    @SubscribeEvent
    public static void modify(final ItemTooltipEvent context) {
//        if (context.getItemStack().is(AllBlocks.COGWHEEL.asItem()) ||
//                context.getItemStack().is(AllBlocks.LARGE_COGWHEEL.asItem())) {
//            context.getToolTip().add(1, Component.translatable("tooltip.bits_n_bobs.new_ponder_notification")
//                    .withColor(FontHelper.Palette.STANDARD_CREATE.primary().getColor().getValue()));
//        }
    }

    @SubscribeEvent
    public static void registerPresetEditors(final RegisterPresetEditorsEvent event) {
        event.register(BnbWorldPresets.PONDER, (lastScreen, context) -> new PonderflatEditor(
                        lastScreen, context,
                        p_267859_ -> lastScreen.getUiState().updateDimensions(ponderflatWorldConfigurator(p_267859_))
                )
        );
    }

    private static WorldCreationContext.DimensionsUpdater ponderflatWorldConfigurator(final PonderflatGeneratorSettings settings) {
        return (p_255454_, p_255455_) -> {
            final Holder.Reference<Biome> voidBiome = p_255454_.registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.THE_VOID);
            final ChunkGenerator chunkgenerator = new PonderflatLevelSource(voidBiome, settings);
            return p_255455_.replaceOverworldGenerator(p_255454_, chunkgenerator);
        };
    }

}

