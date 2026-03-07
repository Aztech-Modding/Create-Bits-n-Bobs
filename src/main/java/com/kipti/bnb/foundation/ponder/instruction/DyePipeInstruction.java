package com.kipti.bnb.foundation.ponder.instruction;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.dyeable_pipes.DyeablePipeBehaviour;
import net.createmod.ponder.api.element.WorldSectionElement;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.instruction.PonderInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

/**
 * Changes the dye color on a pipe in the world to a dye or null (no dye), will update the block shape too
 */
public class DyePipeInstruction extends PonderInstruction {

    private final BlockPos pipePos;
    @Nullable
    private final DyeColor targetColor;

    public DyePipeInstruction(final BlockPos pipePos, @Nullable final DyeColor targetColor) {
        this.pipePos = pipePos;
        this.targetColor = targetColor;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void tick(final PonderScene scene) {
        final Level level = scene.getWorld();
        final DyeablePipeBehaviour behaviour = BlockEntityBehaviour.get(level, pipePos, DyeablePipeBehaviour.TYPE);
        if (behaviour == null) {
            CreateBitsnBobs.LOGGER.warn("Could not find dyeable pipe block entity at {}, skipping instruction", pipePos);
            return;
        }

        final DyeColor previousColor = behaviour.getColor();
        if (previousColor == targetColor) {
            return;
        }

        final int particleStateId = Block.getId(level.getBlockState(pipePos));

        // Set color visually without triggering gameplay neighbour propagation
        behaviour.applyColorClientOnly(targetColor);
        DyeablePipeBehaviour.refreshPipeState(level, pipePos, level.getBlockState(pipePos), false);
        scene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);

        if (previousColor != null && targetColor == null) {
            level.levelEvent(2001, pipePos, particleStateId);
        }
    }
}
