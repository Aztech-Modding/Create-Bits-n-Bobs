package com.kipti.bnb.foundation;

import com.kipti.bnb.content.girder_strut.GirderStrutPlacementEffects;
import com.kipti.bnb.content.girder_strut.cap.GirderCapDebugOutlines;
import com.kipti.bnb.content.weathered_girder.WeatheredGirderWrenchBehaviour;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onTickPost(ClientTickEvent.Post event) {
        WeatheredGirderWrenchBehaviour.tick();
        GirderCapDebugOutlines.flush();
    }

    @SubscribeEvent
    public static void onTickPre(ClientTickEvent.Pre event) {
        //If in a level, there is a player, and the player is holding a girder strut block item, update the preview
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            GirderStrutPlacementEffects.tick(mc.player);
        }
    }

}
