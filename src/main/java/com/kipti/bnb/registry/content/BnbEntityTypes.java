package com.kipti.bnb.registry.content;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.kinetics.cogwheel_chain.carriage.CogwheelChainCarriageContraptionEntity;
import com.kipti.bnb.content.kinetics.flywheel_bearing.contraption.InertControlledContraptionEntity;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.kipti.bnb.content.kinetics.cogwheel_chain.carriage.CogwheelChainCarriageRenderer;
import com.simibubi.create.content.contraptions.render.ContraptionEntityRenderer;
import com.simibubi.create.content.contraptions.render.ContraptionVisual;
import com.simibubi.create.foundation.data.CreateEntityBuilder;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.createmod.catnip.lang.Lang;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.common.Tags;

public class BnbEntityTypes {

    public static final EntityEntry<InertControlledContraptionEntity> INERT_CONTROLLED_CONTRAPTION =
            contraption("inert_stationary_contraption", InertControlledContraptionEntity::new, () -> ContraptionEntityRenderer::new,
                    20, 40, false)
                    .visual(() -> ContraptionVisual::new)
                    .register();

    public static final EntityEntry<CogwheelChainCarriageContraptionEntity> COGWHEEL_CHAIN_CARRIAGE_CONTRAPTION =
            contraption("cogwheel_chain_carriage_contraption", CogwheelChainCarriageContraptionEntity::new,
                    () -> CogwheelChainCarriageRenderer::new, 20, 40, false)
                    .register();

    private static <T extends Entity> CreateEntityBuilder<T, ?> contraption(final String name, final EntityType.EntityFactory<T> factory,
                                                                            final NonNullSupplier<NonNullFunction<EntityRendererProvider.Context, EntityRenderer<? super T>>> renderer, final int range,
                                                                            final int updateFrequency, final boolean sendVelocity) {
        return (CreateEntityBuilder<T, ?>) register(name, factory, renderer, MobCategory.MISC, range, updateFrequency, sendVelocity, true,
                AbstractContraptionEntity::build)
                .tag(Tags.EntityTypes.TELEPORTING_NOT_SUPPORTED);
    }

    private static <T extends Entity> CreateEntityBuilder<T, ?> register(final String name, final EntityType.EntityFactory<T> factory,
                                                                         final NonNullSupplier<NonNullFunction<EntityRendererProvider.Context, EntityRenderer<? super T>>> renderer,
                                                                         final MobCategory group, final int range, final int updateFrequency, final boolean sendVelocity, final boolean immuneToFire,
                                                                         final NonNullConsumer<EntityType.Builder<T>> propertyBuilder) {
        final String id = Lang.asId(name);
        return (CreateEntityBuilder<T, ?>) CreateBitsnBobs.REGISTRATE
                .entity(id, factory, group)
                .properties(b -> b.setTrackingRange(range)
                        .setUpdateInterval(updateFrequency)
                        .setShouldReceiveVelocityUpdates(sendVelocity))
                .properties(propertyBuilder)
                .properties(b -> {
                    if (immuneToFire)
                        b.fireImmune();
                })
                .renderer(renderer);
    }

    public static void register() {
    }

}

