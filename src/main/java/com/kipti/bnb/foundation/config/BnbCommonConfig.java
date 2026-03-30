package com.kipti.bnb.foundation.config;

import com.google.common.collect.ImmutableMap;
import com.kipti.bnb.registry.core.BnbFeatureFlag;
import com.kipti.bnb.registry.core.BnbFeatureGroup;
import com.kipti.bnb.registry.core.FeatureCategory;
import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Common configuration for Bits 'n' Bobs, containing feature flag toggles and bulk feature group toggles.
 */
public class BnbCommonConfig extends ConfigBase {

    private final Map<BnbFeatureGroup, Boolean> previousGroupValues = new EnumMap<>(BnbFeatureGroup.class);
    private boolean syncingGroupStates;

    public final ConfigGroup FEATURE_FLAGS_GROUP = group(0, "featureFlags", "Feature flags to enable or disable certain features of the mod.");
    public final Map<BnbFeatureGroup, ConfigBool> FEATURE_GROUP_TOGGLES = createFeatureGroupToggleConfigs();
    public final Map<BnbFeatureFlag, ConfigBool> FEATURE_FLAGS = createFeatureFlagConfigs();

    private Map<BnbFeatureGroup, ConfigBool> createFeatureGroupToggleConfigs() {
        group(1, "groups", "Bulk toggle groups. Enabling a group enables all its child features. Disabling a group disables all its child features.");

        final HashMap<BnbFeatureGroup, ConfigBool> map = new HashMap<>();
        for (final BnbFeatureGroup featureGroup : BnbFeatureGroup.values()) {
            final ConfigBool configBool = b(
                    true,
                    enumToCamelCase(featureGroup.name().toLowerCase()),
                    featureGroup.getDescription()
            );
            map.put(featureGroup, configBool);
        }
        return ImmutableMap.copyOf(map);
    }

    private Map<BnbFeatureFlag, ConfigBool> createFeatureFlagConfigs() {
        final HashMap<BnbFeatureFlag, ConfigBool> map = new HashMap<>();

        for (final FeatureCategory category : FeatureCategory.values()) {
            selectCategoryGroup(category);
            for (final BnbFeatureFlag flag : BnbFeatureFlag.values()) {
                if (flag.getCategory() != category) {
                    continue;
                }
                if (flag.isReleaseLocked()) {
                    continue;
                }
                final ConfigBool configBool = b(
                        flag.getDefaultState(),
                        enumToCamelCase(flag.name().toLowerCase()),
                        flag.getDescription()
                );
                map.put(flag, configBool);
            }
        }

        return ImmutableMap.copyOf(map);
    }

    private void selectCategoryGroup(final FeatureCategory category) {
        switch (category) {
            case BLOCK -> group(1, "blocks", "Block feature toggles.");
            case ITEM -> group(1, "items", "Item feature toggles.");
            case BEHAVIOUR -> group(1, "behaviours", "Behaviour feature toggles.");
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
        syncGroupStatesFromChildren();
    }

    @Override
    public void onReload() {
        if (this.syncingGroupStates) {
            return;
        }
        this.syncingGroupStates = true;
        try {
            propagateChangedGroups();
            syncGroupStatesFromChildren();
        } finally {
            this.syncingGroupStates = false;
        }
    }

    private void propagateChangedGroups() {
        for (final BnbFeatureGroup group : BnbFeatureGroup.values()) {
            final ConfigBool groupBool = this.FEATURE_GROUP_TOGGLES.get(group);
            if (groupBool == null) {
                continue;
            }

            final boolean currentValue = groupBool.get();
            final Boolean previousValue = this.previousGroupValues.get(group);
            if (previousValue == null || currentValue == previousValue) {
                continue;
            }

            if (currentValue) {
                group.enableAll();
            } else {
                group.disableAll();
            }
        }
    }

    private void syncGroupStatesFromChildren() {
        for (final BnbFeatureGroup group : BnbFeatureGroup.values()) {
            final ConfigBool groupBool = this.FEATURE_GROUP_TOGGLES.get(group);
            if (groupBool == null) {
                continue;
            }

            final boolean allEnabled = computeGroupState(group) == BnbFeatureGroup.GroupState.ALL_ENABLED;
            groupBool.set(allEnabled);
            this.previousGroupValues.put(group, allEnabled);
        }
    }

    private BnbFeatureGroup.GroupState computeGroupState(final BnbFeatureGroup group) {
        boolean anyEnabled = false;
        boolean anyDisabled = false;
        for (final BnbFeatureFlag child : group.getChildren()) {
            if (child.isReleaseLocked()) {
                continue;
            }
            final ConfigBool childBool = this.FEATURE_FLAGS.get(child);
            if (childBool == null) {
                continue;
            }
            if (childBool.get()) {
                anyEnabled = true;
            } else {
                anyDisabled = true;
            }
        }
        if (anyEnabled && anyDisabled) {
            return BnbFeatureGroup.GroupState.PARTIAL;
        }
        if (anyEnabled) {
            return BnbFeatureGroup.GroupState.ALL_ENABLED;
        }
        return BnbFeatureGroup.GroupState.ALL_DISABLED;
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

