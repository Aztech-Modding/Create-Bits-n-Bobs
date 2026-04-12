package com.kipti.bnb.registry.core;

public enum BnbFeatureGroup {
    CHAIN_DRIVES(
            "Chain Drives", "Bulk toggle for all chain drive related features.",
            BnbFeatureFlag.COGWHEEL_CHAIN_DRIVES, BnbFeatureFlag.FLANGED_CHAIN_DRIVES, BnbFeatureFlag.CHAIN_PULLEY
    ),
    LIGHTS(
            "Lights", "Bulk toggle for light related features (Excluding the old brass lamp block).",
            BnbFeatureFlag.LIGHTBULB, BnbFeatureFlag.HEADLAMP
    ),
    GIRDER_STRUTS(
            "Girder Struts",
            "Bulk toggle for all girder strut related features.",
            BnbFeatureFlag.GIRDER_STRUT,
            BnbFeatureFlag.WEATHERED_GIRDER,
            BnbFeatureFlag.CABLE_GIRDER_STRUT,
            BnbFeatureFlag.WOODEN_STRUT
    ),
    DYEABLE_PIPES_AND_TANKS(
            "Dyeable Pipes and Tanks", "Bulk toggle for dyeable pipe and tank behaviours.",
            BnbFeatureFlag.DYEABLE_PIPES, BnbFeatureFlag.DYEABLE_TANKS
    ),
    ;

    private final String displayName;
    private final String description;
    private final BnbFeatureFlag[] children;

    BnbFeatureGroup(final String displayName, final String description, final BnbFeatureFlag... children) {
        this.displayName = displayName;
        this.description = description;
        this.children = children;
    }

    public enum GroupState {
        ALL_ENABLED,
        ALL_DISABLED,
        PARTIAL
    }

    public GroupState getState() {
        boolean anyEnabled = false;
        boolean anyDisabled = false;
        for (final BnbFeatureFlag child : this.children) {
            if (child.isReleaseLocked()) {
                continue;
            }
            if (child.isEnabled()) {
                anyEnabled = true;
            } else {
                anyDisabled = true;
            }
        }
        if (anyEnabled && anyDisabled) {
            return GroupState.PARTIAL;
        }
        if (anyEnabled) {
            return GroupState.ALL_ENABLED;
        }
        return GroupState.ALL_DISABLED;
    }

    public void enableAll() {
        for (final BnbFeatureFlag child : this.children) {
            if (!child.isReleaseLocked()) {
                BnbConfigs.common().setFeatureFlagState(child, true);
            }
        }
    }

    public void disableAll() {
        for (final BnbFeatureFlag child : this.children) {
            if (!child.isReleaseLocked()) {
                BnbConfigs.common().setFeatureFlagState(child, false);
            }
        }
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getDescription() {
        return this.description;
    }

    public BnbFeatureFlag[] getChildren() {
        return this.children;
    }

    public static BnbFeatureGroup findGroupFor(final BnbFeatureFlag flag) {
        for (final BnbFeatureGroup group : values()) {
            for (final BnbFeatureFlag child : group.children) {
                if (child == flag) {
                    return group;
                }
            }
        }
        return null;
    }
}
