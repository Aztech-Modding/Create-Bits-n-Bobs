package com.kipti.bnb.content.decoration.grating;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.jetbrains.annotations.NotNull;

public class GratingPanelBlockItem extends BlockItem {

    public GratingPanelBlockItem(final GratingPanelBlock block, final Properties properties) {
        super(block, properties);
    }

    @Override
    public @NotNull InteractionResult place(final BlockPlaceContext context) {
        final InteractionResult result = super.place(context);
        if (!result.consumesAction()) {
            return result;
        }
        return result;
    }

}
