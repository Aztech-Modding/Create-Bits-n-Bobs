package com.kipti.bnb.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AllBlocks.class)
public class AllBlocksMixin {

    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/data/CreateRegistrate;block(Ljava/lang/String;Lcom/tterrag/registrate/util/nullness/NonNullFunction;)Lcom/tterrag/registrate/builders/BlockBuilder;"))
    private static BlockBuilder addEmissiveness(CreateRegistrate instance, String s, NonNullFunction nonNullFunction, Operation<BlockBuilder> original) {
        BlockBuilder builder = original.call(instance, s, nonNullFunction);

        if (s.contains("belt")) {
            builder.properties(p -> ((BlockBehaviour.Properties) p).emissiveRendering((a, b, c) -> true));
        }

        return builder;
    }

}
