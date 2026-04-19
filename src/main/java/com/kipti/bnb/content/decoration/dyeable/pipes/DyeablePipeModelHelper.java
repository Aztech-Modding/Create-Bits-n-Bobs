package com.kipti.bnb.content.decoration.dyeable.pipes;

import com.cake.azimuth.utility.client.model.QuadTransformer;
import com.kipti.bnb.registry.client.BnbSpriteShifts;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class DyeablePipeModelHelper {

    public static final ModelProperty<DyeColor> PIPE_DYE_COLOR = new ModelProperty<>();

    private DyeablePipeModelHelper() {
    }

    public static @Nullable DyeColor getDyeColor(final ModelData data) {
        if (!data.has(PIPE_DYE_COLOR)) {
            return null;
        }
        return data.get(PIPE_DYE_COLOR);
    }

    public static List<BakedQuad> shiftQuads(final List<BakedQuad> quads, final @Nullable DyeColor color) {
        if (color == null) {
            return quads;
        }
        return QuadTransformer.shiftSprites(quads, getShiftEntries(color));
    }

    private static SpriteShiftEntry[] getShiftEntries(final DyeColor color) {
        return new SpriteShiftEntry[] {
                BnbSpriteShifts.DYED_PIPES.get(color),
                BnbSpriteShifts.DYED_PIPES_CONNECTED.get(color),
                BnbSpriteShifts.DYED_GLASS_FLUID_PIPE.get(color)
        };
    }
}
