package com.kipti.bnb;

import com.kipti.bnb.content.kinetics.cogwheel_chain.riding.ServerCogwheelChainRidingHandler;
import com.kipti.bnb.content.trinkets.light.headlamp.HeadlampBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber
public class CommonEvents {

    @SubscribeEvent
    public static void onServerTick(final ServerTickEvent.Post event) {
        ServerCogwheelChainRidingHandler.tick();
    }

    @SubscribeEvent
    public static void onServerStopping(final ServerStoppingEvent event) {
        ServerCogwheelChainRidingHandler.reset();
    }

    @EventBusSubscriber
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
            HeadlampBlockEntity.registerCapabilities(event);
        }
    }
}

