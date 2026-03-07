package com.kipti.bnb.foundation.ponder.scenes;

import com.kipti.bnb.content.trinkets.nixie.foundation.GenericNixieDisplayBlockEntity;
import com.kipti.bnb.content.trinkets.nixie.foundation.GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions;
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

public class LargeNixieTubeScenes {

    // Front row (facing UP, orientation NORTH): walk order controller→end
    private static final BlockPos FRONT_LEFT = new BlockPos(3, 1, 1);
    private static final BlockPos FRONT_MIDDLE = new BlockPos(2, 1, 1);
    private static final BlockPos FRONT_RIGHT = new BlockPos(1, 1, 1);

    // Vertical column (facing WEST, orientation NORTH): walk order controller→end
    private static final BlockPos VERT_TOP = new BlockPos(3, 4, 3);
    private static final BlockPos VERT_MIDDLE = new BlockPos(3, 3, 3);
    private static final BlockPos VERT_BOTTOM = new BlockPos(3, 2, 3);

    public static void largeNixieTube(final SceneBuilder builder, final SceneBuildingUtil util) {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("large_nixie_tube", "Using Large Nixie Tubes");
        scene.configureBasePlate(0, 0, 6);

        // Reset all nixie block entities to a clean state
        resetAllNixies(scene, FRONT_LEFT, FRONT_MIDDLE, FRONT_RIGHT, VERT_TOP, VERT_MIDDLE, VERT_BOTTOM);

        // Show base plate
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);

        // ── Stage 1: Show middle front tube ─────────────────────────────────────
        scene.addKeyframe();
        scene.world().showSection(util.select().position(FRONT_MIDDLE), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(60)
                .text("This is a Large Nixie Tube. It can be used to display text")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(FRONT_MIDDLE));
        scene.idle(70);

        // ── Stage 2: Extend front row ───────────────────────────────────────────
        scene.addKeyframe();
        scene.world().showSection(util.select().position(FRONT_RIGHT), Direction.EAST);
        scene.world().showSection(util.select().position(FRONT_LEFT), Direction.WEST);
        scene.idle(15);

