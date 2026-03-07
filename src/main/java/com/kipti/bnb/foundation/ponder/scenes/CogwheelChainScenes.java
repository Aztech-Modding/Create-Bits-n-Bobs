package com.kipti.bnb.foundation.ponder.scenes;

import com.cake.azimuth.client.outlines.instructions.ExpandingOutlineInstruction;
import com.kipti.bnb.foundation.ponder.instruction.ConveyChainRotationsInstruction;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import static com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS;

public class CogwheelChainScenes {

    public static void flatCogwheelChain(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("chain_cog_flat", "Connecting cogwheels with a chain");
        scene.configureBasePlate(0, 0, 7);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        scene.world().setBlock(new BlockPos(1, 1, 1), AllBlocks.COGWHEEL.getDefaultState(), false);
        scene.world().setBlock(new BlockPos(2, 1, 2), AllBlocks.COGWHEEL.getDefaultState(), false);
        scene.world().setBlock(new BlockPos(4, 1, 5), AllBlocks.COGWHEEL.getDefaultState(), false);
        scene.world().setBlock(new BlockPos(4, 1, 2), AllBlocks.LARGE_COGWHEEL.getDefaultState(), false);
        scene.world().setBlock(new BlockPos(2, 1, 4), AllBlocks.LARGE_COGWHEEL.getDefaultState(), false);

        scene.world().setKineticSpeed(util.select().position(4, 0, 2), 16f);
        scene.world().setKineticSpeed(util.select().position(4, 1, 2), 16f);

        scene.idle(5);

        scene.world().showSection(util.select().position(4, 1, 2), Direction.DOWN);

        scene.idle(5);
        scene.world().showSection(util.select().position(1, 1, 1), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(2, 1, 2), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(4, 1, 5), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(2, 1, 4), Direction.DOWN);
        scene.idle(20);

        scene.addKeyframe();

        scene.idle(20);

        scene.overlay().showText(70)
                .text("Chains can be used to connect 2 or more cogwheels")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(util.grid().at(4, 1, 2), Direction.WEST));

        scene.idle(80);
        scene.overlay().showControls(util.vector().centerOf(4, 1, 2), Pointing.RIGHT, 60)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(10);
        scene.overlay().showControls(util.vector().centerOf(2, 1, 2), Pointing.DOWN, 50)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(10);
        scene.overlay().showControls(util.vector().centerOf(1, 1, 1), Pointing.UP, 40)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(10);
        scene.overlay().showControls(util.vector().centerOf(2, 1, 4), Pointing.LEFT, 30)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(10);
        scene.overlay().showControls(util.vector().centerOf(4, 1, 5), Pointing.DOWN, 20)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(5);
        scene.addKeyframe();
        scene.idle(20);

        scene.world().restoreBlocks(util.select().layer(1));

        scene.world().setKineticSpeed(util.select().position(4, 1, 2), 16f);
        scene.world().setKineticSpeed(util.select().position(2, 1, 2), -32f);
        scene.world().setKineticSpeed(util.select().position(1, 1, 1), 32f);
        scene.world().setKineticSpeed(util.select().position(2, 1, 4), 16f);
        scene.world().setKineticSpeed(util.select().position(4, 1, 5), 32f);
        scene.idle(20);

        scene.addKeyframe();

