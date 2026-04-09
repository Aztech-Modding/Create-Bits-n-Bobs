package com.kipti.bnb.foundation.ponder.scenes;

import com.kipti.bnb.content.trinkets.nixie.foundation.DoubleOrientedDirections;
import com.kipti.bnb.content.trinkets.nixie.foundation.GenericNixieDisplayBlock;
import com.kipti.bnb.content.trinkets.nixie.foundation.GenericNixieDisplayBlockEntity;
import com.kipti.bnb.content.trinkets.nixie.foundation.GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions;
import com.kipti.bnb.content.trinkets.nixie.nixie_board.NixieBoardBlockNixie;
import com.kipti.bnb.registry.content.blocks.BnbTrinketBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.foundation.PonderScene;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

//ignore-complexity: Ponders are animations not logic
public class NixieDisplayScenes {

    private static final ItemStack CLIPBOARD_WITH_CONTENT = makeFilledClipboard();

    private static @NotNull ItemStack makeFilledClipboard() {
        final ItemStack stack = AllBlocks.CLIPBOARD.asStack();
        stack.set(AllDataComponents.CLIPBOARD_TYPE, ClipboardOverrides.ClipboardType.WRITTEN);
        return stack;
    }

    private static final BlockPos TUBE_FRONT_LEFT = new BlockPos(3, 1, 1);
    private static final BlockPos TUBE_FRONT_MIDDLE = new BlockPos(2, 1, 1);
    private static final BlockPos TUBE_FRONT_RIGHT = new BlockPos(1, 1, 1);

    private static final BlockPos TUBE_VERT_TOP = new BlockPos(3, 4, 3);
    private static final BlockPos TUBE_VERT_MIDDLE = new BlockPos(3, 3, 3);
    private static final BlockPos TUBE_VERT_BOTTOM = new BlockPos(3, 2, 3);

    public static void largeNixieTube(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("large_nixie_tube", "Using Large Nixie Tubes");
        scene.configureBasePlate(0, 0, 6);

        resetAllNixies(
                scene, TUBE_FRONT_LEFT, TUBE_FRONT_MIDDLE, TUBE_FRONT_RIGHT,
                TUBE_VERT_TOP, TUBE_VERT_MIDDLE, TUBE_VERT_BOTTOM
        );

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(20);

        scene.addKeyframe();
        scene.world().showSection(util.select().position(TUBE_FRONT_MIDDLE), Direction.DOWN);
        scene.idle(30);
        scene.overlay().showText(60)
                .text("This is a Large Nixie Tube, it can be used to display custom text and symbols")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(TUBE_FRONT_MIDDLE));
        scene.idle(70);

        scene.world().showSection(util.select().position(TUBE_FRONT_RIGHT), Direction.EAST);
        scene.world().showSection(util.select().position(TUBE_FRONT_LEFT), Direction.WEST);
        scene.idle(30);

