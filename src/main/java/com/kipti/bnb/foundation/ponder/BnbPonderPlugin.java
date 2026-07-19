package com.kipti.bnb.foundation.ponder;

import com.cake.azimuth.lang.IncludeLangDefaults;
import com.cake.azimuth.lang.LangDefault;
import com.kipti.bnb.CreateBitsnBobs;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@IncludeLangDefaults(
        @LangDefault(
                key = "bits_n_bobs.ponder.foreign_label", value = "Bits n Bobs"
        )
)
public class BnbPonderPlugin implements PonderPlugin {

    @Override
    public @NotNull String getModId() {
        return CreateBitsnBobs.MOD_ID;
    }

    @Override
    public void registerScenes(final @NotNull PonderSceneRegistrationHelper<ResourceLocation> helper) {
        BnbPonderScenes.register(helper);
    }

    @Override
    public void registerTags(final @NotNull PonderTagRegistrationHelper<ResourceLocation> helper) {
    }

    @Override
    public void registerSharedText(final @NotNull SharedTextRegistrationHelper helper) {
    }

}