        scene.overlay().showText(70)
                .text("Gear ratios will be preserved across the chain")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 1), Direction.UP));

        scene.idle(80);
        scene.markAsFinished();
    }

    public static void changingAxisCogwheelChain(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("chain_cog_changing_axis", "Changing axes on cogwheel chains");
        scene.configureBasePlate(0, 0, 6);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        //Turn the chain cogwheels into normal ones
        scene.world().setBlock(new BlockPos(4, 1, 1), AllBlocks.LARGE_COGWHEEL.getDefaultState(), false);
        scene.world().setBlock(new BlockPos(1, 2, 2), AllBlocks.LARGE_COGWHEEL.getDefaultState().setValue(AXIS, Direction.Axis.Z), false);

        scene.world().setBlock(new BlockPos(2, 2, 4), AllBlocks.LARGE_COGWHEEL.getDefaultState().setValue(AXIS, Direction.Axis.Z), false);

        scene.world().setBlock(new BlockPos(2, 1, 0), AllBlocks.COGWHEEL.getDefaultState(), false);
        scene.world().setBlock(new BlockPos(0, 1, 1), AllBlocks.COGWHEEL.getDefaultState(), false);
        scene.world().setBlock(new BlockPos(0, 1, 5), AllBlocks.COGWHEEL.getDefaultState(), false);

        scene.world().setBlock(new BlockPos(4, 1, 5), AllBlocks.LARGE_COGWHEEL.getDefaultState(), false);
        scene.world().setBlock(new BlockPos(4, 3, 3), AllBlocks.LARGE_COGWHEEL.getDefaultState(), false);

        //Power and place the initial two
        scene.world().setKineticSpeed(util.select().position(4, 1, 1), 16f);
        scene.world().setKineticSpeed(util.select().position(4, 0, 1), 16f);

        scene.idle(5);
        scene.world().showSection(util.select().position(4, 1, 1), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(1, 2, 2), Direction.DOWN);
        scene.idle(10);

        //Start of the ponder
        scene.addKeyframe();
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Chains can change axis when connecting two large cogwheels")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(1, 2, 2));

        scene.idle(80);

        scene.addKeyframe();
        scene.idle(5);

        scene.addInstruction(new ExpandingOutlineInstruction(PonderPalette.BLUE, new Vec3(5.5f, 1.5, 2.5), new Vec3(0.5f, 1.5, 2.5), 80, 20));
        scene.idle(20);

        scene.overlay().showText(70)
                .text("They must share a common tangent that the chain can follow")
                .placeNearTarget()
                .pointAt(util.vector().of(3, 1.5, 2.5));
        scene.idle(80);

        //Animate in the rest of the cogwheels
        scene.world().showSection(util.select().position(2, 1, 0), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(0, 1, 1), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(0, 1, 5), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(4, 1, 5), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(4, 3, 3), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(2, 2, 4), Direction.NORTH);
        scene.idle(5);

        scene.addKeyframe();

        //Show chain placement
        scene.overlay().showControls(util.vector().centerOf(4, 1, 1), Pointing.RIGHT, 60)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(5);
        scene.overlay().showControls(util.vector().centerOf(2, 1, 0), Pointing.RIGHT, 55)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(5);
        scene.overlay().showControls(util.vector().centerOf(0, 1, 1), Pointing.UP, 50)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(5);
        scene.overlay().showControls(util.vector().centerOf(0, 1, 5), Pointing.UP, 45)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(5);
        scene.overlay().showControls(util.vector().centerOf(4, 1, 5), Pointing.DOWN, 40)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(5);
        scene.overlay().showControls(util.vector().centerOf(2, 2, 4), Pointing.LEFT, 35)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(5);
        scene.overlay().showControls(util.vector().centerOf(4, 3, 3), Pointing.RIGHT, 30)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(5);
        scene.overlay().showControls(util.vector().centerOf(1, 2, 2), Pointing.RIGHT, 25)
                .withItem(Items.CHAIN.getDefaultInstance());
        scene.idle(30);

        //"Place" the chains
        scene.world().restoreBlocks(util.select().everywhere());
        scene.world().setKineticSpeed(util.select().everywhere(), 16f);
        //Fix more specific speeds
        scene.world().setKineticSpeed(util.select().position(2, 1, 0), 32f);
        scene.world().setKineticSpeed(util.select().position(0, 1, 1), 32f);
        scene.world().setKineticSpeed(util.select().position(0, 1, 5), 32f);
        scene.world().setKineticSpeed(util.select().position(2, 2, 4), -16f);

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

