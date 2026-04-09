package com.kipti.bnb.foundation.ponder.instruction;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.decoration.dyeable.pipes.DyeablePipeBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.ponder.api.element.WorldSectionElement;
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
        final DyeablePipeBehaviour behaviour = BlockEntityBehaviour.get(level, this.pipePos, DyeablePipeBehaviour.TYPE);
        if (behaviour == null) {
            CreateBitsnBobs.LOGGER.warn(
                    "Could not find dyeable pipe block entity at {}, skipping instruction",
                    this.pipePos
            );
            return;
        }

        final DyeColor previousColor = behaviour.getColor();
        if (previousColor == this.targetColor) {
            return;
        }

        final int particleStateId = Block.getId(level.getBlockState(this.pipePos));

        // Set color visually without triggering gameplay neighbour propagation
        behaviour.applyColorClientOnly(this.targetColor);
        DyeablePipeBehaviour.refreshPipeState(level, this.pipePos, level.getBlockState(this.pipePos), false);
        scene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);

        if (previousColor != null && this.targetColor == null) {
            level.levelEvent(2001, this.pipePos, particleStateId);
        }
    }
}