        scene.addKeyframe();
        final Selection frontRowSel = util.select().fromTo(1, 1, 1, 3, 1, 1);
        scene.overlay().showOutline(PonderPalette.OUTPUT, "front_row", frontRowSel, 120);
        scene.idle(5);
        scene.overlay().showText(60)
                .text("Large Nixie Tubes can be combined when in compatible rows")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(TUBE_FRONT_MIDDLE));
        scene.idle(70);

        scene.overlay().showControls(util.vector().blockSurface(TUBE_FRONT_MIDDLE, Direction.NORTH), Pointing.LEFT, 30)
                .withItem(CLIPBOARD_WITH_CONTENT);
        scene.idle(5);
        setNixieRowText(scene, "txt", 0, new int[]{0, 1, 2}, TUBE_FRONT_LEFT, TUBE_FRONT_MIDDLE, TUBE_FRONT_RIGHT);
        scene.idle(40);

        scene.world().showSection(util.select().fromTo(4, 1, 3, 4, 4, 3), Direction.WEST);
        scene.idle(20);

        scene.world().showSection(util.select().position(TUBE_VERT_BOTTOM), Direction.EAST);
        scene.idle(5);
        scene.world().showSection(util.select().position(TUBE_VERT_MIDDLE), Direction.EAST);
        scene.idle(5);
        scene.world().showSection(util.select().position(TUBE_VERT_TOP), Direction.EAST);
        scene.idle(30);

        final Selection vertColSel = util.select().fromTo(3, 2, 3, 3, 4, 3);
        scene.overlay().showOutline(PonderPalette.OUTPUT, "vert_col_text", vertColSel, 50);
        scene.idle(5);
        scene.overlay().showControls(util.vector().blockSurface(TUBE_VERT_MIDDLE, Direction.WEST), Pointing.LEFT, 30)
                .withItem(CLIPBOARD_WITH_CONTENT);
        scene.idle(5);
        setNixieRowText(scene, "txt", 0, new int[]{0, 1, 2}, TUBE_VERT_TOP, TUBE_VERT_MIDDLE, TUBE_VERT_BOTTOM);
        scene.idle(40);

        scene.addKeyframe();
        scene.overlay().showText(60)
                .text("A Wrench can be used to change the display mode of the Nixie Tube")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(TUBE_VERT_MIDDLE));
        scene.idle(70);

        scene.overlay().showOutline(PonderPalette.OUTPUT, "vert_col_wrench", vertColSel, 50);
        scene.idle(5);
        scene.overlay().showControls(util.vector().blockSurface(TUBE_VERT_MIDDLE, Direction.WEST), Pointing.LEFT, 30)
                .withItem(AllItems.WRENCH.asStack());
        scene.idle(5);
        setDisplayOptionForRow(
                scene, ConfigurableDisplayOptions.ALWAYS_UP, new int[]{0, 1, 2},
                TUBE_VERT_TOP, TUBE_VERT_MIDDLE, TUBE_VERT_BOTTOM
        );
        setNixieRowText(scene, "txt", 0, new int[]{0, 1, 2}, TUBE_VERT_TOP, TUBE_VERT_MIDDLE, TUBE_VERT_BOTTOM);
        scene.idle(40);

        scene.addKeyframe();
        scene.overlay().showText(60)
                .text("Dye can be used to change the color of all connected tubes at once")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(TUBE_VERT_MIDDLE));
        scene.idle(70);

        scene.overlay().showOutline(PonderPalette.OUTPUT, "front_row_dye", frontRowSel, 50);
        scene.idle(5);
        scene.overlay().showControls(util.vector().blockSurface(TUBE_FRONT_MIDDLE, Direction.NORTH), Pointing.LEFT, 30)
                .withItem(Items.PURPLE_DYE.getDefaultInstance());
        scene.idle(5);
        applyDye(
                scene, DyeColor.PURPLE, c -> BnbTrinketBlocks.DYED_LARGE_NIXIE_TUBE.get(c).get(),
                TUBE_FRONT_LEFT, TUBE_FRONT_MIDDLE, TUBE_FRONT_RIGHT
        );
        scene.idle(40);

        scene.overlay().showOutline(PonderPalette.OUTPUT, "vert_col_dye", vertColSel, 50);
        scene.idle(5);
        scene.overlay().showControls(util.vector().blockSurface(TUBE_VERT_MIDDLE, Direction.WEST), Pointing.LEFT, 30)
                .withItem(Items.BLUE_DYE.getDefaultInstance());
        scene.idle(5);
        applyDye(
                scene, DyeColor.BLUE, c -> BnbTrinketBlocks.DYED_LARGE_NIXIE_TUBE.get(c).get(),
                TUBE_VERT_TOP, TUBE_VERT_MIDDLE, TUBE_VERT_BOTTOM
        );
        scene.idle(40);

        scene.markAsFinished();
    }

    private static final BlockPos BOARD_FRONT_RIGHT = new BlockPos(3, 1, 1);
    private static final BlockPos BOARD_FRONT_MIDDLE = new BlockPos(2, 1, 1);
    private static final BlockPos BOARD_FRONT_LEFT = new BlockPos(1, 1, 1);

    private static final BlockPos BOARD_BACK_BOT_RIGHT = new BlockPos(3, 1, 3);
    private static final BlockPos BOARD_BACK_BOT_MIDDLE = new BlockPos(2, 1, 3);
    private static final BlockPos BOARD_BACK_BOT_LEFT = new BlockPos(1, 1, 3);

    private static final BlockPos BOARD_BACK_TOP_RIGHT = new BlockPos(3, 2, 3);
    private static final BlockPos BOARD_BACK_TOP_MIDDLE = new BlockPos(2, 2, 3);
    private static final BlockPos BOARD_BACK_TOP_LEFT = new BlockPos(1, 2, 3);

    public static void nixieBoard(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("nixie_board", "Using Nixie Boards");
        scene.configureBasePlate(0, 0, 5);

        final Selection frontRow = util.select().fromTo(1, 1, 1, 3, 1, 1);
        final Selection backBottomRow = util.select().fromTo(1, 1, 3, 3, 1, 3);
        final Selection backTopRow = util.select().fromTo(1, 2, 3, 3, 2, 3);
        final Selection backBothRows = util.select().fromTo(1, 1, 3, 3, 2, 3);

        resetAllNixies(
                scene,
                BOARD_FRONT_RIGHT, BOARD_FRONT_MIDDLE, BOARD_FRONT_LEFT,
                BOARD_BACK_BOT_RIGHT, BOARD_BACK_BOT_MIDDLE, BOARD_BACK_BOT_LEFT,
                BOARD_BACK_TOP_RIGHT, BOARD_BACK_TOP_MIDDLE, BOARD_BACK_TOP_LEFT
        );

        // Pre-set initial block states: frontMiddle is standalone (no connections);
        // back rows get horizontal connections only (vertical connections added after both rows appear).
        scene.addInstruction(ponderScene -> {
            // frontMiddle: no neighbours visible yet
            setIsolatedState(ponderScene, BOARD_FRONT_MIDDLE);

            // Back bottom: left/right connected, top/bottom not yet
            for (final BlockPos pos : new BlockPos[]{BOARD_BACK_BOT_RIGHT, BOARD_BACK_BOT_MIDDLE, BOARD_BACK_BOT_LEFT}) {
                final BlockState s = getConnectedState(ponderScene, ponderScene.getWorld().getBlockState(pos), pos)
                        .setValue(NixieBoardBlockNixie.TOP, false)
                        .setValue(NixieBoardBlockNixie.BOTTOM, false);
                ponderScene.getWorld().setBlock(pos, s, Block.UPDATE_ALL);
            }

            for (final BlockPos pos : new BlockPos[]{BOARD_BACK_TOP_RIGHT, BOARD_BACK_TOP_MIDDLE, BOARD_BACK_TOP_LEFT}) {
                final BlockState s = getConnectedState(ponderScene, ponderScene.getWorld().getBlockState(pos), pos)
                        .setValue(NixieBoardBlockNixie.TOP, false)
                        .setValue(NixieBoardBlockNixie.BOTTOM, false);
                ponderScene.getWorld().setBlock(pos, s, Block.UPDATE_ALL);
            }

            ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
        });

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(20);

        scene.addKeyframe();
        scene.world().showSection(util.select().position(BOARD_FRONT_MIDDLE), Direction.DOWN);
        scene.idle(30);
        scene.overlay().showText(80)
                .text("This is a Nixie Board. It can be used to display text")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(BOARD_FRONT_MIDDLE));
        scene.idle(90);

        scene.addKeyframe();
        scene.world().showSection(util.select().position(BOARD_FRONT_LEFT), Direction.EAST);
        scene.world().showSection(util.select().position(BOARD_FRONT_RIGHT), Direction.WEST);
        scene.idle(20);

        scene.addInstruction(ponderScene -> {
            for (final BlockPos pos : new BlockPos[]{BOARD_FRONT_RIGHT, BOARD_FRONT_MIDDLE, BOARD_FRONT_LEFT}) {
                ponderScene.getWorld().setBlock(
                        pos,
                        getConnectedState(ponderScene, ponderScene.getWorld().getBlockState(pos), pos),
                        Block.UPDATE_ALL
                );
            }
            ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
        });
        scene.idle(30);

        scene.overlay().showOutline(PonderPalette.OUTPUT, "front_row", frontRow, 80);
        scene.overlay().showText(80)
                .text("The Nixie Board extends when connected to other neighbours")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(BOARD_FRONT_MIDDLE));
        scene.idle(90);

        scene.addKeyframe();
        scene.world().showSection(backBottomRow, Direction.DOWN);
        scene.idle(20);
        scene.world().showSection(backTopRow, Direction.DOWN);
        scene.idle(20);

        scene.addInstruction(ponderScene -> {
            for (final BlockPos pos : new BlockPos[]{
                    BOARD_BACK_BOT_RIGHT, BOARD_BACK_BOT_MIDDLE, BOARD_BACK_BOT_LEFT,
                    BOARD_BACK_TOP_RIGHT, BOARD_BACK_TOP_MIDDLE, BOARD_BACK_TOP_LEFT}) {
                ponderScene.getWorld().setBlock(
                        pos,
                        getConnectedState(ponderScene, ponderScene.getWorld().getBlockState(pos), pos),
                        Block.UPDATE_ALL
                );
            }
            ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
        });
        scene.idle(30);

        scene.overlay().showText(80)
                .text("Boards placed above or below will connect to form larger displays")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(BOARD_BACK_BOT_MIDDLE));
        scene.idle(90);

        scene.addKeyframe();
        scene.overlay().showText(100)
                .text("Clipboards, Display Links, or Name Tags can be used to change the displayed text")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(BOARD_FRONT_MIDDLE));
        scene.idle(25);

        scene.overlay().showControls(util.vector().blockSurface(BOARD_FRONT_MIDDLE, Direction.NORTH), Pointing.DOWN, 50)
                .withItem(CLIPBOARD_WITH_CONTENT);
        scene.overlay().showOutline(PonderPalette.OUTPUT, "front_text", frontRow, 50);
        scene.idle(20);
        setNixieRowText(scene, "txt", 0, new int[]{0, 1, 2}, BOARD_FRONT_RIGHT, BOARD_FRONT_MIDDLE, BOARD_FRONT_LEFT);
        scene.idle(40);

        scene.overlay().showControls(
                        util.vector().blockSurface(BOARD_BACK_BOT_MIDDLE, Direction.NORTH),
                        Pointing.DOWN,
                        50
                )
                .withItem(CLIPBOARD_WITH_CONTENT);
        scene.overlay().showOutline(PonderPalette.OUTPUT, "back_bot_text", backBottomRow, 50);
        scene.idle(20);
        setNixieRowText(
                scene, "txt", 0, new int[]{0, 1, 2},
                BOARD_BACK_BOT_RIGHT, BOARD_BACK_BOT_MIDDLE, BOARD_BACK_BOT_LEFT
        );
        scene.idle(40);

        scene.overlay().showControls(
                        util.vector().blockSurface(BOARD_BACK_TOP_MIDDLE, Direction.NORTH),
                        Pointing.DOWN,
                        50
                )
                .withItem(CLIPBOARD_WITH_CONTENT);
        scene.overlay().showOutline(PonderPalette.OUTPUT, "back_top_text", backTopRow, 50);
        scene.idle(20);
        setNixieRowText(
                scene, "txt", 0, new int[]{0, 1, 2},
                BOARD_BACK_TOP_RIGHT, BOARD_BACK_TOP_MIDDLE, BOARD_BACK_TOP_LEFT
        );
        scene.idle(40);

        scene.addKeyframe();

        scene.overlay().showControls(
                        util.vector().blockSurface(BOARD_BACK_BOT_MIDDLE, Direction.NORTH),
                        Pointing.DOWN,
                        50
                )
                .withItem(AllItems.WRENCH.asStack());
        scene.overlay().showOutline(PonderPalette.OUTPUT, "back_bot_wrench", backBottomRow, 50);
        scene.idle(20);
        setDisplayOptionForRow(
                scene, ConfigurableDisplayOptions.DOUBLE_CHAR,
                new int[]{0, 2, 4}, BOARD_BACK_BOT_RIGHT, BOARD_BACK_BOT_MIDDLE, BOARD_BACK_BOT_LEFT
        );
        setNixieRowText(
                scene, "txt", 0, new int[]{0, 2, 4},
                BOARD_BACK_BOT_RIGHT, BOARD_BACK_BOT_MIDDLE, BOARD_BACK_BOT_LEFT
        );
        scene.idle(40);

        scene.overlay().showText(60)
                .text("A Wrench can be used to cycle the display mode")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(BOARD_BACK_BOT_MIDDLE));
        scene.idle(70);

        scene.overlay().showControls(
                        util.vector().blockSurface(BOARD_BACK_TOP_MIDDLE, Direction.NORTH),
                        Pointing.DOWN,
                        50
                )
                .withItem(AllItems.WRENCH.asStack());
        scene.overlay().showOutline(PonderPalette.OUTPUT, "back_top_wrench1", backTopRow, 50);
        scene.idle(20);
        setDisplayOptionForRow(
                scene, ConfigurableDisplayOptions.DOUBLE_CHAR,
                new int[]{0, 2, 4}, BOARD_BACK_TOP_RIGHT, BOARD_BACK_TOP_MIDDLE, BOARD_BACK_TOP_LEFT
        );
        setNixieRowText(
                scene, "txt", 0, new int[]{0, 2, 4},
                BOARD_BACK_TOP_RIGHT, BOARD_BACK_TOP_MIDDLE, BOARD_BACK_TOP_LEFT
        );
        scene.idle(30);

        scene.overlay().showControls(
                        util.vector().blockSurface(BOARD_BACK_TOP_MIDDLE, Direction.NORTH),
                        Pointing.DOWN,
                        50
                )
                .withItem(AllItems.WRENCH.asStack());
        scene.overlay().showOutline(PonderPalette.OUTPUT, "back_top_wrench2", backTopRow, 50);
        scene.idle(20);
        setDisplayOptionForRow(
                scene, ConfigurableDisplayOptions.DOUBLE_CHAR_DOUBLE_LINES,
                new int[]{0, 2, 5}, BOARD_BACK_TOP_RIGHT, BOARD_BACK_TOP_MIDDLE, BOARD_BACK_TOP_LEFT
        );
        setNixieRowText(
                scene, "txt", 0, new int[]{0, 2, 5},
                BOARD_BACK_TOP_RIGHT, BOARD_BACK_TOP_MIDDLE, BOARD_BACK_TOP_LEFT
        );
        scene.idle(30);

        scene.overlay().showText(70)
                .text("Different display modes support more characters and multiple lines")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(BOARD_BACK_TOP_MIDDLE));
        scene.idle(70);

        scene.addKeyframe();

        scene.overlay().showControls(
                        util.vector().blockSurface(BOARD_BACK_BOT_MIDDLE, Direction.NORTH),
                        Pointing.DOWN,
                        50
                )
                .withItem(CLIPBOARD_WITH_CONTENT);
        scene.overlay().showOutline(PonderPalette.OUTPUT, "back_bot_fill", backBottomRow, 50);
        scene.idle(20);
        setNixieRowText(
                scene, "txttxt", 0, new int[]{0, 2, 4},
                BOARD_BACK_BOT_RIGHT, BOARD_BACK_BOT_MIDDLE, BOARD_BACK_BOT_LEFT
        );
        scene.idle(40);

        scene.overlay().showControls(
                        util.vector().blockSurface(BOARD_BACK_TOP_MIDDLE, Direction.NORTH),
                        Pointing.DOWN,
                        50
                )
                .withItem(CLIPBOARD_WITH_CONTENT);
        scene.overlay().showOutline(PonderPalette.OUTPUT, "back_top_fill", backTopRow, 50);
        scene.idle(20);
        setNixieRowText(
                scene, "txt.txt", 0, new int[]{0, 2, 5},
                BOARD_BACK_TOP_RIGHT, BOARD_BACK_TOP_MIDDLE, BOARD_BACK_TOP_LEFT
        );
        setNixieRowText(
                scene, "txt.txt", 1, new int[]{0, 2, 5},
                BOARD_BACK_TOP_RIGHT, BOARD_BACK_TOP_MIDDLE, BOARD_BACK_TOP_LEFT
        );
        scene.idle(40);

        scene.addKeyframe();
        scene.overlay().showText(150)
                .text("Dye can be used to change the color of all connected boards at once")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(BOARD_FRONT_MIDDLE));
        scene.idle(25);

        scene.overlay().showControls(util.vector().blockSurface(BOARD_FRONT_MIDDLE, Direction.NORTH), Pointing.DOWN, 50)
                .withItem(Items.BLUE_DYE.getDefaultInstance());
        scene.overlay().showOutline(PonderPalette.OUTPUT, "front_dye", frontRow, 50);
        scene.idle(20);
        applyDye(
                scene, DyeColor.BLUE, c -> BnbTrinketBlocks.DYED_NIXIE_BOARD.get(c).get(),
                BOARD_FRONT_RIGHT, BOARD_FRONT_MIDDLE, BOARD_FRONT_LEFT
        );
        scene.idle(40);

        scene.overlay().showControls(
                        util.vector().blockSurface(BOARD_BACK_BOT_MIDDLE, Direction.NORTH),
                        Pointing.DOWN,
                        50
                )
                .withItem(Items.PURPLE_DYE.getDefaultInstance());
        scene.overlay().showOutline(PonderPalette.OUTPUT, "back_bot_dye", backBottomRow, 50);
        scene.idle(20);
        applyDye(
                scene, DyeColor.PURPLE, c -> BnbTrinketBlocks.DYED_NIXIE_BOARD.get(c).get(),
                BOARD_BACK_BOT_RIGHT, BOARD_BACK_BOT_MIDDLE, BOARD_BACK_BOT_LEFT
        );
        scene.idle(40);

        scene.overlay().showControls(
                        util.vector().blockSurface(BOARD_BACK_TOP_MIDDLE, Direction.NORTH),
                        Pointing.DOWN,
                        50
                )
                .withItem(Items.PINK_DYE.getDefaultInstance());
        scene.overlay().showOutline(PonderPalette.OUTPUT, "back_top_dye", backTopRow, 50);
        scene.idle(20);
        applyDye(
                scene, DyeColor.PINK, c -> BnbTrinketBlocks.DYED_NIXIE_BOARD.get(c).get(),
                BOARD_BACK_TOP_RIGHT, BOARD_BACK_TOP_MIDDLE, BOARD_BACK_TOP_LEFT
        );
        scene.idle(40);

        scene.markAsFinished();
    }

    private static void resetAllNixies(final CreateSceneBuilder scene, final BlockPos... positions) {
        scene.addInstruction(ponderScene -> {
            final String emptyJson = Component.Serializer.toJson(
                    Component.empty(), ponderScene.getWorld().registryAccess());
            for (final BlockPos pos : positions) {
                final BlockEntity be = ponderScene.getWorld().getBlockEntity(pos);
                if (be instanceof final GenericNixieDisplayBlockEntity nixie) {
                    nixie.setDisplayOption(ConfigurableDisplayOptions.NONE);
                    nixie.setPositionOffset(0);
                    nixie.displayCustomText(emptyJson, 0, 0);
                    nixie.displayCustomText(emptyJson, 0, 1);
                }
            }
            ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
        });
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

    /**
     * Applies a dye colour to a run of nixie blocks, preserving their block-entity data.
     */
    private static void applyDye(final CreateSceneBuilder scene, final DyeColor color,
                                 final Function<DyeColor, Block> dyedBlock,
                                 final BlockPos... positions) {
        scene.addInstruction(ponderScene -> {
            for (final BlockPos pos : positions) {
                final BlockEntity oldBe = ponderScene.getWorld().getBlockEntity(pos);
                final CompoundTag saved = oldBe.saveWithFullMetadata(ponderScene.getWorld().registryAccess());

                final BlockState state = ponderScene.getWorld().getBlockState(pos);
                ponderScene.getWorld().setBlock(
                        pos,
                        BlockHelper.copyProperties(state, dyedBlock.apply(color).defaultBlockState()),
                        Block.UPDATE_ALL
                );

                final BlockEntity newBe = ponderScene.getWorld().getBlockEntity(pos);
                newBe.loadWithComponents(saved, ponderScene.getWorld().registryAccess());
            }
            ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
        });
    }

    /**
     * Computes the correct LEFT/RIGHT/TOP/BOTTOM connection properties for a NixieBoard
     * by checking its neighbours, mirroring the logic in NixieBoardBlockNixie.getConnectedState.
     */
    private static BlockState getConnectedState(final PonderScene ponderScene,
                                                final BlockState state, final BlockPos pos) {
        final Direction left = DoubleOrientedDirections.getLeft(state);
        final Direction right = left.getOpposite();
        final Direction bottom = state.getValue(GenericNixieDisplayBlock.FACING).getOpposite();
        final Direction top = state.getValue(GenericNixieDisplayBlock.FACING);

        return state
                .setValue(
                        NixieBoardBlockNixie.LEFT,
                        GenericNixieDisplayBlockEntity.areStatesComprableForConnection(
                                state,
                                ponderScene.getWorld().getBlockState(pos.relative(left))
                        )
                )
                .setValue(
                        NixieBoardBlockNixie.RIGHT,
                        GenericNixieDisplayBlockEntity.areStatesComprableForConnection(
                                state,
                                ponderScene.getWorld().getBlockState(pos.relative(right))
                        )
                )
                .setValue(
                        NixieBoardBlockNixie.BOTTOM,
                        GenericNixieDisplayBlockEntity.areStatesComprableForConnection(
                                state,
                                ponderScene.getWorld().getBlockState(pos.relative(bottom))
                        )
                )
                .setValue(
                        NixieBoardBlockNixie.TOP,
                        GenericNixieDisplayBlockEntity.areStatesComprableForConnection(
                                state,
                                ponderScene.getWorld().getBlockState(pos.relative(top))
                        )
                );
    }

    /**
     * Forces all four connection flags to false (standalone appearance).
     */
    private static void setIsolatedState(final PonderScene ponderScene, final BlockPos pos) {
        final BlockState state = ponderScene.getWorld().getBlockState(pos);
        ponderScene.getWorld().setBlock(
                pos,
                state.setValue(NixieBoardBlockNixie.LEFT, false)
                        .setValue(NixieBoardBlockNixie.RIGHT, false)
                        .setValue(NixieBoardBlockNixie.TOP, false)
                        .setValue(NixieBoardBlockNixie.BOTTOM, false),
                Block.UPDATE_ALL
        );
    }
}
