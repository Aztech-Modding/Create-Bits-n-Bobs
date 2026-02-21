package com.kipti.bnb.foundation.config;

import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

public class BnbServerConfig extends ConfigBase {

    public final ConfigBool FLYWHEEL_STORAGE_CAPACITY = b(
            false,
            "flywheelBearingStorageCapability",
            "(This block is in development, and disabled in feature flags by default) Determines whether the flywheel bearing should be able to function as kinetic storage, setting this to false this means it will be purely decorative."
    );

    public final ConfigFloat FLYWHEEL_STORAGE_FACTOR = f(
            1, 0f, 100f,
            "flywheelStorageFactor",
            "(This block is in development, and disabled in feature flags by default) Multiplier for the kinetic storage capacity of flywheels mounted on flywheel bearings (if enabled). Values higher than one increase capacity. It is not recommended to set the value to 0, as you should disable the storage feature entirely."
    );

    public final ConfigFloat COGWHEEL_CHAIN_DRIVE_COST_FACTOR = f(
            1, 0f, 10f,
            "cogwheelChainDriveCostFactor",
            "Multiplier for the number of chains required for a cogwheel chain. Minimum cost is always 1 chain, unless this value is set to 0. Does not affect the number of chains returned by existing chain drives."
    );

    public final ConfigInt HEADLAMP_CC_BLOCK_RANGE = i(
            32, 1, 128,
            "headlampCCBlockRange",
            "Maximum range in blocks for the CC peripheral setLamp function. Lamp coordinates must be within [3 - blockRange * 2, blockRange * 2]."
    );

    public final ConfigInt MAX_CHAIN_COGWHEEL_RANGE = i(
            32, 1, 128,
            "maxChainCogwheelRange",
            "May lead to unexpected behaviors."
    );

    @Override
    public @NotNull String getName() {
        return "server";
    }

}
