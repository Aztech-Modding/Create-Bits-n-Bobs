package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.decoration.nixie.foundation.GenericNixieDisplayTarget;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.tterrag.registrate.util.entry.RegistryEntry;

import java.util.function.Supplier;

public class BnbDisplayTargets {

    public static final RegistryEntry<DisplayTarget, GenericNixieDisplayTarget> GENERIC_NIXIE_TARGET = simple("nixie_board", GenericNixieDisplayTarget::new);

    private static <T extends DisplayTarget> RegistryEntry<DisplayTarget, T> simple(String name, Supplier<T> supplier) {
        return CreateBitsnBobs.REGISTRATE.displayTarget(name, supplier).register();
    }
}
