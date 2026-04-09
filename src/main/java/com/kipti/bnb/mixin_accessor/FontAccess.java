package com.kipti.bnb.mixin_accessor;

import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;

public interface FontAccess {

    FontSet bits_n_bobs$getFontSet(ResourceLocation fontLocation);

}

