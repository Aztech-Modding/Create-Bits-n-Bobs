package com.kipti.bnb.foundation.config;

import com.google.common.collect.ImmutableMap;
import com.kipti.bnb.registry.core.BnbFeatureFlag;
import com.kipti.bnb.registry.core.FeatureCategories;
import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.kipti.bnb.registry.core.FeatureCategories.*;

/**
 * Common configuration for Bits 'n' Bobs, containing feature flag toggles.
 */
public class BnbCommonConfig extends ConfigBase {

    public final ConfigGroup FEATURE_FLAGS_GROUP = this.group(
            0,
            "featureFlags",
            "Feature flags to enable or disable certain features of the mod."
    );
    public final Map<BnbFeatureFlag, ConfigBool> FEATURE_FLAGS = this.createFeatureFlagConfigs();

    private Map<BnbFeatureFlag, ConfigBool> createFeatureFlagConfigs() {
        final HashMap<BnbFeatureFlag, ConfigBool> map = new HashMap<>();

        for (final FeatureCategories.FeatureCategory category : FeatureCategories.values()) {
            this.selectCategoryGroup(category);
            for (final BnbFeatureFlag flag : BnbFeatureFlag.values()) {
                if (flag.getCategory() != category) {
                    continue;
                }
                if (flag.isReleaseLocked()) {
                    continue;
                }
                final ConfigBool configBool = this.b(
                        flag.getDefaultState(),
                        this.enumToCamelCase(flag.name().toLowerCase(Locale.ROOT)),
                        flag.getDescription()
                );
                map.put(flag, configBool);
            }
        }

        return ImmutableMap.copyOf(map);
    }

    private void selectCategoryGroup(final FeatureCategories.FeatureCategory category) {
        if (category == BLOCK) {
            this.group(1, "blocks", "Block feature toggles.");
        } else if (category == ITEM) {
            this.group(1, "items", "Item feature toggles.");
        } else if (category == BEHAVIOUR) {
            this.group(1, "behaviours", "Behaviour feature toggles.");
        }
    }

    public void setFeatureFlagState(final BnbFeatureFlag flag, final boolean state) {
        final ConfigBool configBool = this.FEATURE_FLAGS.get(flag);
        if (configBool != null) {
            configBool.set(state);
        }
    }

    public boolean getFeatureFlagState(final BnbFeatureFlag flag) {
        if (flag.isReleaseLocked()) {
            return BnbFeatureFlag.isDevEnvironment();
        }
        if (!this.specification.isLoaded()) {
            return false;
        }
        final ConfigBool configBool = this.FEATURE_FLAGS.get(flag);
        return configBool != null && configBool.get();
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onReload() {
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

    @Override
    public @NotNull String getName() {
        return "common";
    }
}
