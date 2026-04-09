package com.kipti.bnb.foundation.config.conditions;

import com.kipti.bnb.registry.core.BnbFeatureFlag;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

public record BnbFeatureEnabledCondition(String featureFlagKey) implements ICondition {

    public static final MapCodec<BnbFeatureEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(
            c ->
                    c.group(
                            Codec.STRING.fieldOf("feature").forGetter(BnbFeatureEnabledCondition::featureFlagKey)
                    ).apply(c, BnbFeatureEnabledCondition::new)
    );

    @Override
    public boolean test(final @NotNull IContext context) {
        return BnbFeatureFlag.isEnabled(this.featureFlagKey);
    }

    @Override
    public @NotNull MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

}

