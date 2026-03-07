package com.kipti.bnb.foundation.ponder.scenes;

import com.kipti.bnb.content.trinkets.nixie.foundation.DoubleOrientedDirections;
import com.kipti.bnb.content.trinkets.nixie.foundation.GenericNixieDisplayBlock;
import com.kipti.bnb.content.trinkets.nixie.foundation.GenericNixieDisplayBlockEntity;
import com.kipti.bnb.content.trinkets.nixie.foundation.GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions;
import com.kipti.bnb.content.trinkets.nixie.nixie_board.NixieBoardBlockNixie;
import com.kipti.bnb.registry.content.blocks.BnbTrinketBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class NixieBoardScenes {

    public static void nixieBoard(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("nixie_board", "Using Nixie Boards");
        scene.configureBasePlate(0, 0, 5);

        // Positions in walk order (controller at x=3, then x=2, x=1)
        final BlockPos frontRight = new BlockPos(3, 1, 1);
        final BlockPos frontMiddle = new BlockPos(2, 1, 1);
        final BlockPos frontLeft = new BlockPos(1, 1, 1);

        final BlockPos backBottomRight = new BlockPos(3, 1, 3);
        final BlockPos backBottomMiddle = new BlockPos(2, 1, 3);
        final BlockPos backBottomLeft = new BlockPos(1, 1, 3);

        final BlockPos backTopRight = new BlockPos(3, 2, 3);
        final BlockPos backTopMiddle = new BlockPos(2, 2, 3);
        final BlockPos backTopLeft = new BlockPos(1, 2, 3);

        // Selections
        final Selection frontRow = util.select().fromTo(1, 1, 1, 3, 1, 1);
        final Selection backBottomRow = util.select().fromTo(1, 1, 3, 3, 1, 3);
        final Selection backTopRow = util.select().fromTo(1, 2, 3, 3, 2, 3);

        // === Initial Setup ===
        resetAllNixies(scene,
                frontRight, frontMiddle, frontLeft,
                backBottomRight, backBottomMiddle, backBottomLeft,
                backTopRight, backTopMiddle, backTopLeft);

        // Fix front middle block to standalone shape
        scene.addInstruction(ponderScene -> {
            final BlockState state = ponderScene.getWorld().getBlockState(frontMiddle);
            final BlockState connectedState = getConnectedState(ponderScene, state, frontMiddle);
            ponderScene.getWorld().setBlock(frontMiddle, connectedState, Block.UPDATE_ALL);
        });

        // Fix back bottom row shapes (no TOP connection since top row hasn't appeared)
        scene.addInstruction(ponderScene -> {
            BlockState state = ponderScene.getWorld().getBlockState(backBottomRight);
            state = getConnectedState(ponderScene, state, backBottomRight);
            ponderScene.getWorld().setBlock(backBottomRight, state, Block.UPDATE_ALL);

            state = ponderScene.getWorld().getBlockState(backBottomMiddle);
            state = getConnectedState(ponderScene, state, backBottomMiddle);
            ponderScene.getWorld().setBlock(backBottomMiddle, state, Block.UPDATE_ALL);

            state = ponderScene.getWorld().getBlockState(backBottomLeft);
            state = getConnectedState(ponderScene, state, backBottomLeft);
            ponderScene.getWorld().setBlock(backBottomLeft, state, Block.UPDATE_ALL);

            ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
        });

        // Show base plate
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(20);

        // === Stage 1: Show middle front board ===
        scene.addKeyframe();
        scene.world().showSection(util.select().position(2, 1, 1), Direction.DOWN);
        scene.idle(30);

        scene.overlay().showText(80)
                .text("This is a Nixie Board. It can be used to display text")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 1, 1));
        scene.idle(90);

        // === Stage 2: Extend front row ===
        scene.addKeyframe();
        scene.world().showSection(util.select().position(1, 1, 1), Direction.EAST);
        scene.world().showSection(util.select().position(3, 1, 1), Direction.WEST);
        scene.idle(20);

        // Update connection shapes for all front row blocks
        scene.addInstruction(ponderScene -> {
            BlockState state = ponderScene.getWorld().getBlockState(frontRight);
            state = getConnectedState(ponderScene, state, frontRight);
            ponderScene.getWorld().setBlock(frontRight, state, Block.UPDATE_ALL);

            state = ponderScene.getWorld().getBlockState(frontMiddle);
            state = getConnectedState(ponderScene, state, frontMiddle);
            ponderScene.getWorld().setBlock(frontMiddle, state, Block.UPDATE_ALL);

            state = ponderScene.getWorld().getBlockState(frontLeft);
            state = getConnectedState(ponderScene, state, frontLeft);
            ponderScene.getWorld().setBlock(frontLeft, state, Block.UPDATE_ALL);

            ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
        });
        scene.idle(30);

        scene.overlay().showOutline(PonderPalette.OUTPUT, "front_row", frontRow, 80);
        scene.overlay().showText(80)
                .text("The Nixie Board extends when connected to other neighbors")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 1, 1));
        scene.idle(90);

        // === Stage 3: Show back sections ===
        scene.addKeyframe();
        scene.world().showSection(backBottomRow, Direction.DOWN);
        scene.idle(20);
        scene.world().showSection(backTopRow, Direction.DOWN);
        scene.idle(20);

        // Update back bottom row and top row shapes
        scene.addInstruction(ponderScene -> {
            // Back bottom: update to include TOP connections
            BlockState state = ponderScene.getWorld().getBlockState(backBottomRight);
            state = getConnectedState(ponderScene, state, backBottomRight);
            ponderScene.getWorld().setBlock(backBottomRight, state, Block.UPDATE_ALL);

            state = ponderScene.getWorld().getBlockState(backBottomMiddle);
            state = getConnectedState(ponderScene, state, backBottomMiddle);
            ponderScene.getWorld().setBlock(backBottomMiddle, state, Block.UPDATE_ALL);

            state = ponderScene.getWorld().getBlockState(backBottomLeft);
            state = getConnectedState(ponderScene, state, backBottomLeft);
            ponderScene.getWorld().setBlock(backBottomLeft, state, Block.UPDATE_ALL);

            // Back top: update to include BOTTOM connections
            state = ponderScene.getWorld().getBlockState(backTopRight);
            state = getConnectedState(ponderScene, state, backTopRight);
            ponderScene.getWorld().setBlock(backTopRight, state, Block.UPDATE_ALL);

            state = ponderScene.getWorld().getBlockState(backTopMiddle);
            state = getConnectedState(ponderScene, state, backTopMiddle);
            ponderScene.getWorld().setBlock(backTopMiddle, state, Block.UPDATE_ALL);

            state = ponderScene.getWorld().getBlockState(backTopLeft);
            state = getConnectedState(ponderScene, state, backTopLeft);
            ponderScene.getWorld().setBlock(backTopLeft, state, Block.UPDATE_ALL);

            ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
        });
        scene.idle(30);

        scene.overlay().showText(80)
                .text("Boards placed above or below will connect to form larger displays")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 1, 3));
        scene.idle(90);

        // === Stage 4: Set text with clipboard ===
        scene.addKeyframe();
        scene.overlay().showText(100)
                .text("Clipboards, Display Links, or Name Tags can be used to change the displayed text")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 1, 1));
        scene.idle(25);

        // Front row
        scene.overlay().showControls(util.vector().centerOf(2, 1, 1), Pointing.LEFT, 50)
                .withItem(AllBlocks.CLIPBOARD.asStack());
        scene.overlay().showOutline(PonderPalette.OUTPUT, "front_text", frontRow, 50);
        scene.idle(20);
        setNixieRowText(scene, "txt", 0, new int[]{0, 1, 2}, frontRight, frontMiddle, frontLeft);
        scene.idle(40);

        // Back bottom row
        scene.overlay().showControls(util.vector().centerOf(2, 1, 3), Pointing.LEFT, 50)
                .withItem(AllBlocks.CLIPBOARD.asStack());
        scene.overlay().showOutline(PonderPalette.OUTPUT, "back_bottom_text", backBottomRow, 50);
        scene.idle(20);
        setNixieRowText(scene, "txt", 0, new int[]{0, 1, 2}, backBottomRight, backBottomMiddle, backBottomLeft);
        scene.idle(40);

        // Back top row
        scene.overlay().showControls(util.vector().centerOf(2, 2, 3), Pointing.LEFT, 50)
                .withItem(AllBlocks.CLIPBOARD.asStack());
        scene.overlay().showOutline(PonderPalette.OUTPUT, "back_top_text", backTopRow, 50);
        scene.idle(20);
        setNixieRowText(scene, "txt", 0, new int[]{0, 1, 2}, backTopRight, backTopMiddle, backTopLeft);
        scene.idle(40);

        // === Stage 5: Wrench display types ===
        scene.addKeyframe();

        // DOUBLE_CHAR on back bottom row
        scene.overlay().showControls(util.vector().centerOf(2, 1, 3), Pointing.LEFT, 50)
                .withItem(AllItems.WRENCH.asStack());
        scene.overlay().showOutline(PonderPalette.OUTPUT, "back_bottom_wrench", backBottomRow, 50);
        scene.idle(20);
        setDisplayOptionForRow(scene, ConfigurableDisplayOptions.DOUBLE_CHAR,
                new int[]{0, 2, 4}, backBottomRight, backBottomMiddle, backBottomLeft);
        setNixieRowText(scene, "txt", 0, new int[]{0, 2, 4}, backBottomRight, backBottomMiddle, backBottomLeft);
        scene.idle(40);

        scene.overlay().showText(60)
                .text("A Wrench can be used to cycle the display mode")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 1, 3));
        scene.idle(60);

        // DOUBLE_CHAR on back top row
        scene.overlay().showControls(util.vector().centerOf(2, 2, 3), Pointing.LEFT, 50)
                .withItem(AllItems.WRENCH.asStack());
        scene.overlay().showOutline(PonderPalette.OUTPUT, "back_top_wrench1", backTopRow, 50);
        scene.idle(20);
        setDisplayOptionForRow(scene, ConfigurableDisplayOptions.DOUBLE_CHAR,
                new int[]{0, 2, 4}, backTopRight, backTopMiddle, backTopLeft);
        setNixieRowText(scene, "txt", 0, new int[]{0, 2, 4}, backTopRight, backTopMiddle, backTopLeft);
        scene.idle(30);

        // DOUBLE_CHAR_DOUBLE_LINES on back top row
        scene.overlay().showControls(util.vector().centerOf(2, 2, 3), Pointing.LEFT, 50)
                .withItem(AllItems.WRENCH.asStack());
        scene.overlay().showOutline(PonderPalette.OUTPUT, "back_top_wrench2", backTopRow, 50);
        scene.idle(20);
        setDisplayOptionForRow(scene, ConfigurableDisplayOptions.DOUBLE_CHAR_DOUBLE_LINES,
                new int[]{0, 2, 5}, backTopRight, backTopMiddle, backTopLeft);
        setNixieRowText(scene, "txt", 0, new int[]{0, 2, 5}, backTopRight, backTopMiddle, backTopLeft);
        scene.idle(30);

        scene.overlay().showText(70)
                .text("Different display modes support more characters and multiple lines")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 2, 3));
        scene.idle(70);

        // === Stage 6: Fill text with clipboard ===
        scene.addKeyframe();

        // Back bottom: fill "txttxt" in DOUBLE_CHAR mode
        scene.overlay().showControls(util.vector().centerOf(2, 1, 3), Pointing.LEFT, 50)
                .withItem(AllBlocks.CLIPBOARD.asStack());
        scene.overlay().showOutline(PonderPalette.OUTPUT, "back_bottom_fill", backBottomRow, 50);
        scene.idle(20);
        setNixieRowText(scene, "txttxt", 0, new int[]{0, 2, 4},
                backBottomRight, backBottomMiddle, backBottomLeft);
        scene.idle(40);

        // Back top: fill "txt.txt" in DOUBLE_CHAR_DOUBLE_LINES on both lines
        scene.overlay().showControls(util.vector().centerOf(2, 2, 3), Pointing.LEFT, 50)
                .withItem(AllBlocks.CLIPBOARD.asStack());
        scene.overlay().showOutline(PonderPalette.OUTPUT, "back_top_fill", backTopRow, 50);
        scene.idle(20);
        setNixieRowText(scene, "txt.txt", 0, new int[]{0, 2, 5},
                backTopRight, backTopMiddle, backTopLeft);
        setNixieRowText(scene, "txt.txt", 1, new int[]{0, 2, 5},
                backTopRight, backTopMiddle, backTopLeft);
        scene.idle(40);

        // === Stage 7: Dye ===
        scene.addKeyframe();
        scene.overlay().showText(150)
                .text("Dye can be used to change the color of all connected boards at once")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 1, 1));
        scene.idle(25);

        // Blue dye on front row
        scene.overlay().showControls(util.vector().centerOf(2, 1, 1), Pointing.LEFT, 50)
                .withItem(Items.BLUE_DYE.getDefaultInstance());
        scene.overlay().showOutline(PonderPalette.BLUE, "front_dye", frontRow, 50);
        scene.idle(20);
        applyDyeToNixieBoards(scene, DyeColor.BLUE, frontRight, frontMiddle, frontLeft);
        scene.idle(40);

        // Purple dye on back bottom row
        scene.overlay().showControls(util.vector().centerOf(2, 1, 3), Pointing.LEFT, 50)
                .withItem(Items.PURPLE_DYE.getDefaultInstance());
        scene.overlay().showOutline(PonderPalette.BLUE, "back_bottom_dye", backBottomRow, 50);
        scene.idle(20);
        applyDyeToNixieBoards(scene, DyeColor.PURPLE,
                backBottomRight, backBottomMiddle, backBottomLeft);
        scene.idle(40);

        // Pink dye on back top row
        scene.overlay().showControls(util.vector().centerOf(2, 2, 3), Pointing.LEFT, 50)
                .withItem(Items.PINK_DYE.getDefaultInstance());
        scene.overlay().showOutline(PonderPalette.BLUE, "back_top_dye", backTopRow, 50);
        scene.idle(20);
        applyDyeToNixieBoards(scene, DyeColor.PINK, backTopRight, backTopMiddle, backTopLeft);
        scene.idle(40);

        scene.markAsFinished();
    }

    // ── helpers ──────────────────────────────────────────────────────────────────

    private static void resetAllNixies(final CreateSceneBuilder scene, final BlockPos... positions) {
        scene.addInstruction(ponderScene -> {
            for (final BlockPos pos : positions) {
                final BlockEntity be = ponderScene.getWorld().getBlockEntity(pos);
                if (be instanceof final GenericNixieDisplayBlockEntity nixie) {
                    nixie.setDisplayOption(ConfigurableDisplayOptions.NONE);
                    nixie.setPositionOffset(0);
                    final String emptyJson = Component.Serializer.toJson(
                            Component.empty(), ponderScene.getWorld().registryAccess());
                    nixie.displayCustomText(emptyJson, 0, 0);
                    nixie.displayCustomText(emptyJson, 0, 1);
                }
            }
            ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
        });
    }

    /**
     * Calculates and updates block state connection properties based on neighboring blocks.
     * This mimics the logic in NixieBoardBlockNixie.getConnectedState.
     * Should be called from within a ponderScene lambda context.
     * 
     * @param ponderScene The scene context providing access to the world
     * @param state The block state to update
     * @param pos The block position to check connections from
     * @return Updated block state with correct LEFT, RIGHT, TOP, BOTTOM connection flags
     */
    private static BlockState getConnectedState(final PonderScene ponderScene, final BlockState state, final BlockPos pos) {
        final Direction left = DoubleOrientedDirections.getLeft(state);
        final Direction right = left.getOpposite();
        final Direction bottom = state.getValue(GenericNixieDisplayBlock.FACING).getOpposite();
        final Direction top = state.getValue(GenericNixieDisplayBlock.FACING);

        final boolean leftConnection = GenericNixieDisplayBlockEntity.areStatesComprableForConnection(
                state, ponderScene.getWorld().getBlockState(pos.relative(left)));
        final boolean rightConnection = GenericNixieDisplayBlockEntity.areStatesComprableForConnection(
                state, ponderScene.getWorld().getBlockState(pos.relative(right)));
        final boolean bottomConnection = GenericNixieDisplayBlockEntity.areStatesComprableForConnection(
                state, ponderScene.getWorld().getBlockState(pos.relative(bottom)));
        final boolean topConnection = GenericNixieDisplayBlockEntity.areStatesComprableForConnection(
                state, ponderScene.getWorld().getBlockState(pos.relative(top)));

        return state
                .setValue(NixieBoardBlockNixie.LEFT, leftConnection)
                .setValue(NixieBoardBlockNixie.RIGHT, rightConnection)
                .setValue(NixieBoardBlockNixie.TOP, topConnection)
                .setValue(NixieBoardBlockNixie.BOTTOM, bottomConnection);
    }

    private static void setNixieRowText(final CreateSceneBuilder scene, final String text,
                                        final int line, final int[] offsets,
                                        final BlockPos... positions) {
        scene.addInstruction(ponderScene -> {
            final String json = Component.Serializer.toJson(
                    Component.literal(text), ponderScene.getWorld().registryAccess());
            for (int i = 0; i < positions.length; i++) {
                final BlockEntity be = ponderScene.getWorld().getBlockEntity(positions[i]);
                if (be instanceof final GenericNixieDisplayBlockEntity nixie) {
                    nixie.setPositionOffset(offsets[i]);
                    nixie.displayCustomText(json, offsets[i], line);
                }
            }
            ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
        });
    }

    private static void setDisplayOptionForRow(final CreateSceneBuilder scene,
                                               final ConfigurableDisplayOptions option,
                                               final int[] offsets,
                                               final BlockPos... positions) {
        scene.addInstruction(ponderScene -> {
            for (int i = 0; i < positions.length; i++) {
                final BlockEntity be = ponderScene.getWorld().getBlockEntity(positions[i]);
                if (be instanceof final GenericNixieDisplayBlockEntity nixie) {
                    nixie.setDisplayOption(option);
                    nixie.setPositionOffset(offsets[i]);
                }
            }
            ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
        });
    }

    private static void applyDyeToNixieBoards(final CreateSceneBuilder scene, final DyeColor color,
                                              final BlockPos... positions) {
        scene.addInstruction(ponderScene -> {
            for (final BlockPos pos : positions) {
                final BlockEntity oldBe = ponderScene.getWorld().getBlockEntity(pos);
                final CompoundTag savedData = oldBe != null
                        ? oldBe.saveWithFullMetadata(ponderScene.getWorld().registryAccess()) : null;

                final BlockState state = ponderScene.getWorld().getBlockState(pos);
                final Block dyedBlock = BnbTrinketBlocks.DYED_NIXIE_BOARD.get(color).get();
                ponderScene.getWorld().setBlock(pos,
                        BlockHelper.copyProperties(state, dyedBlock.defaultBlockState()),
                        Block.UPDATE_ALL);

                if (savedData != null) {
                    final BlockEntity newBe = ponderScene.getWorld().getBlockEntity(pos);
                    if (newBe != null) {
                        newBe.loadWithComponents(savedData, ponderScene.getWorld().registryAccess());
                    }
                }
            }
            ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
        });
    }

    private static void queueRedraw(final CreateSceneBuilder scene) {
        scene.addInstruction(ponderScene ->
                ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw));
    }

}
