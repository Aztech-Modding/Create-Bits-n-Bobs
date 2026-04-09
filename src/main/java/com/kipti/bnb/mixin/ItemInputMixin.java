package com.kipti.bnb.mixin;

import com.kipti.bnb.registry.core.BnbFeatureFlag;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemInput.class)
public abstract class ItemInputMixin {

    @Shadow
    @Final
    private Holder<Item> item;

    @Unique
    private static final SimpleCommandExceptionType bits_n_bobs$RELEASE_LOCKED =
            new SimpleCommandExceptionType(Component.literal("nuh uh, that's not ready yet"));

    @Inject(method = "createItemStack", at = @At("HEAD"), cancellable = true)
    private void bits_n_bobs$onCreateItemStack(final int count, final boolean allowOversizedStacks, final CallbackInfoReturnable<ItemStack> cir) throws CommandSyntaxException {
        if (BnbFeatureFlag.isDevEnvironment()) {
            return;
        }

        if (BnbFeatureFlag.isReleaseLocked(this.item.value())) {
            throw bits_n_bobs$RELEASE_LOCKED.create();
        }
    }

}
