package com.kipti.bnb.foundation;

import com.kipti.bnb.foundation.command.BnbCommands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber
public class BnbCommonEvents {

    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event) {
        BnbCommands.register(event.getDispatcher());
    }
}
