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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@Mod(value = CreateBitsnBobs.MOD_ID, dist = Dist.CLIENT)
public class CreateBitsnBobsClient {

    public CreateBitsnBobsClient(final ModContainer container) {
        final IEventBus eventBus = container.getEventBus();

        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        eventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
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

    @EventBusSubscriber(Dist.CLIENT)
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
            final Supplier<IConfigScreenFactory> configScreen = () -> (mc, previousScreen) -> new BaseConfigScreen(previousScreen, CreateBitsnBobs.MOD_ID);
            createContainer.registerExtensionPoint(IConfigScreenFactory.class, configScreen);
        }
    }

}