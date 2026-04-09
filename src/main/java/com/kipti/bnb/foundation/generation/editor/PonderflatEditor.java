package com.kipti.bnb.foundation.generation.editor;

import com.kipti.bnb.foundation.generation.PonderflatGeneratorSettings;
import com.kipti.bnb.foundation.generation.PonderflatGeneratorSettings.CellStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;

import java.util.Arrays;
import java.util.function.Consumer;

public class PonderflatEditor extends Screen {
    private static final Component TITLE = Component.literal("Ponderflat Settings");
    private static final int SPACING = 8;
    private static final int WIDGET_WIDTH = 210;
    private static final int WIDGET_HEIGHT = 20;

    private HeaderAndFooterLayout layout;
    private final Screen parent;
    private final Consumer<PonderflatGeneratorSettings> settingsConsumer;

    private int cellSize = 1;
    private Block blockLight = Blocks.SNOW_BLOCK;
    private Block blockDark = Blocks.WHITE_CONCRETE;
    private CellStyle cellStyle = CellStyle.BORDERED;

    private EditBox lightBlockInput;
    private EditBox darkBlockInput;

    private record Preset(String name, int cellSize, Block blockLight, Block blockDark, CellStyle cellStyle) {
    }

    private static final Preset[] PRESETS = {
            new Preset("Small Ponder", 1, Blocks.SNOW_BLOCK, Blocks.WHITE_CONCRETE, CellStyle.BORDERED),
            new Preset("Big Ponder", 3, Blocks.SNOW_BLOCK, Blocks.WHITE_CONCRETE, CellStyle.BORDERED),
            new Preset("World Ponder", 5, Blocks.SNOW_BLOCK, Blocks.WHITE_CONCRETE, CellStyle.BORDERED),
            new Preset("Garden Ponder", 1, Blocks.MOSS_BLOCK, Blocks.GRASS_BLOCK, CellStyle.BORDERED),
    };

    public PonderflatEditor(final Screen parent, final WorldCreationContext context, final Consumer<PonderflatGeneratorSettings> settingsConsumer) {
        super(TITLE);
        this.parent = parent;
        this.settingsConsumer = settingsConsumer;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private void done() {
        this.settingsConsumer.accept(new PonderflatGeneratorSettings(cellSize, blockLight, blockDark, cellStyle));
        this.minecraft.setScreen(this.parent);
    }

    @Override
    protected void init() {
        this.layout = new HeaderAndFooterLayout(this);

        // Header
        final LinearLayout header = this.layout.addToHeader(LinearLayout.vertical().spacing(SPACING));
        header.defaultCellSetting().alignHorizontallyCenter();
        header.addChild(new StringWidget(this.getTitle(), this.font));

        // Body
        final LinearLayout body = this.layout.addToContents(LinearLayout.vertical().spacing(SPACING));
        body.defaultCellSetting().alignHorizontallyCenter();

        // Presets
        // Try figure out which preset (if any) matches the current settings, and select that one by default
        Preset selectedPreset = null;
        for (final Preset preset : PRESETS) {
            if (preset.cellSize == cellSize && preset.blockLight == blockLight && preset.blockDark == blockDark && preset.cellStyle == cellStyle) {
                selectedPreset = preset;
                break;
            }
        }
        final Preset nextPreset = selectedPreset == null ? PRESETS[0] : PRESETS[(Arrays.asList(PRESETS).indexOf(selectedPreset) + 1) % PRESETS.length];

        body.addChild(
                Button.builder(
                                Component.literal("Preset: " + (selectedPreset != null ? selectedPreset.name : "Custom")),
                                btn -> applyPreset(nextPreset)
                        )
                        .build()
        );

        // Cell Size slider (1-16)
        final ExtendedSlider cellSizeSlider = new ExtendedSlider(
                0, 0, WIDGET_WIDTH, WIDGET_HEIGHT,
                Component.literal("Cell Size: "), Component.empty(),
                1, 16, cellSize, 1, 0, true
        ) {
            @Override
            protected void applyValue() {
                cellSize = this.getValueInt();
            }
        };
        body.addChild(cellSizeSlider);

        // Cell Style cycle button
        final CycleButton<CellStyle> styleButton = CycleButton.<CellStyle>builder(
                        style -> Component.literal(style.getSerializedName().substring(0, 1).toUpperCase() + style.getSerializedName().substring(1)))
                .withValues(CellStyle.values())
                .withInitialValue(cellStyle)
                .create(0, 0, WIDGET_WIDTH, WIDGET_HEIGHT,
                        Component.literal("Cell Style"),
                        (btn, val) -> cellStyle = val);
        body.addChild(styleButton);

        // Light Block input
        body.addChild(new StringWidget(Component.literal("Light Block ID:").withColor(0xAAAAAA), this.font));
        lightBlockInput = new EditBox(this.font, 0, 0, WIDGET_WIDTH, WIDGET_HEIGHT, Component.literal("Light Block"));
        lightBlockInput.setMaxLength(128);
        lightBlockInput.setValue(BuiltInRegistries.BLOCK.getKey(blockLight).toString());
        lightBlockInput.setResponder(val -> {
            final Block resolved = resolveBlock(val);
            if (resolved != null) {
                blockLight = resolved;
                lightBlockInput.setTextColor(0xFFFFFF);
            } else {
                lightBlockInput.setTextColor(0xFF5555);
            }
        });
        body.addChild(lightBlockInput);

        // Dark Block input
        body.addChild(new StringWidget(Component.literal("Dark Block ID:").withColor(0xAAAAAA), this.font));
        darkBlockInput = new EditBox(this.font, 0, 0, WIDGET_WIDTH, WIDGET_HEIGHT, Component.literal("Dark Block"));
        darkBlockInput.setMaxLength(128);
        darkBlockInput.setValue(BuiltInRegistries.BLOCK.getKey(blockDark).toString());
        darkBlockInput.setResponder(val -> {
            final Block resolved = resolveBlock(val);
            if (resolved != null) {
                blockDark = resolved;
                darkBlockInput.setTextColor(0xFFFFFF);
            } else {
                darkBlockInput.setTextColor(0xFF5555);
            }
        });
        body.addChild(darkBlockInput);

        // Footer (Done / Cancel)
        final LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(SPACING));
        footer.addChild(Button.builder(CommonComponents.GUI_DONE, btn -> this.done()).width(100).build());
        footer.addChild(Button.builder(CommonComponents.GUI_CANCEL, btn -> this.onClose()).width(100).build());

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render item icons next to block input fields
        if (lightBlockInput != null) {
            final ItemStack lightStack = new ItemStack(blockLight);
            guiGraphics.renderItem(lightStack, lightBlockInput.getX() + WIDGET_WIDTH + 4, lightBlockInput.getY() + 2);
        }
        if (darkBlockInput != null) {
            final ItemStack darkStack = new ItemStack(blockDark);
            guiGraphics.renderItem(darkStack, darkBlockInput.getX() + WIDGET_WIDTH + 4, darkBlockInput.getY() + 2);
        }
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    private void applyPreset(final Preset preset) {
        this.cellSize = preset.cellSize();
        this.blockLight = preset.blockLight();
        this.blockDark = preset.blockDark();
        this.cellStyle = preset.cellStyle();
        this.rebuildWidgets();
    }

    private static Block resolveBlock(final String id) {
        try {
            final ResourceLocation loc = ResourceLocation.tryParse(id);
            if (loc != null && BuiltInRegistries.BLOCK.containsKey(loc)) {
                return BuiltInRegistries.BLOCK.get(loc);
            }
        } catch (final Exception ignored) {
        }
        return null;
    }
}

