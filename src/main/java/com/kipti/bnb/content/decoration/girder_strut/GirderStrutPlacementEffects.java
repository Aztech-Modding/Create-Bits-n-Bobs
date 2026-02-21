package com.kipti.bnb.content.decoration.girder_strut;

import com.kipti.bnb.registry.core.BnbDataComponents;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class GirderStrutPlacementEffects {

    private static final float PARTICLE_DENSITY = 0.1f;

    public static void tick(final LocalPlayer player) {
        if (Minecraft.getInstance().isPaused() || Minecraft.getInstance().hitResult == null) return;

        //Get held item
        final ItemStack heldItem = player.getMainHandItem().getItem() instanceof GirderStrutBlockItem ? player.getMainHandItem() :
                player.getOffhandItem().getItem() instanceof GirderStrutBlockItem ? player.getOffhandItem() : null;
        if (heldItem != null) {
            display(player, heldItem);
        }
    }

    private static void display(final LocalPlayer player, final ItemStack heldItem) {
        final ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        final BlockPos fromPos = heldItem.get(BnbDataComponents.GIRDER_STRUT_FROM);
        final Direction fromFace = heldItem.get(BnbDataComponents.GIRDER_STRUT_FROM_FACE);

        if (fromPos == null) {
            return;
        }

        final HitResult genericHit = Minecraft.getInstance().hitResult;
        if (!(genericHit instanceof final BlockHitResult hit) || genericHit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        final BlockPos targetPos = resolvePlacementPos(level, hit.getBlockPos(), hit.getDirection());
        if (targetPos == null || targetPos.distSqr(fromPos) > GirderStrutBlock.MAX_SPAN * GirderStrutBlock.MAX_SPAN * 1.5) {
            return;
        }

        Direction targetFace = hit.getDirection();

        final BlockState targetState = level.getBlockState(targetPos);
        if (targetState.getBlock() instanceof GirderStrutBlock) {
            targetFace = targetState.getValue(GirderStrutBlock.FACING);
        }

        final Vec3 renderFrom = Vec3.atCenterOf(fromPos);
        final Vec3 renderTo = Vec3.atCenterOf(targetPos);

        final Vec3 delta = renderTo.subtract(renderFrom);
        final double length = delta.length();
        if (length < 1.0E-3 || length > GirderStrutBlock.MAX_SPAN * 3) {
            return;
        }

        final boolean valid = GirderStrutBlockItem.isValidConnection(level, fromPos, fromFace, targetPos, targetFace);

        final Vec3 dir = delta.normalize();
        final double step = 0.25;
        // 95CD41 valid and EA5C2B invalid
        final Vector3f color = valid ? new Vector3f(.3f, .9f, .5f) : new Vector3f(.9f, .3f, .5f);
        final Vector3f outlinerColor = valid ? new Vector3f(.35f, .85f, .55f) : new Vector3f(.85f, .35f, .55f);
        for (double t = 0; t <= length; t += step) {
            final Vec3 lerped = renderFrom.add(dir.scale(t));

            if (level.getRandom().nextFloat() > PARTICLE_DENSITY) {
                continue;
            }

            level.addParticle(
                    new DustParticleOptions(color, 1), true,
                    lerped.x, lerped.y, lerped.z, 0, 0, 0);
        }
        level.addParticle(
                new DustParticleOptions(color, 1), true,
                renderTo.x, renderTo.y, renderTo.z, 0, 0, 0);

        showAnchorBox(fromPos, fromFace.getOpposite(), "from", (int) (outlinerColor.x * 256), (int) (outlinerColor.y * 256), (int) (outlinerColor.z * 256));
        showAnchorBox(targetPos, targetFace.getOpposite(), "to", (int) (outlinerColor.x * 256), (int) (outlinerColor.y * 256), (int) (outlinerColor.z * 256));

    }

    private static void showAnchorBox(final BlockPos targetPos, final Direction targetFace, final String id, final int r, final int g, final int b) {
        AABB box = new AABB(
                0, 0, 0, (targetFace.getStepX() == 0 ? 0.25f : 0f) + 0.25f, (targetFace.getStepY() == 0 ? 0.25f : 0f) + 0.25f, (targetFace.getStepZ() == 0 ? 0.25f : 0f) + 0.25f
        );
        box = box
                .move(targetPos.getX(), targetPos.getY(), targetPos.getZ())
                .move(-box.getXsize() * 0.5f + 0.5f, -box.getYsize() * 0.5f + 0.5f, -box.getZsize() * 0.5f + 0.5f)
                .move(targetFace.getStepX() * 0.5f, targetFace.getStepY() * 0.5f, targetFace.getStepZ() * 0.5f);
        Outliner.getInstance().showAABB(id, box).colored(new Color(r, g, b)).lineWidth(1 / 16f);
    }

    private static BlockPos resolvePlacementPos(final ClientLevel level, final BlockPos clickedPos, final Direction face) {
        BlockPos pos = clickedPos;
        if (!(level.getBlockState(pos).getBlock() instanceof GirderStrutBlock)) {
            pos = pos.relative(face);
            if (!(level.getBlockState(pos).canBeReplaced() || level.getBlockState(pos).getBlock() instanceof GirderStrutBlock)) {
                return null;
            }
        }
        return pos;
    }

}

