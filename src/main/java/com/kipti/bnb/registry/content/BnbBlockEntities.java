package com.kipti.bnb.registry.content;

import com.cake.struts.content.block.StrutBlockEntity;
import com.cake.struts.content.block.StrutBlockEntityRenderer;
import com.kipti.bnb.content.kinetics.chain_pulley.ChainPulleyBlockEntity;
import com.kipti.bnb.content.kinetics.chain_pulley.ChainPulleyRenderer;
import com.kipti.bnb.content.kinetics.cogwheel_carriage.block.CogwheelChainCarriageBlockEntity;
import com.kipti.bnb.content.kinetics.cogwheel_carriage.block.CogwheelChainCarriageRenderer;
import com.kipti.bnb.content.kinetics.cogwheel_chain.migration.MigratingSimpleKineticBlockEntity;
import com.kipti.bnb.content.kinetics.flywheel_bearing.FlywheelBearingBlockEntity;
import com.kipti.bnb.content.kinetics.flywheel_bearing.FlywheelBearingBlockEntityRenderer;
import com.kipti.bnb.content.kinetics.throttle_lever.ThrottleLeverBlockEntity;
import com.kipti.bnb.content.kinetics.throttle_lever.ThrottleLeverBlockEntityRenderer;
import com.kipti.bnb.content.trinkets.light.headlamp.HeadlampBlockEntity;
import com.kipti.bnb.content.trinkets.light.headlamp.rendering.pipeline.block_entity.HeadlampBlockEntityRenderer;
import com.kipti.bnb.content.trinkets.light.headlamp.rendering.pipeline.visual.HeadlampVisual;
import com.kipti.bnb.content.trinkets.nixie.foundation.GenericNixieDisplayBlockEntity;
import com.kipti.bnb.content.trinkets.nixie.foundation.GenericNixieDisplayBoardRenderer;
import com.kipti.bnb.foundation.client.GenericBlockEntityRenderModels;
import com.kipti.bnb.registry.content.blocks.BnbKineticBlocks;
import com.kipti.bnb.registry.content.blocks.BnbTrinketBlocks;
import com.kipti.bnb.registry.content.blocks.deco.BnbDecorativeBlocks;
import com.kipti.bnb.registry.content.blocks.encased.BnbEncasedListBlocks;
import com.kipti.bnb.registry.content.blocks.encased.BnbExtraEncasedBlocks;
import com.kipti.bnb.registry.content.blocks.encased.BnbSpecialEncasedBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.engine_room.flywheel.lib.model.Models;

import static com.kipti.bnb.CreateBitsnBobs.REGISTRATE;

public class BnbBlockEntities {

