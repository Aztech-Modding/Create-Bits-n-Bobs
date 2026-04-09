package com.kipti.bnb.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.kipti.bnb.mixin_accessor.DynamicComponentMigrator;
import com.simibubi.create.foundation.utility.DynamicComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DynamicComponent.class)
public abstract class DynamicComponentMixin implements DynamicComponentMigrator {

    @Shadow
    private Component parsedCustomText;

    @Shadow
    private JsonElement rawCustomText;

    @Inject(method = "displayCustomText", at = @At("TAIL"))
    private void bits_n_bobs$parseVirtualCustomText(final Level level, final BlockPos pos, final String tagElement, final CallbackInfo ci) {
        if (parsedCustomText != null || rawCustomText == null) {
            return;
        }

        try {
            parsedCustomText = Component.Serializer.fromJson(rawCustomText, level.registryAccess());
        } catch (final JsonParseException e) {
            parsedCustomText = null;
        }
    }

    @Override
    public void bits_n_bobs$setValueToLiteral(final String value, final HolderLookup.Provider registryAccess) {
        rawCustomText = DynamicComponent.getJsonFromString(Component.Serializer.toJson(Component.literal(value), registryAccess));
        parsedCustomText = Component.literal(value);
    }

}

