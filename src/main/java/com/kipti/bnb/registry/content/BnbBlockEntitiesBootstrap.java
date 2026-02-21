package com.kipti.bnb.registry.content;

import com.kipti.bnb.content.decoration.girder_strut.GirderStrutBlockEntity;
import com.kipti.bnb.content.decoration.girder_strut.GirderStrutBlockEntityRenderer;
import com.kipti.bnb.content.decoration.light.headlamp.HeadlampBlockEntity;
import com.kipti.bnb.content.decoration.light.headlamp.rendering.pipeline.block_entity.HeadlampBlockEntityRenderer;
import com.kipti.bnb.content.decoration.light.headlamp.rendering.pipeline.visual.HeadlampVisual;
import com.kipti.bnb.content.decoration.nixie.foundation.GenericNixieDisplayBlockEntity;
import com.kipti.bnb.content.decoration.nixie.foundation.GenericNixieDisplayBoardRenderer;
import com.kipti.bnb.content.kinetics.chain_pulley.ChainPulleyBlockEntity;
import com.kipti.bnb.content.kinetics.chain_pulley.ChainPulleyRenderer;
import com.kipti.bnb.content.kinetics.cogwheel_chain.block.CogwheelChainBlockEntity;
import com.kipti.bnb.content.kinetics.cogwheel_chain.block.CogwheelChainBlockEntityRenderer;
import com.kipti.bnb.content.kinetics.cogwheel_chain.block.GenericBlockEntityRenderModels;
import com.kipti.bnb.content.kinetics.flywheel_bearing.FlywheelBearingBlockEntity;
import com.kipti.bnb.content.kinetics.flywheel_bearing.FlywheelBearingBlockEntityRenderer;
import com.kipti.bnb.content.kinetics.throttle_lever.ThrottleLeverBlockEntity;
import com.kipti.bnb.content.kinetics.throttle_lever.ThrottleLeverBlockEntityRenderer;
import com.kipti.bnb.registry.content.blocks.deco.BnbDecorativeBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.Models;

import static com.kipti.bnb.CreateBitsnBobs.REGISTRATE;

public class BnbBlockEntitiesBootstrap {

