package com.kipti.bnb.content.kinetics.cogwheel_chain.graph;

import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ChainInteractionFailedException extends Exception {

    public static final String ABORTED_PLACEMENT_PREFIX = "message.bits_n_bobs.cogwheel_chain.chain_addition_aborted.";

    public ChainInteractionFailedException(final String message) {
        super(message);
    }

    public MutableComponent getTranslatedMessage() {
        return Component.translatable("message.bits_n_bobs.cogwheel_chain.chain_addition_aborted." + getMessage());
    }

    public Component getComponent() {
        return getTranslatedMessage().withColor(0xFF_ff5d6c);
    }

    public static void addTranslationLangs(final CreateRegistrate registrate, final String... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("KeyValuePairs length must be even");
        }
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            registrate.addRawLang(ABORTED_PLACEMENT_PREFIX + keyValuePairs[i], keyValuePairs[i + 1]);
        }
    }

}
