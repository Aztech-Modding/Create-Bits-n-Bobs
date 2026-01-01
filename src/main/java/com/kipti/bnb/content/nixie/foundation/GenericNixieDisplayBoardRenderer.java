package com.kipti.bnb.content.nixie.foundation;

import com.kipti.bnb.content.nixie.large_nixie_tube.LargeNixieTubeBlockNixie;
import com.kipti.bnb.content.nixie.nixie_board.NixieBoardBlockNixie;
import com.kipti.bnb.mixin_accessor.FontAccess;
import com.kipti.bnb.mixin_accessor.ReverseRenderableBakedGlyph;
import com.kipti.bnb.registry.BnbBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.utility.DyeHelper;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.data.Couple;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class GenericNixieDisplayBoardRenderer extends SmartBlockEntityRenderer<GenericNixieDisplayBlockEntity> {

    public GenericNixieDisplayBoardRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final GenericNixieDisplayBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        final Font fontRenderer = Minecraft.getInstance().font;
        final FontSet fontSet = ((FontAccess) fontRenderer).bits_n_bobs$getFontSet(Style.DEFAULT_FONT);
        if (fontSet == null) {
            return; // No font set available, nothing to render
        }

        //TODO remove these so many BnbBlocks.NIXIE_BOARD.is(be.getBlockState().getBlock()) || BnbBlocks.DYED_NIXIE_BOARD.contains(be.getBlockState().getBlock())
        final boolean isNixieBoard = BnbBlocks.NIXIE_BOARD.is(be.getBlockState().getBlock()) || BnbBlocks.DYED_NIXIE_BOARD.contains(be.getBlockState().getBlock());
        final float scale = isNixieBoard ? 1f / 16f : 1f / 20f;
        final float offset = isNixieBoard ? 0 : 1f / 8f;

        ms.pushPose();
        final Direction facing = be.getBlockState().getValue(NixieBoardBlockNixie.FACING);
        final Direction orientation = be.getBlockState().getValue(NixieBoardBlockNixie.ORIENTATION);

        float orientationOffset = 0;
        final boolean isLargeNixieTube = be.getBlockState().getBlock() instanceof LargeNixieTubeBlockNixie;
        if (be.currentDisplayOption != GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions.ALWAYS_UP && facing != Direction.UP) {
            TransformStack.of(ms)
                .center()
                .mulPose(DoubleOrientedBlockModel.getRotation(facing, orientation))
                .uncenter();
            orientationOffset = isLargeNixieTube ? -2 / 16f : 0;
        } else {
            TransformStack.of(ms)
                .center()
                .translate(Vec3.atLowerCornerOf(facing.getNormal()).scale(2 / 16f))
                .mulPose(DoubleOrientedBlockModel.getRotation(Direction.UP, orientation))
                .uncenter();
            orientationOffset = -4 / 16f;
        }

        ms.translate(0.5, 1f + orientationOffset, 0.5);
        ms.scale(1 / 16f, 1 / 16f, 1 / 16f);
        if (isLargeNixieTube) {
            ms.scale(0.85f, 0.85f, 0.85f); // Scale down for large nixie tubes
        }
        ms.scale(-1, -1, +1);

        final Couple<Integer> baseColor = DyeHelper.getDyeColors(be.getBlockState().getBlock() instanceof final DyeProviderBlock dyeProviderBlock ? dyeProviderBlock.getDyeColor() : DyeColor.ORANGE);

        final ConfigurableDisplayOptionTransform transform = be.getCurrentDisplayOption().renderTransform.get();

        final TextBlockSubAtlas subAtlas = transform.isMoreThanOneCharacter() ? TextBlockSubAtlas.SMALL_NIXIE_TEXT_SUB_ATLAS : TextBlockSubAtlas.NIXIE_TEXT_SUB_ATLAS;
        transform.render(ms, be, glyph -> {
            if (glyph == ' ') {
                return; // Skip rendering spaces
            }
            final int charCode = glyph;
            final Couple<Integer> color = subAtlas.isInColorExcludedCharacterSet(charCode) ? DyeHelper.getDyeColors(DyeColor.WHITE) : baseColor;
            if (subAtlas.isInCharacterSet(charCode)) {
                renderGlyphUsingSpecialFont(ms, buffer, overlay, charCode, ms.last().pose(), color);
            } else {
                renderUsingNormalFont(ms, buffer, fontSet, charCode, ms.last().pose(), color);
            }
        });
        ms.popPose();
    }

    private static void renderGlyphUsingSpecialFont(final PoseStack ms, final MultiBufferSource buffer, final int overlay, final int glyph, Matrix4f pose, final Couple<Integer> col) {
        RenderType type = RenderType.cutout();
        final VertexConsumer cutoutBuffer = buffer.getBuffer(type);

        ms.translate(-6, -3, 0);
        final TextBlockSubAtlas.Uv characterUv = TextBlockSubAtlas.NIXIE_TEXT_SUB_ATLAS.getUvForCharacter(glyph);
        final float u0 = characterUv.getU0();
        final float u1 = characterUv.getU1();
        final float v0 = characterUv.getV0();
        final float v1 = characterUv.getV1();

        final int primary = (col.get(true)) | -16777216;
        addVerticesForChar(overlay, cutoutBuffer, pose, u0, v1, v0, u1, primary);
        pose = pose.translate(0.5f, 0.5f, 0.1f);
        final int secondary = (col.get(false)) | -16777216;
        addVerticesForChar(overlay, cutoutBuffer, pose, u0, v1, v0, u1, secondary);
        if (buffer instanceof final MultiBufferSource.BufferSource bs) {
            bs.endBatch(type);
        }
    }

    private static void addVerticesForChar(final int overlay, final VertexConsumer cutoutBuffer, final Matrix4f pose, final float u0, final float v1, final float v0, final float u1, final int col) {
        cutoutBuffer
            .vertex(pose, 0, 12, 0)
            .color(col)
            .uv(u0, v1)
            .overlayCoords(overlay)
            .uv2(LightTexture.FULL_BRIGHT)
            .normal(1, 0, 0)
            .endVertex();
        cutoutBuffer
            .vertex(pose, 0, 0, 0)
            .color(col)
            .uv(u0, v0)
            .overlayCoords(overlay)
            .uv2(LightTexture.FULL_BRIGHT)
            .normal(1, 0, 0)
            .endVertex();
        cutoutBuffer
            .vertex(pose, 12, 0, 0)
            .color(col)
            .uv(u1, v0)
            .overlayCoords(overlay)
            .uv2(LightTexture.FULL_BRIGHT)
            .normal(1, 0, 0)
            .endVertex();
        cutoutBuffer
            .vertex(pose, 12, 12, 0)
            .color(col)
            .uv(u1, v1)
            .overlayCoords(overlay)
            .uv2(LightTexture.FULL_BRIGHT)
            .normal(1, 0, 0)
            .endVertex();

        cutoutBuffer
            .vertex(pose, 12, 12, 0)
            .color(col)
            .uv(u1, v1)
            .overlayCoords(overlay)
            .uv2(LightTexture.FULL_BRIGHT)
            .normal(1, 0, 0)
            .endVertex();
        cutoutBuffer
            .vertex(pose, 12, 0, 0)
            .color(col)
            .uv(u1, v0)
            .overlayCoords(overlay)
            .uv2(LightTexture.FULL_BRIGHT)
            .normal(1, 0, 0)
            .endVertex();
        cutoutBuffer
            .vertex(pose, 0, 0, 0)
            .color(col)
            .uv(u0, v0)
            .overlayCoords(overlay)
            .uv2(LightTexture.FULL_BRIGHT)
            .normal(1, 0, 0)
            .endVertex();
        cutoutBuffer
            .vertex(pose, 0, 12, 0)
            .color(col)
            .uv(u0, v1)
            .overlayCoords(overlay)
            .uv2(LightTexture.FULL_BRIGHT)
            .normal(1, 0, 0)
            .endVertex();
    }

    private static void renderUsingNormalFont(final PoseStack ms, final MultiBufferSource buffer, final FontSet fontSet, final int glyph, final Matrix4f pose, final Couple<Integer> colours) {
        final BakedGlyph bakedGlyph = fontSet.getGlyph(glyph);
        final RenderType renderType = bakedGlyph.renderType(Font.DisplayMode.NORMAL);
        final VertexConsumer vertexconsumer = buffer.getBuffer(renderType);
        final float width = fontSet.getGlyphInfo(glyph, true).getAdvance(false) - 1;

        final float r = (colours.get(true) >> 16 & 0xFF) / 255.0f;
        final float g = (colours.get(true) >> 8 & 0xFF) / 255.0f;
        final float b = (colours.get(true) & 0xFF) / 255.0f;
        final float rSecondary = (colours.get(false) >> 16 & 0xFF) / 255.0f;
        final float gSecondary = (colours.get(false) >> 8 & 0xFF) / 255.0f;
        final float bSecondary = (colours.get(false) & 0xFF) / 255.0f;

        renderGlyph(ms, 0, bakedGlyph, width, vertexconsumer, r, g, b);

        ms.pushPose();
        ms.translate(0, 0, 0.1f);
        renderGlyph(ms, 0.5f, bakedGlyph, width, vertexconsumer, rSecondary, gSecondary, bSecondary);
        ms.popPose();

    }

    private static void renderGlyph(final PoseStack ms, final float offset, final BakedGlyph bakedGlyph, final float width, final VertexConsumer vertexconsumer, final float r, final float g, final float b) {
        bakedGlyph.render(false, -width / 2 + offset, offset, ms.last().pose(), vertexconsumer, r, g, b, 1, LightTexture.FULL_BRIGHT);
        if (bakedGlyph instanceof ReverseRenderableBakedGlyph) { //Font render types don't support normal transforms, so we have to do this hacky thing, i still cant get over create not doing this, IT'S NOT FLIPPED IN BASE CREATE!?
            ((ReverseRenderableBakedGlyph) bakedGlyph).bits_n_bobs$renderReverse(false, -width / 2 + offset, offset, ms.last().pose(), vertexconsumer, r, g, b, 1, LightTexture.FULL_BRIGHT);
        }
    }
}
