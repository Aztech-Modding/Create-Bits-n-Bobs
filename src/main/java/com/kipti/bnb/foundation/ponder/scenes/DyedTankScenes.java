package com.kipti.bnb.foundation.ponder.scenes;

import com.kipti.bnb.content.decoration.dyeable.fluid_tank.DyeableFluidTankBehaviour;
import com.kipti.bnb.content.decoration.dyeable.pipes.DyeablePipeBehaviour;
import com.kipti.bnb.foundation.ponder.instruction.DyeTankInstruction;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

public class DyedTankScenes {

    public static void dyedTank(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("dyed_tank", "Dyeing tanks");
        scene.configureBasePlate(0, 0, 6);

        stageSetup(scene, util);
        stageDyeTanks(scene, util);
        stagePipeConnectivity(scene, util);

        scene.markAsFinished();
    }

    /**
     * Clear everything except the big tank, undye it, and bring it in from above.
     */
    private static void stageSetup(final CreateSceneBuilder scene, final SceneBuildingUtil util) {
        scene.world().setBlocks(
                util.select().fromTo(0, 1, 0, 0, 2, 0)
                        .add(util.select().position(1, 1, 5))
                        .add(util.select().fromTo(1, 2, 3, 1, 2, 5))
                        .add(util.select().position(5, 1, 1))
                        .add(util.select().fromTo(3, 2, 1, 5, 2, 1)),
                Blocks.AIR.defaultBlockState(), false
        );

        dyeTankRegion(scene, 2, 1, 2, 4, 4, 4, null);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        scene.world().showSection(util.select().fromTo(2, 1, 2, 4, 4, 4), Direction.DOWN);
        scene.idle(30);
    }

    /**
     * Dye the big tank red, then dye a single block of the small tank to show mismatched connectivity.
     */
    private static void stageDyeTanks(final CreateSceneBuilder scene, final SceneBuildingUtil util) {
        scene.addKeyframe();

        scene.overlay().showText(70)
                .text("Fluid Tanks can be recolored by applying Dye")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(3, 3, 3));
        scene.idle(30);

        scene.overlay().showControls(util.vector().topOf(3, 4, 3), Pointing.DOWN, 40)
                .rightClick()
                .withItem(Items.RED_DYE.getDefaultInstance());
        scene.idle(7);

        dyeTankRegion(scene, 2, 1, 2, 4, 4, 4, DyeColor.RED);
        scene.idle(50);

        scene.world().restoreBlocks(util.select().fromTo(0, 1, 0, 0, 2, 0));
        final ElementLink<WorldSectionElement> smallTank = scene.world().showIndependentSection(
                util.select().fromTo(0, 1, 0, 0, 2, 0), Direction.DOWN
        );
        scene.idle(30);