    public static final BlockEntityEntry<HeadlampBlockEntity> HEADLAMP = REGISTRATE.blockEntity("headlamp", HeadlampBlockEntity::new)
            .visual(() -> HeadlampVisual::new)
            .validBlock(BnbBlocksBootstrap.HEADLAMP)
            .renderer(() -> HeadlampBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<KineticBlockEntity> ENCASED_SHAFT = REGISTRATE
            .blockEntity("encased_shaft", KineticBlockEntity::new)
            .visual(() -> SingleAxisRotatingVisual::shaft, false)
            .validBlocks(BnbDecorativeBlocks.WEATHERED_METAL_GIRDER_ENCASED_SHAFT, BnbBlocksBootstrap.INDUSTRIAL_IRON_ENCASED_SHAFT, BnbBlocksBootstrap.WEATHERED_IRON_ENCASED_SHAFT)
            .renderer(() -> ShaftRenderer::new)
            .register();

    public static final BlockEntityEntry<SimpleKineticBlockEntity> ENCASED_COGWHEEL = REGISTRATE
            .blockEntity("encased_cogwheel", SimpleKineticBlockEntity::new)
            .visual(() -> EncasedCogVisual::small, false)
            .validBlocks(BnbBlocksBootstrap.INDUSTRIAL_IRON_ENCASED_COGWHEEL, BnbBlocksBootstrap.WEATHERED_IRON_ENCASED_COGWHEEL)
            .renderer(() -> EncasedCogRenderer::small)
            .register();

    public static final BlockEntityEntry<SimpleKineticBlockEntity> ENCASED_LARGE_COGWHEEL = REGISTRATE
            .blockEntity("encased_large_cogwheel", SimpleKineticBlockEntity::new)
            .visual(() -> EncasedCogVisual::large, false)
            .validBlocks(BnbBlocksBootstrap.INDUSTRIAL_IRON_ENCASED_LARGE_COGWHEEL, BnbBlocksBootstrap.WEATHERED_IRON_ENCASED_LARGE_COGWHEEL)
            .renderer(() -> EncasedCogRenderer::large)
            .register();

    public static final BlockEntityEntry<GenericNixieDisplayBlockEntity> GENERIC_NIXIE_DISPLAY = REGISTRATE.blockEntity("generic_nixie_display", GenericNixieDisplayBlockEntity::new)
            .validBlocks(BnbBlocksBootstrap.NIXIE_BOARD, BnbBlocksBootstrap.LARGE_NIXIE_TUBE)
            .validBlocks(BnbBlocksBootstrap.DYED_NIXIE_BOARD.toArray())
            .validBlocks(BnbBlocksBootstrap.DYED_LARGE_NIXIE_TUBE.toArray())
            .renderer(() -> GenericNixieDisplayBoardRenderer::new)
            .register();

    public static final BlockEntityEntry<GirderStrutBlockEntity> GIRDER_STRUT = REGISTRATE.blockEntity("girder_strut", GirderStrutBlockEntity::new)
            .validBlocks(BnbDecorativeBlocks.GIRDER_STRUT, BnbDecorativeBlocks.WEATHERED_GIRDER_STRUT, BnbDecorativeBlocks.WOODEN_GIRDER_STRUT, BnbDecorativeBlocks.CABLE_GIRDER_STRUT)
            .renderer(() -> GirderStrutBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<CogwheelChainBlockEntity> COGWHEEL_CHAIN = REGISTRATE.blockEntity("cogwheel_chain", CogwheelChainBlockEntity::new)
            .visual(() -> (context, blockEntity, partialTick) -> {
                Model model = Models.partial(GenericBlockEntityRenderModels.REGISTRY.get(blockEntity.getBlockState().getBlock()));
                return new SingleAxisRotatingVisual<>(context, blockEntity, partialTick, model);
            }, true)
            .validBlocks(BnbBlocksBootstrap.SMALL_COGWHEEL_CHAIN, BnbBlocksBootstrap.LARGE_COGWHEEL_CHAIN, BnbBlocksBootstrap.SMALL_FLANGED_COGWHEEL_CHAIN, BnbBlocksBootstrap.LARGE_FLANGED_COGWHEEL_CHAIN)
            .validBlocks(BnbBlocksBootstrap.ENCASED_LARGE_CHAIN_COGWHEEL.toArray())
            .validBlocks(BnbBlocksBootstrap.ENCASED_LARGE_FLANGED_CHAIN_COGWHEEL.toArray())
            .validBlocks(BnbBlocksBootstrap.ENCASED_CHAIN_COGWHEEL.toArray())
            .validBlocks(BnbBlocksBootstrap.ENCASED_FLANGED_CHAIN_COGWHEEL.toArray())
            .renderer(() -> CogwheelChainBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<FlywheelBearingBlockEntity> FLYWHEEL_BEARING = REGISTRATE
            .blockEntity("flywheel_bearing", FlywheelBearingBlockEntity::new)
            .validBlocks(BnbBlocksBootstrap.FLYWHEEL_BEARING)
            .renderer(() -> FlywheelBearingBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<ChainPulleyBlockEntity> CHAIN_ROPE_PULLEY = REGISTRATE
            .blockEntity("chain_pulley", ChainPulleyBlockEntity::new)
//            .visual(() -> ChainPulleyVisual::new, false)
            .validBlocks(BnbBlocksBootstrap.CHAIN_PULLEY)
            .renderer(() -> ChainPulleyRenderer::new)
            .register();

    public static final BlockEntityEntry<SimpleKineticBlockEntity> EMPTY_FLANGED_COGWHEEL = REGISTRATE.blockEntity("empty_flanged_cogwheel", SimpleKineticBlockEntity::new)
            .visual(() -> (context, blockEntity, partialTick) ->
                    new SingleAxisRotatingVisual<>(context, blockEntity, partialTick,
                            Models.partial(GenericBlockEntityRenderModels.REGISTRY.get(blockEntity.getBlockState().getBlock()))), true)
            .validBlocks(BnbBlocksBootstrap.SMALL_EMPTY_FLANGED_COGWHEEL, BnbBlocksBootstrap.LARGE_EMPTY_FLANGED_COGWHEEL)
            .validBlocks(BnbBlocksBootstrap.ENCASED_LARGE_EMPTY_FLANGED_COGWHEEL.toArray())
            .validBlocks(BnbBlocksBootstrap.ENCASED_EMPTY_FLANGED_COGWHEEL.toArray())
            .renderer(() -> KineticBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<ThrottleLeverBlockEntity> THROTTLE_LEVER = REGISTRATE
            .blockEntity("throttle_lever", ThrottleLeverBlockEntity::new)
            .validBlocks(BnbBlocksBootstrap.THROTTLE_LEVER)
            .renderer(() -> ThrottleLeverBlockEntityRenderer::new)
            .register();

    public static void register() {
    }

}


