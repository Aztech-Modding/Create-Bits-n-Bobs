package com.kipti.bnb.content.kinetics.chain_pulley;

import com.kipti.bnb.registry.content.BnbBlocksBootstrap;
import com.kipti.bnb.registry.client.BnbPartialModels;
import com.kipti.bnb.registry.client.BnbSpriteShifts;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.pulley.AbstractPulleyRenderer;
import com.simibubi.create.content.contraptions.pulley.PulleyBlock;
import com.simibubi.create.content.contraptions.pulley.PulleyContraption;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ChainPulleyRenderer extends AbstractPulleyRenderer<ChainPulleyBlockEntity> {

    //Private field duplication
    private final PartialModel halfRope = BnbPartialModels.CHAIN_ROPE_HALF;
    private final PartialModel halfMagnet = BnbPartialModels.CHAIN_ROPE_HALF_MAGNET;

    public ChainPulleyRenderer(final BlockEntityRendererProvider.Context context) {
        super(context, BnbPartialModels.CHAIN_ROPE_HALF, BnbPartialModels.CHAIN_ROPE_HALF_MAGNET);
    }

    /**
     * Overrides to force the non-visual rendering always (because {@link ChainPulleyVisual} reasons} is unused)
     */
    @Override
    protected void renderSafe(final ChainPulleyBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer,
                              final int light, final int overlay) {
        final BlockState state = getRenderedBlockState(be);
        final RenderType type = getRenderType(be, state);
        renderRotatingBuffer(be, getRotatedModel(be, state), ms, buffer.getBuffer(type), light);

        final float offset = getOffset(be, partialTicks);
        final boolean running = isRunning(be);

        final VertexConsumer vb = buffer.getBuffer(RenderType.cutout());
        scrollCoil(getRotatedCoil(be), getCoilShift(), offset, 1)
                .light(light)
                .renderInto(ms, vb);

        final Level world = be.getLevel();
        final BlockState blockState = be.getBlockState();
        final BlockPos pos = be.getBlockPos();

        final SuperByteBuffer halfMagnetChain = CachedBuffers.partial(BnbPartialModels.CHAIN_PULLEY_MAGNET_CHAIN_HALF, blockState);
        final SuperByteBuffer magnetChain = CachedBuffers.partial(BnbPartialModels.CHAIN_PULLEY_MAGNET_CHAIN, blockState);

        final SuperByteBuffer halfRope = CachedBuffers.partial(this.halfRope, blockState);

        final SuperByteBuffer magnetNoChains = renderMagnet(be);

        final SuperByteBuffer rope = renderRope(be);

        if (running || offset == 0) {
            renderAt(world, magnetNoChains, offset, pos, ms, vb);
            renderAt(world, scrollCoil(offset > .25f ? magnetChain : halfMagnetChain, BnbSpriteShifts.CHAIN_ROPE, offset, 1), offset, pos, ms, vb);
        }

        final float f = offset % 1;
        if (offset > .75f && (f < .25f || f > .75f))
            renderAt(world, scrollCoil(halfRope, BnbSpriteShifts.CHAIN_ROPE, offset, 1), f > .75f ? f - 1 : f, pos, ms, vb);

        if (!running)
            return;

        for (int i = 0; i < offset - 1.25f; i++)
            renderAt(world, scrollCoil(rope, BnbSpriteShifts.CHAIN_ROPE, offset, 1), offset - i - 1, pos, ms, vb);
    }

    @Override
    protected Direction.Axis getShaftAxis(final ChainPulleyBlockEntity be) {
        return be.getBlockState()
                .getValue(PulleyBlock.HORIZONTAL_AXIS);
    }

    @Override
    protected PartialModel getCoil() {
        return BnbPartialModels.CHAIN_ROPE_COIL;
    }

    @Override
    protected SuperByteBuffer renderRope(final ChainPulleyBlockEntity be) {
        return CachedBuffers.block(BnbBlocksBootstrap.CHAIN_ROPE.getDefaultState());
    }

    @Override
    protected SuperByteBuffer renderMagnet(final ChainPulleyBlockEntity be) {
        return CachedBuffers.partial(BnbPartialModels.CHAIN_PULLEY_MAGNET_NO_CHAIN, be.getBlockState());
    }

    @Override
    protected float getOffset(final ChainPulleyBlockEntity be, final float partialTicks) {
        return getBlockEntityOffset(partialTicks, be);
    }

    @Override
    protected boolean isRunning(final ChainPulleyBlockEntity be) {
        return isPulleyRunning(be);
    }

    public static boolean isPulleyRunning(final ChainPulleyBlockEntity be) {
        return be.running || be.getMirrorParent() != null || be.isVirtual();
    }

    @Override
    protected SpriteShiftEntry getCoilShift() {
        return BnbSpriteShifts.CHAIN_PULLEY_COIL;
    }

    public static float getBlockEntityOffset(final float partialTicks, final ChainPulleyBlockEntity blockEntity) {
        float offset = blockEntity.getInterpolatedOffset(partialTicks);

        final AbstractContraptionEntity attachedContraption = blockEntity.getAttachedContraption();
        if (attachedContraption != null) {
            final PulleyContraption c = (PulleyContraption) attachedContraption.getContraption();
            final double entityPos = Mth.lerp(partialTicks, attachedContraption.yOld, attachedContraption.getY());
            offset = (float) -(entityPos - c.anchor.getY() - c.getInitialOffset());
        }

        return offset;
    }

}

