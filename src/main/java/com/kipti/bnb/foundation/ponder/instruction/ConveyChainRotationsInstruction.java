package com.kipti.bnb.foundation.ponder.instruction;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour.CogwheelChainBehaviour;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PathedCogwheelNode;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.instruction.PonderInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;

public class ConveyChainRotationsInstruction extends PonderInstruction {

    private final BlockPos chainStartLocation;
    private final float rpm;

    public ConveyChainRotationsInstruction(final BlockPos chainStartLocation, final float rpm) {
        this.chainStartLocation = chainStartLocation;
        this.rpm = rpm;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void tick(final PonderScene scene) {
        final BlockEntity blockEntity = scene.getWorld().getBlockEntity(chainStartLocation);
        if (!(blockEntity instanceof final KineticBlockEntity kineticBlockEntity)) {
            CreateBitsnBobs.LOGGER.warn("Could not find kinetic chain block entity at {}, skipping instruction", chainStartLocation);
            return;
        }
        final CogwheelChainBehaviour behaviour = CogwheelChainBehaviour.getOrThrow(scene.getWorld(), chainStartLocation, CogwheelChainBehaviour.TYPE);
        if (behaviour.isController()) {
            conveyRotationsFromController(behaviour, kineticBlockEntity, rpm);
        } else {
            final Vec3i offset = behaviour.getControllerOffset();
            if (offset == null) {
                CreateBitsnBobs.LOGGER.warn("Could not convey rotations for ponder chain block entity at {} as it had no controller offset set", chainStartLocation);
                return;
            }
            final BlockEntity controllerBlockEntity = scene.getWorld().getBlockEntity(chainStartLocation.offset(offset));
            if (!(controllerBlockEntity instanceof final KineticBlockEntity kineticControllerBlockEntity)) {
                CreateBitsnBobs.LOGGER.warn("Could not find kinetic block entity for controller at {}, skipping instruction", chainStartLocation.offset(offset));
                return;
            }
            final CogwheelChainBehaviour controllerBehaviour = behaviour.getComplementaryBehaviourOrThrow(controllerBlockEntity);
            conveyRotationsFromController(controllerBehaviour, kineticControllerBlockEntity, rpm);
        }
    }

    private void conveyRotationsFromController(final CogwheelChainBehaviour controllerBehaviour, final KineticBlockEntity kineticBlockEntity, final float rpm) {
        final BlockPos chainControllerPos = controllerBehaviour.getPos();
        final float initialChainRotationFactor = controllerBehaviour.getChainRotationFactor();
        final CogwheelChain chain = controllerBehaviour.getControlledChain();
        if (chain == null) {
            CreateBitsnBobs.LOGGER.warn("Ponder block entities chain was null despite it being a controller, ignoring instruction");
            return;
        }
        final Level level = controllerBehaviour.getLevel();

        for (final PathedCogwheelNode chainNode : new ArrayList<>(chain.getChainPathCogwheelNodes())) {
            final BlockPos nodePos = chainControllerPos.offset(chainNode.localPos());
            if (chainNode.localPos().equals(Vec3i.ZERO)) { // Controller node, skip
                continue;
            }

            final BlockEntity blockEntity = controllerBehaviour.getLevel().getBlockEntity(nodePos);
            if (!(blockEntity instanceof final KineticBlockEntity childKineticBlockEntity)) {
                CreateBitsnBobs.LOGGER.warn("Could not find kinetic block entity for chain node at {}, skipping node", nodePos);
                continue;
            }
            final CogwheelChainBehaviour behaviour = controllerBehaviour.getComplementaryBehaviourOrThrow(childKineticBlockEntity);
            modifyBlockEntityKineticRotation(level, childKineticBlockEntity, initialChainRotationFactor, behaviour.getChainRotationFactor(), rpm);
        }
        modifyBlockEntityKineticRotation(level, kineticBlockEntity, -1, initialChainRotationFactor, rpm);
    }

    private void modifyBlockEntityKineticRotation(final Level level, final KineticBlockEntity childCogwheelChainBlockEntity, final float initialFactor, final float factor, final float rpm) {
        if (Math.abs(factor) < 1.0e-6f) {
            CreateBitsnBobs.LOGGER.warn("Skipped kinetic rotation update at {} due to zero chain factor", childCogwheelChainBlockEntity.getBlockPos());
            return;
        }
        final CompoundTag tag = childCogwheelChainBlockEntity.saveWithFullMetadata(level.registryAccess());
        tag.putFloat("Speed", rpm * initialFactor / factor);
        childCogwheelChainBlockEntity.loadWithComponents(tag, level.registryAccess());
    }

    @Override
    public void reset(final PonderScene scene) {
        super.reset(scene);
    }

}

