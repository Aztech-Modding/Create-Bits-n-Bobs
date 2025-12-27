package com.kipti.bnb.registry;

import com.kipti.bnb.content.decoration.girder_strut.GirderStrutBlockEntity;
import com.kipti.bnb.content.decoration.girder_strut.GirderStrutBlockEntityRenderer;
import com.kipti.bnb.content.decoration.light.headlamp.HeadlampBlockEntity;
import com.kipti.bnb.content.decoration.nixie.foundation.GenericNixieDisplayBlockEntity;
import com.kipti.bnb.content.decoration.nixie.foundation.GenericNixieDisplayBoardRenderer;
import com.kipti.bnb.content.kinetics.chain_pulley.ChainPulleyBlockEntity;
import com.kipti.bnb.content.kinetics.chain_pulley.ChainPulleyRenderer;
import com.kipti.bnb.content.kinetics.cogwheel_chain.block.CogwheelChainBlockEntity;
import com.kipti.bnb.content.kinetics.cogwheel_chain.block.CogwheelChainBlockEntityRenderer;
import com.kipti.bnb.content.kinetics.flywheel_bearing.FlywheelBearingBlockEntity;
import com.kipti.bnb.content.kinetics.flywheel_bearing.FlywheelBearingBlockEntityRenderer;
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

public class BnbBlockEntities {

    public static final BlockEntityEntry<HeadlampBlockEntity> HEADLAMP = REGISTRATE.blockEntity("headlamp", HeadlampBlockEntity::new)
            .validBlock(BnbBlocks.HEADLAMP)
            .register();

    public static final BlockEntityEntry<KineticBlockEntity> ENCASED_SHAFT = REGISTRATE
            .blockEntity("encased_shaft", KineticBlockEntity::new)
            .visual(() -> SingleAxisRotatingVisual::shaft, false)
            .validBlocks(BnbBlocks.WEATHERED_METAL_GIRDER_ENCASED_SHAFT, BnbBlocks.INDUSTRIAL_IRON_ENCASED_SHAFT, BnbBlocks.WEATHERED_IRON_ENCASED_SHAFT)
            .renderer(() -> ShaftRenderer::new)
            .register();

    public static final BlockEntityEntry<SimpleKineticBlockEntity> ENCASED_COGWHEEL = REGISTRATE
            .blockEntity("encased_cogwheel", SimpleKineticBlockEntity::new)
            .visual(() -> EncasedCogVisual::small, false)
            .validBlocks(BnbBlocks.INDUSTRIAL_IRON_ENCASED_COGWHEEL, BnbBlocks.WEATHERED_IRON_ENCASED_COGWHEEL)
            .renderer(() -> EncasedCogRenderer::small)
            .register();

    public static final BlockEntityEntry<SimpleKineticBlockEntity> ENCASED_LARGE_COGWHEEL = REGISTRATE
            .blockEntity("encased_large_cogwheel", SimpleKineticBlockEntity::new)
            .visual(() -> EncasedCogVisual::large, false)
            .validBlocks(BnbBlocks.INDUSTRIAL_IRON_ENCASED_LARGE_COGWHEEL, BnbBlocks.WEATHERED_IRON_ENCASED_LARGE_COGWHEEL)
            .renderer(() -> EncasedCogRenderer::large)
            .register();

    public static final BlockEntityEntry<GenericNixieDisplayBlockEntity> GENERIC_NIXIE_DISPLAY = REGISTRATE.blockEntity("generic_nixie_display", GenericNixieDisplayBlockEntity::new)
            .validBlocks(BnbBlocks.NIXIE_BOARD, BnbBlocks.LARGE_NIXIE_TUBE)
            .validBlocks(BnbBlocks.DYED_NIXIE_BOARD.toArray())
            .validBlocks(BnbBlocks.DYED_LARGE_NIXIE_TUBE.toArray())
            .renderer(() -> GenericNixieDisplayBoardRenderer::new)
            .register();

    public static final BlockEntityEntry<GirderStrutBlockEntity> GIRDER_STRUT = REGISTRATE.blockEntity("girder_strut", GirderStrutBlockEntity::new)
            .validBlocks(BnbBlocks.GIRDER_STRUT, BnbBlocks.WEATHERED_GIRDER_STRUT)
            .renderer(() -> GirderStrutBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<CogwheelChainBlockEntity> COGWHEEL_CHAIN = REGISTRATE.blockEntity("cogwheel_chain", CogwheelChainBlockEntity::new)
            .visual(() -> (context, blockEntity, partialTick) -> {
                Model model = Models.partial(
                        blockEntity.getBlockState().is(BnbBlocks.SMALL_SPROCKET_COGWHEEL_CHAIN) ? BnbPartialModels.SMALL_SPROCKET_COGWHEEL_BLOCK :
                                blockEntity.getBlockState().is(BnbBlocks.LARGE_SPROCKET_COGWHEEL_CHAIN) ? BnbPartialModels.LARGE_SPROCKET_COGWHEEL_BLOCK :
                                        blockEntity.getBlockState().is(BnbBlocks.SMALL_FLANGED_COGWHEEL_CHAIN) ? BnbPartialModels.SMALL_FLANGED_COGWHEEL_BLOCK : BnbPartialModels.LARGE_FLANGED_COGWHEEL_BLOCK
                );
                return new SingleAxisRotatingVisual<>(context, blockEntity, partialTick, model);
            }, true)
            .validBlocks(BnbBlocks.SMALL_SPROCKET_COGWHEEL_CHAIN, BnbBlocks.LARGE_SPROCKET_COGWHEEL_CHAIN, BnbBlocks.SMALL_FLANGED_COGWHEEL_CHAIN, BnbBlocks.LARGE_FLANGED_COGWHEEL_CHAIN)
            .renderer(() -> CogwheelChainBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<FlywheelBearingBlockEntity> FLYWHEEL_BEARING = REGISTRATE
            .blockEntity("flywheel_bearing", FlywheelBearingBlockEntity::new)
            .validBlocks(BnbBlocks.FLYWHEEL_BEARING)
            .renderer(() -> FlywheelBearingBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<ChainPulleyBlockEntity> CHAIN_ROPE_PULLEY = REGISTRATE
            .blockEntity("chain_pulley", ChainPulleyBlockEntity::new)
//            .visual(() -> ChainPulleyVisual::new, false)
            .validBlocks(BnbBlocks.CHAIN_PULLEY)
            .renderer(() -> ChainPulleyRenderer::new)
            .register();

    public static final BlockEntityEntry<KineticBlockEntity> EMPTY_FLANGED_COGWHEEL = REGISTRATE.blockEntity("empty_flanged_cogwheel", KineticBlockEntity::new)
            .visual(() -> (context, blockEntity, partialTick) ->
                    new SingleAxisRotatingVisual<>(context, blockEntity, partialTick,
                            Models.partial(blockEntity.getBlockState().is(BnbBlocks.SMALL_EMPTY_FLANGED_COGWHEEL) ? BnbPartialModels.SMALL_FLANGED_COGWHEEL_BLOCK : BnbPartialModels.LARGE_FLANGED_COGWHEEL_BLOCK)), true)
            .validBlocks(BnbBlocks.SMALL_EMPTY_FLANGED_COGWHEEL, BnbBlocks.LARGE_EMPTY_FLANGED_COGWHEEL)
            .renderer(() -> KineticBlockEntityRenderer::new)
            .register();

    public static void register() {
    }

}
