package com.kipti.bnb.content.trinkets.cookie_dough;

import com.kipti.bnb.registry.content.BnbAdvancements;
import com.kipti.bnb.registry.content.BnbItems;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

@EventBusSubscriber
public class CookieDoughEvents {

    @SubscribeEvent
    public static void onItemUse(final LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().is(BnbItems.COOKIE_DOUGH.get())) {
            if (event.getEntity() instanceof final Player player) {
                BnbAdvancements.COOKIE_DOUGH.awardTo(player);
            }
        }
    }

}