        scene.overlay().showText(80)
                .text("Sneak to apply Dye to a single block within a Tank")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(0, 2, 0))
                .attachKeyFrame();
        scene.idle(30);

        scene.overlay().showControls(util.vector().topOf(0, 2, 0), Pointing.DOWN, 40)
                .rightClick()
                .withItem(Items.RED_DYE.getDefaultInstance());
        scene.idle(7);

        scene.addInstruction(new DyeTankInstruction(new BlockPos(0, 2, 0), DyeColor.RED));
        splitTankMultiBlock(scene, new BlockPos(0, 1, 0), new BlockPos(0, 2, 0));
        scene.idle(50);

        scene.overlay().showText(70)
                .text("Dyed tanks will not connect to tanks without a matching dye")
                .placeNearTarget()
                .pointAt(util.vector().of(0.5, 1.5, 0.5))
                .attachKeyFrame();
        scene.idle(80);

        scene.world().hideIndependentSection(smallTank, Direction.UP);
        scene.idle(20);
    }

    /**
     * Bring in the dyed pipes step-by-step and point out same-color, different-color, and undyed connectivity.
     */
    private static void stagePipeConnectivity(final CreateSceneBuilder scene, final SceneBuildingUtil util) {
        scene.addKeyframe();

        showPipeSequence(
                scene, util,
                new BlockPos(5, 1, 1),
                new BlockPos(5, 2, 1),
                new BlockPos(4, 2, 1),
                new BlockPos(3, 2, 1)
        );

        showPipeSequence(
                scene, util,
                new BlockPos(1, 1, 5),
                new BlockPos(1, 2, 5),
                new BlockPos(1, 2, 4),
                new BlockPos(1, 2, 3)
        );
        scene.idle(30);

        scene.overlay().showText(70)
                .text("Pipes and Tanks of the same color will connect as usual")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(1, 2, 4))
                .colored(PonderPalette.RED);
        scene.idle(80);

        scene.overlay().showText(70)
                .text("However, Pipes of different colors will not automatically connect")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(4, 2, 1))
                .colored(PonderPalette.BLUE);
        scene.idle(80);

        scene.overlay().showText(70)
                .text("Pipes without dye will automatically connect to all other tanks or pipes")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(1, 2, 3));
        scene.idle(80);
    }

    private static void dyeTankRegion(
            final CreateSceneBuilder scene,
            final int x1, final int y1, final int z1,
            final int x2, final int y2, final int z2,
            @Nullable final DyeColor color
    ) {
        scene.addInstruction(ponderScene -> {
            applyColorToRegion(ponderScene, x1, y1, z1, x2, y2, z2, color);
            ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
        });
    }

    private static void applyColorToRegion(
            final PonderScene ponderScene,
            final int x1, final int y1, final int z1,
            final int x2, final int y2, final int z2,
            @Nullable final DyeColor color
    ) {
        final Level level = ponderScene.getWorld();
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    final DyeableFluidTankBehaviour behaviour = BlockEntityBehaviour.get(
                            level, new BlockPos(x, y, z), DyeableFluidTankBehaviour.TYPE
                    );
                    if (behaviour != null) {
                        behaviour.applyColorClientOnly(color);
                    }
                }
            }
        }
    }

    private static void splitTankMultiBlock(final CreateSceneBuilder scene, final BlockPos... positions) {
        for (final BlockPos pos : positions) {
            scene.world().modifyBlock(
                    pos,
                    state -> state.setValue(FluidTankBlock.TOP, true)
                            .setValue(FluidTankBlock.BOTTOM, true)
                            .setValue(FluidTankBlock.SHAPE, FluidTankBlock.Shape.WINDOW),
                    false
            );
        }
        scene.addInstruction(ponderScene -> {
            for (final BlockPos pos : positions) {
                if (ponderScene.getWorld().getBlockEntity(pos) instanceof final FluidTankBlockEntity tankBE) {
                    final CompoundTag nbt = tankBE.saveWithFullMetadata(
                            ponderScene.getWorld().registryAccess()
                    );
                    nbt.remove("Controller");
                    nbt.remove("Uninitialized");
                    nbt.putInt("Size", 1);
                    nbt.putInt("Height", 1);
                    nbt.putBoolean("Window", true);
                    tankBE.loadWithComponents(nbt, ponderScene.getWorld().registryAccess());
                }
            }
            ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
        });
    }

    private static void showPipeSequence(
            final CreateSceneBuilder scene,
            final SceneBuildingUtil util,
            final BlockPos... positions
    ) {
        Direction direction = Direction.DOWN;
        for (int i = 0; i < positions.length; i++) {
            scene.world().restoreBlocks(util.select().position(positions[i]));
            assert direction != null;
            scene.world().showSection(util.select().position(positions[i]), direction);
            if (i < positions.length - 1) {
                final Vec3i delta = positions[i].subtract(positions[i + 1]);
                direction = Direction.fromDelta(delta.getX(), delta.getY(), delta.getZ());
            }
            scene.idle(4);
        }
    }

    private static void refreshPipes(final CreateSceneBuilder scene, final BlockPos... positions) {
        scene.addInstruction(ponderScene -> {
            for (final BlockPos pos : positions) {
                DyeablePipeBehaviour.refreshPipeState(
                        ponderScene.getWorld(), pos, ponderScene.getWorld().getBlockState(pos), false
                );
            }
            ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
        });
    }
}
