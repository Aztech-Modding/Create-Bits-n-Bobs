package com.kipti.bnb.mixin;

import com.kipti.bnb.mixin_accessor.FontAccess;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Font.class)
public abstract class FontAccessMixin implements FontAccess {

    @Shadow
    abstract FontSet getFontSet(ResourceLocation fontLocation);

    @Override
    public FontSet bits_n_bobs$getFontSet(ResourceLocation fontLocation) {
        return getFontSet(fontLocation);
    }

}
