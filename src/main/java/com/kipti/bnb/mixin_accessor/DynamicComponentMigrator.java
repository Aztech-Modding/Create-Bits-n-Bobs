package com.kipti.bnb.mixin_accessor;

import net.minecraft.core.HolderLookup;

public interface DynamicComponentMigrator {

    void bits_n_bobs$setValueToLiteral(String value, final HolderLookup.Provider registryAccess);

}

