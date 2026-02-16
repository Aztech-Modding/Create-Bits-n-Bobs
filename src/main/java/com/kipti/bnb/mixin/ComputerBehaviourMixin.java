package com.kipti.bnb.mixin;

import com.kipti.bnb.registry.BnbComputerBehaviourPeripherals;
import com.simibubi.create.compat.computercraft.implementation.ComputerBehaviour;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(ComputerBehaviour.class)
public class ComputerBehaviourMixin {

    @Inject(method = "getPeripheralFor", at = @At("HEAD"), cancellable = true)
    private static void getPeripheralFor(final SmartBlockEntity be, final CallbackInfoReturnable<Supplier<SyncedPeripheral<SmartBlockEntity>>> cir) {
        final BnbComputerBehaviourPeripherals.ComputerBehaviourPeripheralType<SmartBlockEntity> type = BnbComputerBehaviourPeripherals.COMPUTER_BEHAVIOUR_EXTRA_PERIPHERALS.getForBlockEntity(be);
        if (type != null) {
            cir.setReturnValue(type.supplierConstructor().apply(be));
        }
    }

}
