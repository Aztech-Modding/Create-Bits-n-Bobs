package com.kipti.bnb.foundation.config;

import com.kipti.bnb.registry.core.BnbFeatureFlag;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ReleaseLockTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(final ItemTooltipEvent event) {
        if (!BnbFeatureFlag.isDevEnvironment()) {
            return;
        }

        if (BnbFeatureFlag.isReleaseLocked(event.getItemStack().getItem())) {
            event.getToolTip().add(Component.literal("[!] Feature disabled in production").withStyle(ChatFormatting.RED));
        }
    }

}
