package com.kipti.bnb.foundation.ponder.instruction;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.kinetics.cogwheel_chain.block.CogwheelChainBlockEntity;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PathedCogwheelNode;
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
    public void tick(PonderScene scene) {
        final BlockEntity chainStartBlockEntity = scene.getWorld().getBlockEntity(chainStartLocation);
        if (chainStartBlockEntity instanceof final CogwheelChainBlockEntity cogwheelChainBlockEntity) {
            if (cogwheelChainBlockEntity.isController()) {
                conveyRotationsFromController(cogwheelChainBlockEntity, rpm);
            } else {
                final Vec3i offset = cogwheelChainBlockEntity.getControllerOffset();
                if (offset == null) {
                    CreateBitsnBobs.LOGGER.warn("Could not convey rotations for ponder chain block entity at {} as it had no controller offset set", chainStartLocation);
                    return;
                }
                final BlockPos controllerPos = chainStartLocation.offset(offset);
                final BlockEntity controllerBlockEntity = scene.getWorld().getBlockEntity(controllerPos);
                if (controllerBlockEntity instanceof final CogwheelChainBlockEntity controllerCogwheelChainBlockEntity) {
                    conveyRotationsFromController(controllerCogwheelChainBlockEntity, rpm);
                }
            }
        }
    }

    private void conveyRotationsFromController(final CogwheelChainBlockEntity controllerChainBlockEntity, final float rpm) {
        final BlockPos chainControllerPos = controllerChainBlockEntity.getBlockPos();
        final float initialChainRotationFactor = controllerChainBlockEntity.getChainRotationFactor();
        final CogwheelChain chain = controllerChainBlockEntity.getChain();
        if (chain == null) {
            CreateBitsnBobs.LOGGER.warn("Ponder block entities chain was null despite it being a controller, ignoring instruction");
            return;
        }
        final Level level = controllerChainBlockEntity.getLevel();

        for (final PathedCogwheelNode chainNode : new ArrayList<>(chain.getChainPathCogwheelNodes())) {
            final BlockPos nodePos = chainControllerPos.offset(chainNode.localPos());
            if (chainNode.localPos() == Vec3i.ZERO) { // Controller node, skip
                continue;
            }

            final BlockEntity blockEntity = controllerChainBlockEntity.getLevel().getBlockEntity(nodePos);
            if (!(blockEntity instanceof CogwheelChainBlockEntity childCogwheelChainBlockEntity)) {
                continue;
            }
            modifyBlockEntityKineticRotation(level, childCogwheelChainBlockEntity, initialChainRotationFactor, childCogwheelChainBlockEntity.getChainRotationFactor(), rpm);
        }
        modifyBlockEntityKineticRotation(level, controllerChainBlockEntity, -1, initialChainRotationFactor, rpm);
    }

    private void modifyBlockEntityKineticRotation(final Level level, final CogwheelChainBlockEntity childCogwheelChainBlockEntity, final float initialFactor, final float factor, final float rpm) {
        final CompoundTag tag = childCogwheelChainBlockEntity.saveWithFullMetadata(level.registryAccess());
        tag.putFloat("Speed", rpm * initialFactor / factor);
        childCogwheelChainBlockEntity.loadWithComponents(tag, level.registryAccess());

    }

    @Override
    public void reset(final PonderScene scene) {
        super.reset(scene);
    }

}
