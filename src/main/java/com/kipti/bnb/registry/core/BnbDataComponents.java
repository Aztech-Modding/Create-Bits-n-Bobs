package com.kipti.bnb.registry.core;

import com.cake.struts.registry.StrutDataComponents;
import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelChain;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.UnaryOperator;

public class BnbDataComponents {

    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CreateBitsnBobs.MOD_ID);

    public static final DataComponentType<BlockPos> GIRDER_STRUT_FROM = StrutDataComponents.GIRDER_STRUT_FROM;

    public static final DataComponentType<Direction> GIRDER_STRUT_FROM_FACE = StrutDataComponents.GIRDER_STRUT_FROM_FACE;

    public static final DataComponentType<PlacingCogwheelChain> PARTIAL_COGWHEEL_CHAIN = register(
            "partial_cogwheel_chain",
            builder -> builder.persistent(PlacingCogwheelChain.CODEC).networkSynchronized(PlacingCogwheelChain.STREAM_CODEC)
    );

    public static final DataComponentType<Float> TRACK_TILT = register(
            "track_tilt",
            builder -> builder
                    .persistent(Codec.FLOAT)
                    .networkSynchronized(ByteBufCodecs.FLOAT)
    );

    private static <T> DataComponentType<T> register(final String name, final UnaryOperator<DataComponentType.Builder<T>> builder) {
        final DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
        DATA_COMPONENTS.register(name, () -> type);
        return type;
    }

    @ApiStatus.Internal
    public static void register(final IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }

}

