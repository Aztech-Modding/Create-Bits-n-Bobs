package com.kipti.bnb.mixin;

import com.kipti.bnb.registry.BnbCreateBlockEdits;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(AllBlocks.class)
public class AllBlocksMixin {

    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/data/CreateRegistrate;block(Ljava/lang/String;Lcom/tterrag/registrate/util/nullness/NonNullFunction;)Lcom/tterrag/registrate/builders/BlockBuilder;"))
    private static BlockBuilder addEmissiveness(final CreateRegistrate instance, final String s, final NonNullFunction nonNullFunction, final Operation<BlockBuilder> original) {
        final BlockBuilder builder = original.call(instance, s, nonNullFunction);

        final Consumer<BlockBuilder<?, CreateRegistrate>> transform = BnbCreateBlockEdits.getEditForId(s);
        if (transform != null) {
            transform.accept(builder);
        }

        return builder;
    }

}
