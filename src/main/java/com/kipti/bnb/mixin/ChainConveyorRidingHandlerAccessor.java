package com.kipti.bnb.mixin;

import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRidingHandler;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChainConveyorRidingHandler.class)
public interface ChainConveyorRidingHandlerAccessor {

    @Accessor("ridingChainConveyor")
    static BlockPos bits_n_bobs$getRidingChainConveyor() {
        throw new AssertionError();
    }

    @Invoker("stopRiding")
    static void bits_n_bobs$invokeStopRiding() {
        throw new AssertionError();
    }
}
