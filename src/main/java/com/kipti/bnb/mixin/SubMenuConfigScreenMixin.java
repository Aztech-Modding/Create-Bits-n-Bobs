package com.kipti.bnb.mixin;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.kipti.bnb.foundation.gui.screen.BnbFeatureGroupEntry;
import com.kipti.bnb.registry.core.BnbFeatureGroup;
import net.createmod.catnip.config.ui.ConfigScreen;
import net.createmod.catnip.config.ui.ConfigScreenList;
import net.createmod.catnip.config.ui.SubMenuConfigScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = SubMenuConfigScreen.class, remap = false)
public class SubMenuConfigScreenMixin {

    @Shadow
    UnmodifiableConfig configGroup;

    @Shadow
    ConfigScreenList list;

    @Inject(method = "init", at = @At("RETURN"))
    private void injectFeatureGroupEntries(final CallbackInfo ci) {
        if (!ConfigScreen.modID.equals("bits_n_bobs"))
            return;
        if (!this.configGroup.valueMap().containsKey("blocks") && !this.configGroup.valueMap().containsKey("behaviours"))
            return;

        final List<ConfigScreenList.Entry> children = this.list.children();
        children.removeIf(e -> e instanceof BnbFeatureGroupEntry);

        final List<ConfigScreenList.Entry> entries = new ArrayList<>();
        for (final BnbFeatureGroup group : BnbFeatureGroup.values()) {
            entries.add(new BnbFeatureGroupEntry(group));
        }

        children.addAll(0, entries);
    }
    
}
