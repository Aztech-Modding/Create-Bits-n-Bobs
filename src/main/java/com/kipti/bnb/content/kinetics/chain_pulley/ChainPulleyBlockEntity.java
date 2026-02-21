package com.kipti.bnb.content.kinetics.chain_pulley;

import com.kipti.bnb.registry.content.blocks.BnbBlocksBootstrap;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.ContraptionCollider;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.pulley.PulleyBlock;
import com.simibubi.create.content.contraptions.pulley.PulleyBlockEntity;
import com.simibubi.create.content.contraptions.pulley.PulleyContraption;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * Variant of the {@link PulleyBlockEntity} that uses ropes made of chains.
 * No original code here, just having to copy methods to change references to ropes.
 */
public class ChainPulleyBlockEntity extends PulleyBlockEntity {

    public ChainPulleyBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void assemble() throws AssemblyException {
        if (!(level.getBlockState(worldPosition)
                .getBlock() instanceof PulleyBlock))
            return;
        if (speed == 0 && mirrorParent == null)
            return;
        final int maxLength = AllConfigs.server().kinetics.maxRopeLength.get();
        int i = 1;
        while (i <= maxLength) {
            final BlockPos ropePos = worldPosition.below(i);
            final BlockState ropeState = level.getBlockState(ropePos);
            if (!BnbBlocksBootstrap.CHAIN_ROPE.has(ropeState) && !BnbBlocksBootstrap.CHAIN_PULLEY_MAGNET.has(ropeState)) {
                break;
            }
            ++i;
        }
        offset = i - 1;
        if (offset >= getExtensionRange() && getSpeed() > 0)
            return;
        if (offset <= 0 && getSpeed() < 0)
            return;

        // Collect Construct
        if (!level.isClientSide && mirrorParent == null) {
            needsContraption = false;
            final BlockPos anchor = worldPosition.below(Mth.floor(offset + 1));
            initialOffset = Mth.floor(offset);
            final PulleyContraption contraption = new PulleyContraption(initialOffset);
            boolean canAssembleStructure = contraption.assemble(level, anchor);

            if (canAssembleStructure) {
                final Direction movementDirection = getSpeed() > 0 ? Direction.DOWN : Direction.UP;
                if (ContraptionCollider.isCollidingWithWorld(level, contraption, anchor.relative(movementDirection),
                        movementDirection))
                    canAssembleStructure = false;
            }

            if (!canAssembleStructure && getSpeed() > 0)
                return;

            removeRopes();

            if (!contraption.getBlocks()
                    .isEmpty()) {
                contraption.removeBlocksFromWorld(level, BlockPos.ZERO);
                movedContraption = ControlledContraptionEntity.create(level, this, contraption);
                movedContraption.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
                level.addFreshEntity(movedContraption);
                forceMove = true;
                needsContraption = true;

                if (contraption.containsBlockBreakers())
                    award(AllAdvancements.CONTRAPTION_ACTORS);

                for (BlockPos pos : contraption.createColliders(level, Direction.UP)) {
                    if (pos.getY() != 0)
                        continue;
                    pos = pos.offset(anchor);
                    if (level.getBlockEntity(
                            new BlockPos(pos.getX(), worldPosition.getY(), pos.getZ())) instanceof final PulleyBlockEntity pbe)
                        pbe.startMirroringOther(worldPosition);
                }
            }
        }

        if (mirrorParent != null)
            removeRopes();

        clientOffsetDiff = 0;
        running = true;
        sendData();
    }

    private void removeRopes() {
        for (int i = ((int) offset); i > 0; i--) {
            final BlockPos offset = worldPosition.below(i);
            final BlockState oldState = level.getBlockState(offset);
            level.setBlock(offset, oldState.getFluidState()
                    .createLegacyBlock(), 66);
        }
    }


    @Override
    public void disassemble() {
        if (!running && movedContraption == null && mirrorParent == null)
            return;
        offset = getGridOffset(offset);
        if (movedContraption != null)
            resetContraptionToOffset();

        if (!level.isClientSide) {
            if (shouldCreateRopes()) {
                if (offset > 0) {
                    final BlockPos magnetPos = worldPosition.below((int) offset);
                    final FluidState ifluidstate = level.getFluidState(magnetPos);
                    if (level.getBlockState(magnetPos)
                            .getDestroySpeed(level, magnetPos) != -1) {

                        level.destroyBlock(magnetPos, level.getBlockState(magnetPos)
                                .getCollisionShape(level, magnetPos)
                                .isEmpty());
                        level.setBlock(magnetPos, BnbBlocksBootstrap.CHAIN_PULLEY_MAGNET.getDefaultState()
                                        .setValue(BlockStateProperties.WATERLOGGED,
                                                ifluidstate.getType() == Fluids.WATER),
                                66);
                    }
                }

                final boolean[] waterlog = new boolean[(int) offset];

                for (final boolean destroyPass : Iterate.trueAndFalse) {
                    for (int i = 1; i <= ((int) offset) - 1; i++) {
                        final BlockPos ropePos = worldPosition.below(i);
                        if (level.getBlockState(ropePos)
                                .getDestroySpeed(level, ropePos) == -1)
                            continue;

                        if (destroyPass) {
                            final FluidState ifluidstate = level.getFluidState(ropePos);
                            waterlog[i] = ifluidstate.getType() == Fluids.WATER;
                            level.destroyBlock(ropePos, level.getBlockState(ropePos)
                                    .getCollisionShape(level, ropePos)
                                    .isEmpty());
                            continue;
                        }

                        level.setBlock(worldPosition.below(i), BnbBlocksBootstrap.CHAIN_ROPE.getDefaultState()
                                .setValue(BlockStateProperties.WATERLOGGED, waterlog[i]), 66);
                    }
                }

            }

            if (movedContraption != null && mirrorParent == null)
                movedContraption.disassemble();
            notifyMirrorsOfDisassembly();
        }

        if (movedContraption != null)
            movedContraption.discard();

        movedContraption = null;
        initialOffset = 0;
        running = false;
        sendData();
    }

}

