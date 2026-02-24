package com.kipti.bnb.foundation.ponder;

import com.kipti.bnb.CreateBitsnBobs;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class BnbPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return CreateBitsnBobs.MOD_ID;
    }

    @Override
    public void registerScenes(final PonderSceneRegistrationHelper<ResourceLocation> helper) {
        BnbPonderScenes.register(helper);
    }

    @Override
    public void registerTags(final PonderTagRegistrationHelper<ResourceLocation> helper) {
    }

    @Override
    public void registerSharedText(final SharedTextRegistrationHelper helper) {
    }

}

