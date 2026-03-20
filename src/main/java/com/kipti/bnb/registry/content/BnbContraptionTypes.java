package com.kipti.bnb.registry.content;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.kinetics.cogwheel_chain.carriage.CogwheelChainCarriageContraption;
import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;

import java.util.function.Supplier;

public class BnbContraptionTypes {

    public static final Holder.Reference<ContraptionType> COGWHEEL_CHAIN_CARRIAGE = register("cogwheel_chain_carriage", CogwheelChainCarriageContraption::new);

    private static Holder.Reference<ContraptionType> register(final String name, final Supplier<? extends Contraption> factory) {
        final ContraptionType type = new ContraptionType(factory);
        return Registry.registerForHolder(CreateBuiltInRegistries.CONTRAPTION_TYPE, CreateBitsnBobs.asResource(name), type);
    }

    public static void register() {
    }

}
