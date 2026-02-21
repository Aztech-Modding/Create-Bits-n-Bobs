package com.kipti.bnb.mixin;

import com.google.gson.JsonElement;
import com.kipti.bnb.mixin_accessor.DynamicComponentMigrator;
import com.simibubi.create.foundation.utility.DynamicComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DynamicComponent.class)
public abstract class DynamicComponentMixin implements DynamicComponentMigrator {

    @Shadow
    private Component parsedCustomText;

    @Shadow
    private JsonElement rawCustomText;

    @Override
    public void bits_n_bobs$setValueToLiteral(final String value, final HolderLookup.Provider registryAccess) {
        rawCustomText = DynamicComponent.getJsonFromString(Component.Serializer.toJson(Component.literal(value), registryAccess));
        parsedCustomText = Component.literal(value);
    }

}

