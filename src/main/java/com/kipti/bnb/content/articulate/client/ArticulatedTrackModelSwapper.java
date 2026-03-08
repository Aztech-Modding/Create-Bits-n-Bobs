package com.kipti.bnb.content.articulate.client;

import com.simibubi.create.content.trains.track.ITrackBlock;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.Map;

@EventBusSubscriber
public final class ArticulatedTrackModelSwapper {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onModelBake(final ModelEvent.ModifyBakingResult event) {
        final Map<ModelResourceLocation, BakedModel> models = event.getModels();

        for (final Block block : BuiltInRegistries.BLOCK) {
            if (!(block instanceof ITrackBlock)) {
                continue;
            }

            final ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
            for (final BlockState state : block.getStateDefinition().getPossibleStates()) {
                final ModelResourceLocation location = BlockModelShaper.stateToModelLocation(blockId, state);
                final BakedModel existingModel = models.get(location);
                if (existingModel == null || existingModel instanceof ArticulatedTrackModel) {
                    continue;
                }

                models.put(location, new ArticulatedTrackModel(existingModel));
            }
        }
    }

}
