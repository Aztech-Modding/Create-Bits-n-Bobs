package com.kipti.bnb.registry.core;

import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

@EventBusSubscriber
public class BnbRegistries {

    public static final Registry<CogwheelChainType> COGWHEEL_CHAIN_TYPES = new RegistryBuilder<>(BnbResourceKeys.COGWHEEL_CHAIN_TYPE)
            .sync(true)
            .create();

    @SubscribeEvent
    public static void register(final NewRegistryEvent event) {
        event.register(COGWHEEL_CHAIN_TYPES);
    }

}

