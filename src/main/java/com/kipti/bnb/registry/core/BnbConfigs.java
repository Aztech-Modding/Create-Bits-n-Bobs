package com.kipti.bnb.registry.core;

import com.kipti.bnb.foundation.config.BnbCommonConfig;
import com.kipti.bnb.foundation.config.BnbServerConfig;
import net.createmod.catnip.config.ConfigBase;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Create's {@link com.simibubi.create.infrastructure.config.AllConfigs} equivalent for Bits 'n' Bobs.
 */
@EventBusSubscriber
public class BnbConfigs {

    private static final Map<ModConfig.Type, ConfigBase> CONFIGS = new EnumMap<>(ModConfig.Type.class);

    //    private static CClient client;
    private static BnbCommonConfig common;
    private static BnbServerConfig server;

//    public static CClient client() {
//        return client;
//    }

    public static BnbCommonConfig common() {
        return common;
    }

    public static BnbServerConfig server() {
        return server;
    }

    public static ConfigBase byType(final ModConfig.Type type) {
        return CONFIGS.get(type);
    }

    private static <T extends ConfigBase> T register(final Supplier<T> factory, final ModConfig.Type side) {
        final Pair<T, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(builder -> {
            final T config = factory.get();
            config.registerAll(builder);
            return config;
        });

        final T config = specPair.getLeft();
        config.specification = specPair.getRight();
        CONFIGS.put(side, config);
        return config;
    }

    public static void register(final ModLoadingContext context, final ModContainer container) {
//        client = register(CClient::new, ModConfig.Type.CLIENT);
        common = register(BnbCommonConfig::new, ModConfig.Type.COMMON);
        server = register(BnbServerConfig::new, ModConfig.Type.SERVER);

        for (final Map.Entry<ModConfig.Type, ConfigBase> pair : CONFIGS.entrySet())
            container.registerConfig(pair.getKey(), pair.getValue().specification);
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        for (final ConfigBase config : CONFIGS.values())
            if (config.specification == event.getConfig()
                    .getSpec())
                config.onLoad();
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading event) {
        for (final ConfigBase config : CONFIGS.values())
            if (config.specification == event.getConfig()
                    .getSpec())
                config.onReload();
    }

}