        final Selection frontRowSelection = util.select().fromTo(1, 1, 1, 3, 1, 1);
        scene.overlay().showOutline(PonderPalette.OUTPUT, "front_row", frontRowSelection, 60);
        scene.overlay().showText(60)
                .text("Large Nixie Tubes can be combined with other Large Nixie Tubes")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(FRONT_MIDDLE));
        scene.idle(70);

        // ── Stage 3: Set text on front row ──────────────────────────────────────
        scene.addKeyframe();
        scene.overlay().showControls(util.vector().centerOf(FRONT_MIDDLE), Pointing.DOWN, 50)
                .withItem(AllBlocks.CLIPBOARD.asStack());
        scene.overlay().showOutline(PonderPalette.OUTPUT, "front_row_text", frontRowSelection, 50);
        scene.idle(10);

        setNixieRowText(scene, "txt", 0, new int[]{0, 1, 2}, FRONT_LEFT, FRONT_MIDDLE, FRONT_RIGHT);
        scene.idle(40);

        // ── Stage 4: Show supporting beam and vertical tubes ────────────────────
        scene.addKeyframe();
        scene.world().showSection(util.select().fromTo(4, 1, 3, 4, 4, 3), Direction.SOUTH);
        scene.idle(10);

        scene.world().showSection(util.select().position(VERT_BOTTOM), Direction.NORTH);
        scene.idle(3);
        scene.world().showSection(util.select().position(VERT_MIDDLE), Direction.NORTH);
        scene.idle(3);
        scene.world().showSection(util.select().position(VERT_TOP), Direction.NORTH);
        scene.idle(15);

        // ── Stage 5: Set text on vertical column ────────────────────────────────
        final Selection vertColumnSelection = util.select().fromTo(3, 2, 3, 3, 4, 3);
        scene.overlay().showControls(util.vector().centerOf(VERT_MIDDLE), Pointing.DOWN, 50)
                .withItem(AllBlocks.CLIPBOARD.asStack());
        scene.overlay().showOutline(PonderPalette.OUTPUT, "vert_col_text", vertColumnSelection, 50);
        scene.idle(10);

        setNixieRowText(scene, "txt", 0, new int[]{0, 1, 2}, VERT_TOP, VERT_MIDDLE, VERT_BOTTOM);
        scene.idle(40);

        // ── Stage 6: Wrench to ALWAYS_UP mode ───────────────────────────────────
        scene.addKeyframe();
        scene.overlay().showText(60)
                .text("A Wrench can be used to change the display mode of the Nixie Tube")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(VERT_MIDDLE));
        scene.idle(10);

        scene.overlay().showControls(util.vector().centerOf(VERT_MIDDLE), Pointing.DOWN, 50)
                .withItem(AllItems.WRENCH.asStack());
        scene.overlay().showOutline(PonderPalette.OUTPUT, "vert_col_wrench", vertColumnSelection, 50);
        scene.idle(10);

        setDisplayOptionForRow(scene, ConfigurableDisplayOptions.ALWAYS_UP, new int[]{0, 1, 2}, VERT_TOP, VERT_MIDDLE, VERT_BOTTOM);
        setNixieRowText(scene, "txt", 0, new int[]{0, 1, 2}, VERT_TOP, VERT_MIDDLE, VERT_BOTTOM);
        scene.idle(40);

        // ── Stage 7: Dye ────────────────────────────────────────────────────────
        scene.addKeyframe();
        scene.overlay().showText(60)
                .text("Dye can be used to change the color of all connected tubes at once")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(FRONT_MIDDLE));
        scene.idle(10);

        // Purple dye on front row
        scene.overlay().showControls(util.vector().centerOf(FRONT_MIDDLE), Pointing.DOWN, 35)
                .withItem(Items.PURPLE_DYE.getDefaultInstance());
        scene.overlay().showOutline(PonderPalette.OUTPUT, "front_row_dye", frontRowSelection, 35);
        scene.idle(10);
        applyDyeToNixieTubes(scene, DyeColor.PURPLE, FRONT_LEFT, FRONT_MIDDLE, FRONT_RIGHT);
        scene.idle(25);

        // Blue dye on vertical column
        scene.overlay().showControls(util.vector().centerOf(VERT_MIDDLE), Pointing.DOWN, 50)
                .withItem(Items.BLUE_DYE.getDefaultInstance());
        scene.overlay().showOutline(PonderPalette.OUTPUT, "vert_col_dye", vertColumnSelection, 50);
        scene.idle(10);
        applyDyeToNixieTubes(scene, DyeColor.BLUE, VERT_TOP, VERT_MIDDLE, VERT_BOTTOM);
        scene.idle(40);

        scene.markAsFinished();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

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
                }
            }
            ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
        });
    }

    private static void setNixieRowText(final CreateSceneBuilder scene, final String text, final int line,
                                        final int[] offsets, final BlockPos... positions) {
        scene.addInstruction(ponderScene -> {
            for (int i = 0; i < positions.length; i++) {
                final BlockEntity be = ponderScene.getWorld().getBlockEntity(positions[i]);
                if (be instanceof final GenericNixieDisplayBlockEntity nixie) {
                    final String json = Component.Serializer.toJson(
                            Component.literal(text), ponderScene.getWorld().registryAccess());
                    nixie.displayCustomText(json, offsets[i], line);
                }
            }
            ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
        });
    }

    private static void setDisplayOptionForRow(final CreateSceneBuilder scene,
                                               final ConfigurableDisplayOptions option,
                                               final int[] offsets, final BlockPos... positions) {
        scene.addInstruction(ponderScene -> {
            for (int i = 0; i < positions.length; i++) {
                final BlockEntity be = ponderScene.getWorld().getBlockEntity(positions[i]);
                if (be instanceof final GenericNixieDisplayBlockEntity nixie) {
                    nixie.setPositionOffset(offsets[i]);
                    nixie.setDisplayOption(option);
                }
            }
            ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
        });
    }

    private static void applyDyeToNixieTubes(final CreateSceneBuilder scene, final DyeColor color,
                                             final BlockPos... positions) {
        scene.addInstruction(ponderScene -> {
            for (final BlockPos pos : positions) {
                final BlockEntity oldBe = ponderScene.getWorld().getBlockEntity(pos);
                final CompoundTag savedData = oldBe.saveWithFullMetadata(ponderScene.getWorld().registryAccess());

                final BlockState state = ponderScene.getWorld().getBlockState(pos);
                final Block dyedBlock = BnbTrinketBlocks.DYED_LARGE_NIXIE_TUBE.get(color).get();
                ponderScene.getWorld().setBlock(pos,
                        BlockHelper.copyProperties(state, dyedBlock.defaultBlockState()), Block.UPDATE_ALL);

                final BlockEntity newBe = ponderScene.getWorld().getBlockEntity(pos);
                newBe.loadWithComponents(savedData, ponderScene.getWorld().registryAccess());
            }
            ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
        });
    }

    private static void queueRedraw(final CreateSceneBuilder scene) {
        scene.addInstruction(ponderScene ->
                ponderScene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw));
    }

}
