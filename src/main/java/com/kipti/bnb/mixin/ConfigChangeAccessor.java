package com.kipti.bnb.mixin;

import net.createmod.catnip.config.ui.ConfigHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = ConfigHelper.ConfigChange.class, remap = false)
public interface ConfigChangeAccessor {

	@Invoker("<init>")
	static ConfigHelper.ConfigChange bits_n_bobs$create(Object value) {
		throw new AssertionError();
	}

	@Accessor("value")
	Object bits_n_bobs$getValue();
}
