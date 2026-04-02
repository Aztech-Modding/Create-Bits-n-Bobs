package com.kipti.bnb.foundation.ponder.scenes;

import com.cake.azimuth.client.outlines.instructions.ExpandingOutlineInstruction;
import com.kipti.bnb.foundation.ponder.instruction.ConveyChainRotationsInstruction;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.foundation.instruction.BlockEntityDataInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

//ignore-complexity: Ponders are animations not logic
public class CogwheelChainScenes {

    public static void flatCogwheelChain(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("chain_cog_flat", "Connecting cogwheels with a chain");
        scene.configureBasePlate(0, 0, 7);
        scene.world().showSection(util.select().layer(0), Direction.UP);


        scene.world().setKineticSpeed(util.select().position(4, 0, 4), 16f);
        scene.world().setKineticSpeed(util.select().position(4, 1, 4), 16f);

        scene.idle(5);


        scene.world().showSection(util.select().position(4, 1, 4), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(5, 1, 1), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(4, 1, 2), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(1, 1, 2), Direction.DOWN);
        scene.idle(5);
        hideChainFromController(util.select().position(1, 1, 5), scene);
        scene.world().showSection(util.select().position(1, 1, 5), Direction.DOWN);

        scene.idle(20);

        scene.addKeyframe();

        scene.idle(20);

        scene.overlay().showText(70)
                .text("Chains can be used to connect 2 or more cogwheels")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.WEST));

        scene.idle(80);
        scene.overlay().showControls(util.vector().centerOf(4, 1, 4), Pointing.DOWN, 60)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(10);
        scene.overlay().showControls(util.vector().centerOf(5, 1, 1), Pointing.DOWN, 50)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(10);
        scene.overlay().showControls(util.vector().centerOf(4, 1, 2), Pointing.UP, 40)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(10);
        scene.overlay().showControls(util.vector().centerOf(1, 1, 2), Pointing.UP, 30)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(10);
        scene.overlay().showControls(util.vector().centerOf(1, 1, 5), Pointing.LEFT, 20)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(5);
        scene.addKeyframe();
        scene.idle(20);

        //Restore the chain data
        scene.world().restoreBlocks(util.select().layer(1));

        scene.addInstruction(new ConveyChainRotationsInstruction(new BlockPos(4, 1, 4), 32f));
        scene.idle(20);

        scene.addKeyframe();

        scene.overlay().showText(70)
                .text("Gear ratios will be preserved across the chain")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 2), Direction.UP));

        scene.idle(80);

        scene.overlay().showText(70)
                .text("2x RPM")
                .placeNearTarget()
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.UP));

        scene.overlay().showText(70)
                .text("1x RPM")
                .placeNearTarget()
                .colored(PonderPalette.RED)
                .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 2), Direction.UP));

        scene.idle(80);
        scene.markAsFinished();
    }

    private static void hideChainFromController(final Selection selection, final CreateSceneBuilder scene) {
        scene.addInstruction(new BlockEntityDataInstruction(
                selection, KineticBlockEntity.class, (tag) -> {
            tag.remove("Chain");
            return tag;
        }, true
        ));
    }

    public static void changingAxisCogwheelChain(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("chain_cog_changing_axis", "Changing axes on cogwheel chains");
        scene.configureBasePlate(0, 0, 6);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        //Hide the chain from the controller so it doesn't interfere with the demonstration
        hideChainFromController(util.select().position(1, 1, 6), scene);

        //Hide the gearshift
        scene.world().setBlock(util.grid().at(1, 0, 4), Blocks.SNOW_BLOCK.defaultBlockState(), false);

        //Animate in the core 2 cogwheels
        scene.world().showSection(util.select().position(6, 1, 1), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(3, 2, 2), Direction.SOUTH);
        scene.idle(5);
        scene.world().showSection(
                util.select().position(3, 2, 3).add(util.select().position(3, 1, 3)),
                Direction.NORTH
        );
        scene.idle(5);

        scene.addKeyframe();
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Chains can change axis when connecting two large cogwheels")
                .placeNearTarget()
                .pointAt(util.vector().of(5, 1.5, 2.5));

        scene.idle(80);

        scene.addInstruction(new ExpandingOutlineInstruction(
                PonderPalette.BLUE,
                new Vec3(3.5f, 1.5, 2.5),
                new Vec3(6.5f, 1.5, 2.5),
                80,
                20
        ));
        scene.idle(20);

        scene.overlay().showText(70)
                .text("They must share a common tangent that the chain can follow")
                .placeNearTarget()
                .pointAt(util.vector().of(5, 1.5, 2.5));
        scene.idle(20);
        scene.addKeyframe();
        scene.idle(60);

        //Animate in the rest of the cogwheels

        scene.world().showSection(util.select().fromTo(5, 1, 3, 5, 3, 3), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(3, 2, 4), Direction.NORTH);
        scene.idle(5);
        scene.world().showSection(util.select().position(6, 1, 5), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(1, 1, 6), Direction.NORTH);
        scene.idle(5);

        //Power when placing the powering cogwheel
        scene.world().setKineticSpeed(util.select().position(1, 1, 4), 16f);
        scene.world().setKineticSpeed(util.select().position(1, 0, 4), 16f);

        scene.world().restoreBlocks(util.select().position(1, 0, 4));
        scene.world().showSection(util.select().position(1, 0, 4), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(1, 1, 4), Direction.DOWN);
        scene.idle(5);

        scene.world().showSection(util.select().position(1, 1, 1), Direction.DOWN);
        scene.idle(20);
        scene.addKeyframe();

        //Now go around and animate the chain item usage

        final int durationPerChain = 10;

        scene.overlay().showControls(util.vector().centerOf(6, 1, 1), Pointing.RIGHT, durationPerChain * 8)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(durationPerChain);

        scene.overlay().showControls(util.vector().centerOf(3, 2, 2), Pointing.UP, durationPerChain * 7)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(durationPerChain);

        scene.overlay().showControls(util.vector().centerOf(5, 3, 3), Pointing.RIGHT, durationPerChain * 6)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(durationPerChain);

        scene.overlay().showControls(util.vector().centerOf(3, 2, 4), Pointing.LEFT, durationPerChain * 5)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(durationPerChain);

        scene.overlay().showControls(util.vector().centerOf(6, 1, 5), Pointing.DOWN, durationPerChain * 4)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(durationPerChain);

        scene.overlay().showControls(util.vector().centerOf(1, 1, 6), Pointing.LEFT, durationPerChain * 3)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(durationPerChain);

        scene.overlay().showControls(util.vector().centerOf(1, 1, 4), Pointing.LEFT, durationPerChain * 2)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(durationPerChain);

        scene.overlay().showControls(util.vector().centerOf(1, 1, 1), Pointing.UP, durationPerChain)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(durationPerChain + 5);

        scene.addKeyframe();

        //Restore the chain controller block so it can animate properly
        scene.world().restoreBlocks(util.select().position(1, 1, 6));
        scene.addInstruction(new ConveyChainRotationsInstruction(new BlockPos(1, 1, 4), -8f));

        scene.idle(20);
        scene.markAsFinished();
    }

    public static void cogwheelChainPathingBehaviour(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("chain_cog_pathing_behaviour", "Behaviour of chain paths");
        scene.configureBasePlate(0, 0, 8);
        scene.world().showSection(util.select().everywhere(), Direction.UP);

        scene.addInstruction(new ConveyChainRotationsInstruction(new BlockPos(1, 1, 1), 16));

        scene.idle(30);
        scene.overlay().showText(70)
                .text("Chains can follow very complex paths with an unlimited number of axis changes");
        scene.idle(80);

        scene.rotateCameraY(90);

        scene.idle(10);
        scene.addKeyframe();
        scene.idle(30);
        scene.overlay().showText(70)
                .text("Chains will always take the longest path around the cogwheels");
        scene.idle(80);

        scene.rotateCameraY(90);

        scene.idle(10);
        scene.addKeyframe();
        scene.idle(30);
        scene.overlay().showText(70)
                .text("Chains are allowed to self-intersect around nodes, but only when necessary");
        scene.idle(80);

        scene.addKeyframe();

        scene.rotateCameraY(90);
        scene.idle(60);

        scene.rotateCameraY(90);
        scene.idle(40);

        scene.markAsFinished();
    }

}

