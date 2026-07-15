package com.kipti.bnb.foundation.gui.screen;

import com.kipti.bnb.foundation.BnbConfigBridge;
import com.kipti.bnb.registry.core.BnbConfigs;
import com.kipti.bnb.registry.core.BnbFeatureFlag;
import com.kipti.bnb.registry.core.BnbFeatureGroup;
import net.createmod.catnip.config.ConfigBase;
import net.createmod.catnip.config.ui.ConfigScreenList;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.RenderElement;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.gui.widget.BoxWidget;
import net.createmod.catnip.theme.Color;
import net.createmod.ponder.enums.PonderGuiTextures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class BnbFeatureGroupEntry extends ConfigScreenList.LabeledEntry {

    private static final Couple<Color> COLOR_PARTIAL = Couple.create(
            new Color(0xff_c09020),
            new Color(0xff_d4a830)
    );

    private final BnbFeatureGroup group;
    private final BoxWidget button;
    private final RenderElement enabledRenderer;
    private final RenderElement disabledRenderer;
    private final RenderElement partialRenderer;

    public BnbFeatureGroupEntry(final BnbFeatureGroup group) {
        super(group.getDisplayName());
        this.group = group;

        this.path = "featureGroup." + group.name().toLowerCase();

        this.labelTooltip.add(Component.literal(group.getDescription()).withStyle(ChatFormatting.GRAY));

        this.enabledRenderer = PonderGuiTextures.ICON_CONFIRM.asStencil()
                .withElementRenderer((ms, width, height, alpha) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, AbstractSimiWidget.COLOR_SUCCESS))
                .at(10, 0);

        this.disabledRenderer = PonderGuiTextures.ICON_DISABLE.asStencil()
                .withElementRenderer((ms, width, height, alpha) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, AbstractSimiWidget.COLOR_FAIL))
                .at(10, 0);

        this.partialRenderer = PonderGuiTextures.ICON_CONFIRM.asStencil()
                .withElementRenderer((ms, width, height, alpha) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, COLOR_PARTIAL))
                .at(10, 0);

        this.button = new BoxWidget().showingElement(this.enabledRenderer)
                .withCallback(this::onToggle);

        this.listeners.add(this.button);
        this.refreshState();
    }

    private static String getFlagPath(final BnbFeatureFlag flag) {
        final ConfigBase.ConfigBool configBool = BnbConfigs.common().FEATURE_FLAGS.get(flag);
        if (configBool == null)
            return null;
        return "featureFlags." + flag.getCategory().serialName() + "." + configBool.getName();
    }

    private void onToggle() {
        final BnbFeatureGroup.GroupState state = this.computeState();
        final boolean enable = state != BnbFeatureGroup.GroupState.ALL_ENABLED;
        for (final BnbFeatureFlag child : this.group.getChildren()) {
            if (child.isReleaseLocked())
                continue;
            final String path = getFlagPath(child);
            if (path == null)
                continue;
            final boolean persisted = BnbConfigs.common().getFeatureFlagState(child);
            BnbConfigBridge.setPendingBoolean(path, enable, persisted);
        }
        this.refreshState();
    }

    public void refreshState() {
        final BnbFeatureGroup.GroupState state = this.computeState();
        switch (state) {
            case ALL_ENABLED -> this.button.showingElement(this.enabledRenderer);
            case ALL_DISABLED -> this.button.showingElement(this.disabledRenderer);
            case PARTIAL -> this.button.showingElement(this.partialRenderer);
        }
    }

    private BnbFeatureGroup.GroupState computeState() {
        boolean anyEnabled = false;
        boolean anyDisabled = false;
        for (final BnbFeatureFlag child : this.group.getChildren()) {
            if (child.isReleaseLocked())
                continue;
            final String path = getFlagPath(child);
            if (path == null)
                continue;
            final boolean persisted = BnbConfigs.common().getFeatureFlagState(child);
            if (BnbConfigBridge.getPendingBoolean(path, persisted)) {
                anyEnabled = true;
            } else {
                anyDisabled = true;
            }
        }
        if (anyEnabled && anyDisabled)
            return BnbFeatureGroup.GroupState.PARTIAL;
        if (anyEnabled)
            return BnbFeatureGroup.GroupState.ALL_ENABLED;
        return BnbFeatureGroup.GroupState.ALL_DISABLED;
    }

    @Override
    public void tick() {
        super.tick();
        this.button.tick();
        this.refreshState();
    }

    @Override
    public void render(final GuiGraphics graphics, final int index, final int y, final int x, final int width, final int height,
                       final int mouseX, final int mouseY, final boolean p_230432_9_, final float partialTicks) {
        super.render(graphics, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);
        this.button.setX(x + width - 108);
        this.button.setY(y + 10);
        this.button.setWidth(35);
        this.button.setHeight(height - 20);
        this.button.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected int getLabelWidth(final int totalWidth) {
        return (int) (totalWidth * labelWidthMult) + 30;
    }
}
