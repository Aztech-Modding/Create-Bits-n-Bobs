package com.kipti.bnb.content.decoration.dyeable.simple;

import com.cake.azimuth.utility.client.model.QuadTransformer;
import com.kipti.bnb.content.decoration.dyeable.DyeableTransitionHelper;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SimpleDyeableModelHelper {

    public static final ModelProperty<DyeColor> SIMPLE_DYE_COLOR = new ModelProperty<>();

    public static @Nullable DyeColor getDyeColor(@Nullable final ModelData data) {
        if (data == null || !data.has(SIMPLE_DYE_COLOR)) {
            return null;
        }
        return data.get(SIMPLE_DYE_COLOR);
    }

    public static @Nullable DyeColor getDyeColor(final BlockAndTintGetter world, final BlockPos pos) {
        final SimpleDyeableBehaviour behaviour = BlockEntityBehaviour.get(world, pos, SimpleDyeableBehaviour.TYPE);
        if (behaviour != null && behaviour.getColor() != null) {
            return behaviour.getColor();
        }
        return DyeableTransitionHelper.getPendingPlacementColor(world, pos);
    }

    public static void putDyeColor(
            final ModelData.Builder builder,
            final BlockAndTintGetter world,
            final BlockPos pos
    ) {
        final DyeColor color = getDyeColor(world, pos);
        if (color != null) {
            builder.with(SIMPLE_DYE_COLOR, color);
        }
    }

    public static ModelData withDyeColor(
            @Nullable final ModelData data,
            final BlockAndTintGetter world,
            final BlockPos pos
    ) {
        final DyeColor color = getDyeColor(world, pos);
        if (color == null) {
            return data == null ? ModelData.EMPTY : data;
        }
        final ModelData safeData = data == null ? ModelData.EMPTY : data;
        return safeData.derive()
                .with(SIMPLE_DYE_COLOR, color)
                .build();
    }

    public static List<BakedQuad> shiftSprites(
            final List<BakedQuad> quads,
            @Nullable final DyeColor color,
            final SpriteShiftEntry... spriteShifts
    ) {
        if (color == null) {
            return quads;
        }
        return shiftSprites(quads, spriteShifts);
    }

    public static List<BakedQuad> shiftSprites(
            final List<BakedQuad> quads,
            final SpriteShiftEntry... spriteShifts
    ) {
        if (spriteShifts == null || spriteShifts.length == 0) {
            return quads;
        }

        final List<SpriteShiftEntry> entries = new ArrayList<>();
        for (final SpriteShiftEntry spriteShift : spriteShifts) {
            if (spriteShift != null) {
                entries.add(spriteShift);
            }
        }
        if (entries.isEmpty()) {
            return quads;
        }
        return QuadTransformer.shiftSprites(quads, entries);
    }

}
