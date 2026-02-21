package com.kipti.bnb.foundation.config;

import com.google.common.collect.ImmutableMap;
import com.kipti.bnb.registry.core.BnbFeatureFlag;
import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BnbCommonConfig extends ConfigBase {

    public final ConfigBase.ConfigGroup FEATURE_FLAGS_GROUP = group(1, "featureFlags", "Feature flags to enable or disable certain features of the mod.");
    public final Map<BnbFeatureFlag, ConfigBase.ConfigBool> FEATURE_FLAGS = createFeatureFlagConfigs();

    private Map<BnbFeatureFlag, ConfigBase.ConfigBool> createFeatureFlagConfigs() {
        final HashMap<BnbFeatureFlag, ConfigBase.ConfigBool> map = new HashMap<>();
        for (final BnbFeatureFlag featureFlag : BnbFeatureFlag.values()) {
            final ConfigBase.ConfigBool configBool = b(
                    featureFlag.getDefaultState(),
                    enumToCamelCase(featureFlag.name().toLowerCase()),
                    featureFlag.getDescription()
            );
            map.put(featureFlag, configBool);
        }

        return ImmutableMap.copyOf(map);
    }

    private String enumToCamelCase(final String lowerCase) {
        final StringBuilder result = new StringBuilder();

        boolean capitalizeNext = false;
        for (final char c : lowerCase.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(c);
                }
            }
        }

        return result.toString();
    }

    public boolean getFeatureFlagState(final BnbFeatureFlag featureFlagKey) {
        if (!specification.isLoaded()) {
            System.out.println("Config not loaded yet but feature flag was asked for anyways!");
            return false;
        }
        final ConfigBase.ConfigBool configBool = FEATURE_FLAGS.get(featureFlagKey);
        return configBool != null && configBool.get();
    }


    @Override
    public @NotNull String getName() {
        return "common";
    }
}

