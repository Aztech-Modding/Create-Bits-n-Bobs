package com.kipti.bnb;

import com.kipti.bnb.content.decoration.light.headlamp.HeadlampBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class CommonEvents {

    @EventBusSubscriber
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
            HeadlampBlockEntity.registerCapabilities(event);
        }
    }
}
