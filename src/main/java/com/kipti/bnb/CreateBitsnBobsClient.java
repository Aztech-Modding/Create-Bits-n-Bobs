package com.kipti.bnb;

import com.kipti.bnb.content.girder_strut.GirderStrutModelManipulator;
import com.kipti.bnb.foundation.ponder.BnbPonderPlugin;
import com.kipti.bnb.registry.BnbConfigs;
import com.kipti.bnb.registry.BnbPartialModels;
import com.kipti.bnb.registry.BnbSpriteShifts;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;

public class CreateBitsnBobsClient {

    public static void setup(final FMLJavaModLoadingContext context) {
        final IEventBus eventBus = context.getModEventBus();
        eventBus.addListener(CreateBitsnBobsClient::onClientSetup);
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(CreateBitsnBobsClient::clientInit);
    }

    private static void clientInit() {
        PonderIndex.addPlugin(new BnbPonderPlugin());

        BnbPartialModels.register();
        BnbSpriteShifts.register();

        BaseConfigScreen.setDefaultActionFor(CreateBitsnBobs.MOD_ID, base -> base
                .withButtonLabels(null, "Feature Settings", "Balancing Settings")
                .withSpecs(null, BnbConfigs.common().specification, BnbConfigs.server().specification)
        );
    }

    private static class BnbResourceReloadListener implements ResourceManagerReloadListener {
        @Override
        public void onResourceManagerReload(@NotNull final ResourceManager resourceManager) {
            GirderStrutModelManipulator.invalidateMeshes();
        }
    }

    @Mod.EventBusSubscriber(Dist.CLIENT)
    private static class ModBusEvents {

        @SubscribeEvent
        public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(new BnbResourceReloadListener());
        }

        @SubscribeEvent
        public static void onLoadComplete(final FMLLoadCompleteEvent event) {
            final ModContainer createContainer = ModList.get()
                    .getModContainerById(CreateBitsnBobs.MOD_ID)
                    .orElseThrow(() -> new IllegalStateException("Create mod container missing on LoadComplete"));
            createContainer.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                    (mc, previousScreen) -> new BaseConfigScreen(previousScreen, CreateBitsnBobs.MOD_ID)));
        }
    }

}