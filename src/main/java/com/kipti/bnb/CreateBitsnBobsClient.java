package com.kipti.bnb;

import com.cake.azimuth.ponder.PonderForeignLabelRegistry;
import com.cake.struts.compat.flywheel.StrutsFlywheelCompatLoader;
import com.cake.struts.content.StrutModelManipulator;
import com.kipti.bnb.content.trinkets.light.headlamp.rendering.pipeline.block_entity.HeadlampRenderCache;
import com.kipti.bnb.content.trinkets.light.headlamp.rendering.pipeline.block_entity.HeadlampVertexBufferCache;
import com.kipti.bnb.foundation.ponder.BnbPonderPlugin;
import com.kipti.bnb.registry.client.BnbInstanceTypes;
import com.kipti.bnb.registry.client.BnbPartialModels;
import com.kipti.bnb.registry.client.BnbSpriteShifts;
import com.kipti.bnb.registry.content.BnbBlockEntities;
import com.kipti.bnb.registry.content.blocks.deco.BnbStrutDefinitions;
import com.kipti.bnb.registry.core.BnbConfigs;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.function.Supplier;

@Mod(value = CreateBitsnBobs.MOD_ID, dist = Dist.CLIENT)
public class CreateBitsnBobsClient {

    public CreateBitsnBobsClient(final ModContainer container) {
        final IEventBus eventBus = container.getEventBus();

        BnbInstanceTypes.init();

        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        eventBus.addListener(this::onClientSetup);
        eventBus.addListener(this::registerAdditionalModels);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(CreateBitsnBobsClient::clientInit);
    }

    private static void clientInit() {
        PonderIndex.addPlugin(new BnbPonderPlugin());
        PonderForeignLabelRegistry.register("bits_n_bobs", "BITS N BOBS");

        BnbPartialModels.register();
        BnbSpriteShifts.register();

        StrutsFlywheelCompatLoader.registerStrutVisual(BnbBlockEntities.GIRDER_STRUT.get());

        BaseConfigScreen.setDefaultActionFor(CreateBitsnBobs.MOD_ID, base -> base
                .withButtonLabels(null, "Feature Settings", "Balancing Settings")
                .withSpecs(null, BnbConfigs.common().specification, BnbConfigs.server().specification)
        );
    }

    private void registerAdditionalModels(final ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(BnbStrutDefinitions.NORMAL_MODEL.segmentModelLocation()));
        event.register(ModelResourceLocation.standalone(BnbStrutDefinitions.WEATHERED_MODEL.segmentModelLocation()));
        event.register(ModelResourceLocation.standalone(BnbStrutDefinitions.WOODEN_MODEL.segmentModelLocation()));
        event.register(ModelResourceLocation.standalone(BnbStrutDefinitions.CABLE_MODEL.segmentModelLocation()));
    }

    public static void invalidateRenderers() {
        StrutModelManipulator.invalidateMeshes();
        HeadlampRenderCache.clearCaches();
        HeadlampVertexBufferCache.clear();
    }

    @EventBusSubscriber(Dist.CLIENT)
    private static class ModBusEvents {
        @SubscribeEvent
        public static void onLoadComplete(final FMLLoadCompleteEvent event) {
            final ModContainer createContainer = ModList.get()
                    .getModContainerById(CreateBitsnBobs.MOD_ID)
                    .orElseThrow(() -> new IllegalStateException("Create mod container missing on LoadComplete"));
            final Supplier<IConfigScreenFactory> configScreen = () -> (mc, previousScreen) -> new BaseConfigScreen(previousScreen, CreateBitsnBobs.MOD_ID);
            createContainer.registerExtensionPoint(IConfigScreenFactory.class, configScreen);
        }
    }

}