    public static final BlockEntityEntry<HeadlampBlockEntity> HEADLAMP = REGISTRATE.blockEntity(
                    "headlamp",
                    HeadlampBlockEntity::new
            )
            .visual(() -> HeadlampVisual::new)
            .validBlock(BnbTrinketBlocks.HEADLAMP)
            .renderer(() -> HeadlampBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<StrutBlockEntity> GIRDER_STRUT = REGISTRATE
            .blockEntity("girder_strut", StrutBlockEntity::new)
            .validBlocks(
                    BnbDecorativeBlocks.GIRDER_STRUT, BnbDecorativeBlocks.WEATHERED_GIRDER_STRUT,
                    BnbDecorativeBlocks.WOODEN_GIRDER_STRUT, BnbDecorativeBlocks.CABLE_GIRDER_STRUT
            )
            .renderer(() -> StrutBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<KineticBlockEntity> ENCASED_SHAFT = REGISTRATE
            .blockEntity("encased_shaft", KineticBlockEntity::new)
            .visual(() -> SingleAxisRotatingVisual::shaft, false)
            .validBlocks(
                    BnbDecorativeBlocks.WEATHERED_METAL_GIRDER_ENCASED_SHAFT,
                    BnbExtraEncasedBlocks.INDUSTRIAL_IRON_ENCASED_SHAFT,
                    BnbExtraEncasedBlocks.WEATHERED_IRON_ENCASED_SHAFT,
                    BnbSpecialEncasedBlocks.INDUSTRIAL_GRATING_PANEL
            )
            .renderer(() -> ShaftRenderer::new)
            .register();

    public static final BlockEntityEntry<SimpleKineticBlockEntity> ENCASED_COGWHEEL = REGISTRATE
            .blockEntity("encased_cogwheel", SimpleKineticBlockEntity::new)
            .visual(() -> EncasedCogVisual::small, false)
            .validBlocks(
                    BnbExtraEncasedBlocks.INDUSTRIAL_IRON_ENCASED_COGWHEEL,
                    BnbExtraEncasedBlocks.WEATHERED_IRON_ENCASED_COGWHEEL
            )
            .renderer(() -> EncasedCogRenderer::small)
            .register();

    public static final BlockEntityEntry<SimpleKineticBlockEntity> ENCASED_LARGE_COGWHEEL = REGISTRATE
            .blockEntity("encased_large_cogwheel", SimpleKineticBlockEntity::new)
            .visual(() -> EncasedCogVisual::large, false)
            .validBlocks(
                    BnbExtraEncasedBlocks.INDUSTRIAL_IRON_ENCASED_LARGE_COGWHEEL,
                    BnbExtraEncasedBlocks.WEATHERED_IRON_ENCASED_LARGE_COGWHEEL
            )
            .renderer(() -> EncasedCogRenderer::large)
            .register();

    public static final BlockEntityEntry<GenericNixieDisplayBlockEntity> GENERIC_NIXIE_DISPLAY = REGISTRATE.blockEntity(
                    "generic_nixie_display",
                    GenericNixieDisplayBlockEntity::new
            )
            .validBlocks(BnbTrinketBlocks.NIXIE_BOARD, BnbTrinketBlocks.LARGE_NIXIE_TUBE)
            .validBlocks(BnbTrinketBlocks.DYED_NIXIE_BOARD.toArray())
            .validBlocks(BnbTrinketBlocks.DYED_LARGE_NIXIE_TUBE.toArray())
            .renderer(() -> GenericNixieDisplayBoardRenderer::new)
            .register();

    public static final BlockEntityEntry<FlywheelBearingBlockEntity> FLYWHEEL_BEARING = REGISTRATE
            .blockEntity("flywheel_bearing", FlywheelBearingBlockEntity::new)
            .validBlocks(BnbKineticBlocks.FLYWHEEL_BEARING)
            .renderer(() -> FlywheelBearingBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<ChainPulleyBlockEntity> CHAIN_ROPE_PULLEY = REGISTRATE
            .blockEntity("chain_pulley", ChainPulleyBlockEntity::new)
//            .visual(() -> ChainPulleyVisual::new, false)
            .validBlocks(BnbKineticBlocks.CHAIN_PULLEY)
            .renderer(() -> ChainPulleyRenderer::new)
            .register();

    public static final BlockEntityEntry<MigratingSimpleKineticBlockEntity> MIGRATING_SIMPLE_KINETIC = REGISTRATE.blockEntity(
                    "migrating_simple_kinetic",
                    MigratingSimpleKineticBlockEntity::new
            )
            .visual(
                    () -> (context, blockEntity, partialTick) ->
                            new SingleAxisRotatingVisual<>(
                                    context, blockEntity, partialTick,
                                    Models.partial(GenericBlockEntityRenderModels.REGISTRY.get(blockEntity.getBlockState().getBlock()))
                            ), true
            )
            .validBlocks(BnbKineticBlocks.SMALL_FLANGED_COGWHEEL, BnbKineticBlocks.LARGE_FLANGED_COGWHEEL)
            .validBlocks(AllBlocks.COGWHEEL, AllBlocks.LARGE_COGWHEEL)
            .renderer(() -> KineticBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<SimpleKineticBlockEntity> SIMPLE_KINETIC = REGISTRATE.blockEntity(
                    "simple_kinetic",
                    SimpleKineticBlockEntity::new
            )
            .visual(
                    () -> (context, blockEntity, partialTick) ->
                            new SingleAxisRotatingVisual<>(
                                    context, blockEntity, partialTick,
                                    Models.partial(GenericBlockEntityRenderModels.REGISTRY.get(blockEntity.getBlockState().getBlock()))
                            ), true
            )
            .validBlocks(BnbKineticBlocks.SMALL_FLANGED_COGWHEEL, BnbKineticBlocks.LARGE_FLANGED_COGWHEEL)
            .validBlocks(BnbEncasedListBlocks.ENCASED_LARGE_FLANGED_COGWHEEL.toArray())
            .validBlocks(BnbEncasedListBlocks.ENCASED_FLANGED_COGWHEEL.toArray())
            .renderer(() -> KineticBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<ThrottleLeverBlockEntity> THROTTLE_LEVER = REGISTRATE
            .blockEntity("throttle_lever", ThrottleLeverBlockEntity::new)
            .validBlocks(BnbTrinketBlocks.THROTTLE_LEVER)
            .renderer(() -> ThrottleLeverBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<CogwheelChainCarriageBlockEntity> COGWHEEL_CHAIN_CARRIAGE = REGISTRATE
            .blockEntity("cogwheel_chain_carriage", CogwheelChainCarriageBlockEntity::new)
            .validBlock(BnbKineticBlocks.COGWHEEL_CHAIN_CARRIAGE)
            .renderer(() -> CogwheelChainCarriageRenderer::new)
            .register();

    public static void register() {
    }

}


