package com.kipti.bnb.foundation.ponder.scenes;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/**
 * Ponder scenes demonstrating Girder Strut placement mechanics.
 * Registered for all 3 strut variants (industrial, weathered, cable) with variant-specific schematics.
 */
//ignore-complexity: Ponders are animations not logic
public class GirderStrutScenes {

    public static void girderStrut(final SceneBuilder builder,
                                   final SceneBuildingUtil util,
                                   final BlockEntry<?> strutBlock) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        final ItemStack strutItem = strutBlock.asStack();

        scene.title("girder_strut", "Placing Girder Struts");
        scene.configureBasePlate(0, 0, 6);

        //1. Setup
        scene.world().setBlock(util.grid().at(2, 0, 1), Blocks.SNOW_BLOCK.defaultBlockState(), false);
        scene.showBasePlate();
        scene.idle(5);

        scene.world().showSection(util.select().fromTo(4, 1, 4, 4, 3, 4), Direction.DOWN);
        scene.idle(20);

        //2. First strut placement
        scene.overlay().showText(50)
                .text("Struts can be used to span a gap between two blocks")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().of(2.0, 2.0, 4.0));
        scene.idle(60);

        scene.overlay().showControls(util.vector().topOf(1, 0, 4), Pointing.DOWN, 30)
                .rightClick()
                .withItem(strutItem);
        scene.idle(10);

        scene.overlay().showControls(util.vector().of(4, 3.5, 4.5), Pointing.LEFT, 20)
                .rightClick()
                .withItem(strutItem);
        scene.idle(22);

        scene.world().showSection(util.select().fromTo(1, 1, 4, 3, 3, 4), Direction.DOWN);
        scene.idle(30);

        //3. Second strut at different angle
        scene.world().showSection(util.select().fromTo(4, 1, 1, 4, 4, 1), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(50)
                .text("Struts can be placed at different angles")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().of(2.5, 2.5, 1.0));
        scene.idle(60);

        scene.world().restoreBlocks(util.select().position(2, 0, 1));
        scene.world().showSection(util.select().position(2, 0, 1), Direction.DOWN);

        scene.idle(20);

        scene.overlay().showControls(util.vector().topOf(2, 0, 1), Pointing.DOWN, 30)
                .rightClick()
                .withItem(strutItem);
        scene.idle(10);

        scene.overlay().showControls(util.vector().of(4, 4.5, 1.5), Pointing.LEFT, 20)
                .rightClick()
                .withItem(strutItem);
        scene.idle(22);

        scene.world().showSection(util.select().fromTo(2, 1, 1, 3, 4, 1), Direction.DOWN);
        scene.idle(30);

        //4. Straightness constraint
        scene.overlay().showText(70)
                .text("However, Struts can only move in 2 axes at the same time, meaning they cannot bend.")
                .attachKeyFrame()
                .colored(PonderPalette.RED)
                .placeNearTarget()
                .pointAt(util.vector().of(2.5, 2.5, 1.0));
        scene.idle(80);

        scene.markAsFinished();
    }
}
