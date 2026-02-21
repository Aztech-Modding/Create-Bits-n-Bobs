package com.kipti.bnb.registry.datagen;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.foundation.config.conditions.BnbFeatureEnabledCondition;
import com.kipti.bnb.foundation.config.conditions.BnbFeatureItemEnabledCondition;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class BnbDataConditions {

    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, CreateBitsnBobs.MOD_ID);

    public static final Supplier<MapCodec<BnbFeatureEnabledCondition>> FEATURE_ENABLED_CONDITION =
            CONDITION_CODECS.register("feature_enabled", () -> BnbFeatureEnabledCondition.CODEC);
    public static final Supplier<MapCodec<BnbFeatureItemEnabledCondition>> ITEM_ENABLED_BY_FEATURE =
            CONDITION_CODECS.register("feature_item_enabled", () -> BnbFeatureItemEnabledCondition.CODEC);

    public static void register(final IEventBus eventBus) {
        CONDITION_CODECS.register(eventBus);
    }

}

