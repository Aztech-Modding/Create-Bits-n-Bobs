package com.kipti.bnb.foundation.ponder.scenes;

import com.kipti.bnb.content.decoration.dyeable.pipes.DyeablePipeBehaviour;
import com.kipti.bnb.foundation.ponder.instruction.DyePipeInstruction;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class DyedPipeScenes {

    public static void dyedPipes(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("dyed_pipes", "Dyeing pipes");
        scene.configureBasePlate(0, 0, 4);

        stageUndyedPipes(scene, util);
        stageDyePipes(scene, util);
        stageUndyedCompatibility(scene, util);
        stageOffHandPlacement(scene, util);

        scene.markAsFinished();
    }

    /**
     * Show the initial pipe layout with all dyes cleared to demonstrate automatic connectivity.
     */
    private static void stageUndyedPipes(final CreateSceneBuilder scene, final SceneBuildingUtil util) {
        // Hide pipes that will be placed later in the scene
        scene.world().setBlocks(
                util.select().position(1, 1, 1)
                        .add(util.select().position(3, 1, 2))
                        .add(util.select().position(3, 2, 2))
                        .add(util.select().position(3, 2, 3))
                        .add(util.select().position(2, 2, 3))
                        .add(util.select().position(1, 2, 3))
                        .add(util.select().position(1, 1, 3)),
                Blocks.AIR.defaultBlockState(), false
        );

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);

        scene.world().showSection(
                util.select().fromTo(2, 1, 0, 2, 1, 3)
                        .add(util.select().position(3, 1, 1))
                        .add(util.select().fromTo(0, 1, 2, 1, 1, 2)),
                Direction.DOWN
        );

        scene.addInstruction(new DyePipeInstruction(new BlockPos(0, 1, 2), null));
        scene.addInstruction(new DyePipeInstruction(new BlockPos(1, 1, 2), null));
        scene.addInstruction(new DyePipeInstruction(new BlockPos(2, 1, 0), null));
        scene.addInstruction(new DyePipeInstruction(new BlockPos(2, 1, 1), null));
        scene.addInstruction(new DyePipeInstruction(new BlockPos(2, 1, 2), null));
        scene.addInstruction(new DyePipeInstruction(new BlockPos(2, 1, 3), null));
        scene.addInstruction(new DyePipeInstruction(new BlockPos(3, 1, 1), null));
        refreshAllInitialPipes(scene);
        scene.idle(20);

        scene.addKeyframe();
        scene.overlay().showText(60)
                .text("Pipes with no dye color automatically connect when touching.")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.idle(70);
    }

    /**
     * Apply dye colors in two batches, showing item indicators for each color group.
     */
    private static void stageDyePipes(final CreateSceneBuilder scene, final SceneBuildingUtil util) {
        scene.overlay().showControls(util.vector().centerOf(3, 1, 1), Pointing.DOWN, 50)
                .withItem(Items.PINK_DYE.getDefaultInstance());
        scene.idle(10);

        // Batch 1
        scene.addInstruction(new DyePipeInstruction(new BlockPos(3, 1, 1), DyeColor.PINK));
        scene.idle(3);
        scene.addInstruction(new DyePipeInstruction(new BlockPos(2, 1, 1), DyeColor.PINK));
        scene.idle(3);
        scene.addInstruction(new DyePipeInstruction(new BlockPos(2, 1, 0), DyeColor.PINK));
        scene.idle(3);

        // Batch 2
        scene.overlay().showControls(util.vector().centerOf(2, 1, 3), Pointing.DOWN, 50 - 10 - 3 * 3)
                .withItem(Items.BLUE_DYE.getDefaultInstance());

        scene.idle(10);
        scene.addInstruction(new DyePipeInstruction(new BlockPos(2, 1, 3), DyeColor.BLUE));
        scene.idle(3);
        scene.addInstruction(new DyePipeInstruction(new BlockPos(2, 1, 2), DyeColor.BLUE));
        scene.idle(3);
        scene.addInstruction(new DyePipeInstruction(new BlockPos(1, 1, 2), DyeColor.BLUE));
        scene.idle(3);
        scene.addInstruction(new DyePipeInstruction(new BlockPos(0, 1, 2), DyeColor.BLUE));
        refreshAllInitialPipes(scene);
        scene.idle(30);

        scene.addKeyframe();
        scene.overlay().showText(70)
                .text("Dyed pipes will only automatically connect to pipes of the same color.")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.idle(80);
    }

    /**
     * Show that an undyed pipe bridges any dyed color, then break it.
     */
    private static void stageUndyedCompatibility(final CreateSceneBuilder scene, final SceneBuildingUtil util) {
        scene.world().restoreBlocks(util.select().position(1, 1, 1));
        scene.world().showSection(util.select().position(1, 1, 1), Direction.DOWN);
        refreshPipes(scene, new BlockPos(1, 1, 1), new BlockPos(1, 1, 2), new BlockPos(2, 1, 1));
        scene.idle(10);

        scene.overlay().showText(60)
                .text("However, pipes with no dye color will connect to dyed pipes of any color.")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(1, 1, 1));
        scene.idle(70);

        scene.world().destroyBlock(new BlockPos(1, 1, 1));
        refreshPipes(scene, new BlockPos(1, 1, 2), new BlockPos(2, 1, 1));
        scene.idle(15);
    }

    /**
     * Demonstrate off-hand dye placement and build the upper red branch pipe-by-pipe.
     */
    private static void stageOffHandPlacement(final CreateSceneBuilder scene, final SceneBuildingUtil util) {
        scene.addKeyframe();

        scene.world().restoreBlocks(util.select().position(3, 1, 2));
        scene.world().showSection(util.select().position(3, 1, 2), Direction.DOWN);
        refreshPipes(scene, new BlockPos(3, 1, 2));
        scene.idle(10);

        scene.overlay().showText(70)
                .text("Placing a pipe with dye in the off-hand will immediately apply the dye color to the pipe.")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(3, 1, 2));
        scene.idle(80);

        // Build the rest of the red branch sequentially
        scene.world().restoreBlocks(util.select().position(3, 2, 2));
        scene.world().showSection(util.select().position(3, 2, 2), Direction.DOWN);
        refreshPipes(scene, new BlockPos(3, 2, 2), new BlockPos(3, 1, 2));
        scene.idle(4);

        scene.world().restoreBlocks(util.select().position(3, 2, 3));
        scene.world().showSection(util.select().position(3, 2, 3), Direction.DOWN);
        refreshPipes(scene, new BlockPos(3, 2, 3), new BlockPos(3, 2, 2));
        scene.idle(4);

        scene.world().restoreBlocks(util.select().position(2, 2, 3));
        scene.world().showSection(util.select().position(2, 2, 3), Direction.DOWN);
        refreshPipes(scene, new BlockPos(2, 2, 3), new BlockPos(3, 2, 3));
        scene.idle(4);

        scene.world().restoreBlocks(util.select().position(1, 2, 3));
        scene.world().showSection(util.select().position(1, 2, 3), Direction.DOWN);
        refreshPipes(scene, new BlockPos(1, 2, 3), new BlockPos(2, 2, 3));
        scene.idle(4);

        scene.world().restoreBlocks(util.select().position(1, 1, 3));
        scene.world().showSection(util.select().position(1, 1, 3), Direction.DOWN);
        refreshPipes(scene, new BlockPos(1, 1, 3), new BlockPos(1, 2, 3));
        scene.idle(20);
    }

    private static void refreshAllInitialPipes(final CreateSceneBuilder scene) {
        refreshPipes(
                scene,
                new BlockPos(0, 1, 2), new BlockPos(1, 1, 2), new BlockPos(2, 1, 0),
                new BlockPos(2, 1, 1), new BlockPos(2, 1, 2), new BlockPos(2, 1, 3),
                new BlockPos(3, 1, 1)
        );
    }

    private static void refreshPipes(final CreateSceneBuilder scene, final BlockPos... positions) {
        scene.addInstruction(ponderScene -> {
            for (final BlockPos pos : positions) {
                DyeablePipeBehaviour.refreshPipeState(
                        ponderScene.getWorld(), pos, ponderScene.getWorld().getBlockState(pos), false);
            }
            ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
        });
    }

}
