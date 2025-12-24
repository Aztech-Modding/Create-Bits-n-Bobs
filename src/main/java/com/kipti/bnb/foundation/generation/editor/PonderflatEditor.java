package com.kipti.bnb.foundation.generation.editor;

import com.kipti.bnb.foundation.generation.PonderflatGeneratorSettings;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

public class PonderflatEditor extends Screen {
    private static final Component BIOME_SELECT_INFO = Component.translatable("createWorld.customize.buffet.biome").withColor(-8355712);
    private static final int SPACING = 8;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final Screen parent;
    private final Consumer<PonderflatGeneratorSettings> settingsConsumer;

    public PonderflatEditor(final Screen parent, final WorldCreationContext context, Consumer<PonderflatGeneratorSettings> settingsConsumer) {
        super(Component.translatable("createWorld.customize.buffet.title"));
        this.parent = parent;
        this.settingsConsumer = settingsConsumer;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
        this.settingsConsumer.accept(new PonderflatGeneratorSettings(4, Blocks.MOSS_BLOCK, Blocks.GRASS_BLOCK, PonderflatGeneratorSettings.CellStyle.RINGS));
    }

    @Override
    protected void init() {
        final LinearLayout linearlayout = this.layout.addToHeader(LinearLayout.vertical().spacing(8));
        linearlayout.defaultCellSetting().alignHorizontallyCenter();
        linearlayout.addChild(new StringWidget(this.getTitle(), this.font));
        linearlayout.addChild(new StringWidget(BIOME_SELECT_INFO, this.font));
        final LinearLayout linearlayout1 = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        Button doneButton = linearlayout1.addChild(Button.builder(CommonComponents.GUI_DONE, p_329718_ -> {
            this.onClose();
        }).build());
        linearlayout1.addChild(Button.builder(CommonComponents.GUI_CANCEL, p_329719_ -> this.onClose()).build());
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

}
