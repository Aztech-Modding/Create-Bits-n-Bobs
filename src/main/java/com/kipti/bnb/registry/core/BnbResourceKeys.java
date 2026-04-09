package com.kipti.bnb.registry.core;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class BnbResourceKeys {

    public static ResourceKey<Registry<CogwheelChainType>> COGWHEEL_CHAIN_TYPE = key("cogwheel_chain_type");

    private static <T> ResourceKey<Registry<T>> key(final String name) {
        return ResourceKey.createRegistryKey(CreateBitsnBobs.asResource(name));
    }

}

