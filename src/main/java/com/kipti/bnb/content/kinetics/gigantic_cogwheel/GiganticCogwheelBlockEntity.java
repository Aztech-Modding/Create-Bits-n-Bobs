package com.kipti.bnb.content.kinetics.gigantic_cogwheel;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Block entity for the gigantic cogwheel center block.
 * Handles custom texture application, kinetic propagation to nearby small
 * cogwheels at a 5:1 speed ratio, and bidirectional connection validation.
 */
public class GiganticCogwheelBlockEntity extends KineticBlockEntity {

    public BlockState material;

    public GiganticCogwheelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.material = Blocks.SPRUCE_PLANKS.defaultBlockState();
        this.setLazyTickRate(20);
    }

    public ItemInteractionResult applyMaterialIfValid(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        BlockState material = blockItem.getBlock().defaultBlockState();
        if (material == this.material)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!material.is(BlockTags.PLANKS))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (this.level.isClientSide() && !this.isVirtual())
            return ItemInteractionResult.SUCCESS;
        this.material = material;
        this.notifyUpdate();
        this.level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, this.worldPosition, Block.getId(material));
        return ItemInteractionResult.SUCCESS;
    }

    private void redraw() {
        if (!this.isVirtual())
            this.requestModelDataUpdate();
        if (this.hasLevel()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 16);
            this.level.getChunkSource().getLightEngine().checkBlock(this.worldPosition);
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (this.level == null || this.level.isClientSide) return;
        final BlockState state = this.getBlockState();
        if (!(state.getBlock() instanceof final GiganticCogwheelBlock gigantic)) return;
        final Direction.Axis axis = gigantic.getRotationAxis(state);
        for (final Direction dir : Direction.values()) {
            if (dir.getAxis() == axis) continue;
            final BlockPos targetPos = this.worldPosition.relative(dir, 3);
            final BlockState targetState = this.level.getBlockState(targetPos);
            if (!(targetState.getBlock() instanceof final ICogWheel cog)) continue;
            if (cog.isLargeCog()) continue;
            if (cog.getRotationAxis(targetState) != axis) continue;
            final BlockEntity be = this.level.getBlockEntity(targetPos);
            if (be instanceof final KineticBlockEntity kbe) {
                if (kbe.hasSource() && !this.hasSource()) {
                    kbe.detachKinetics();
                    kbe.updateSpeed = true;
                    kbe.attachKinetics();
                    return;
                }
            }
        }
    }

    @Override
    public List<BlockPos> addPropagationLocations(final IRotate block,
                                                  final BlockState state,
                                                  final List<BlockPos> neighbours) {
        final List<BlockPos> result = super.addPropagationLocations(block, state, neighbours);

        if (!(block instanceof final GiganticCogwheelBlock gigantic) || this.level == null)
            return result;

        final Direction.Axis axis = gigantic.getRotationAxis(state);

        for (final Direction dir : Direction.values()) {
            if (dir.getAxis() == axis) continue;

            final BlockPos targetPos = this.worldPosition.relative(dir, 3);
            final BlockState targetState = this.level.getBlockState(targetPos);
            if (!(targetState.getBlock() instanceof final ICogWheel cog)) continue;
            if (cog.isLargeCog()) continue;
            if (cog.getRotationAxis(targetState) != axis) continue;

            if (!result.contains(targetPos))
                result.add(targetPos);
        }

        return result;
    }

    @Override
    public boolean isCustomConnection(final KineticBlockEntity other,
                                      final BlockState state,
                                      final BlockState otherState) {
        if (state.getBlock() instanceof final GiganticCogwheelBlock gigantic
                && otherState.getBlock() instanceof final ICogWheel cog
                && !cog.isLargeCog()) {

            final Direction.Axis axis = gigantic.getRotationAxis(state);
            if (cog.getRotationAxis(otherState) != axis) return false;

            final BlockPos diff = other.getBlockPos().subtract(this.worldPosition);
            return isValidDiff(axis, diff);
        }

        if (otherState.getBlock() instanceof final GiganticCogwheelBlock gigantic
                && state.getBlock() instanceof final ICogWheel cog
                && !cog.isLargeCog()) {

            final Direction.Axis axis = gigantic.getRotationAxis(otherState);
            if (cog.getRotationAxis(state) != axis) return false;

            final BlockPos diff = this.worldPosition.subtract(other.getBlockPos());
            return isValidDiff(axis, diff);
        }

        return false;
    }

    @Override
    public float propagateRotationTo(final KineticBlockEntity target,
                                     final BlockState stateFrom, final BlockState stateTo,
                                     final BlockPos diff,
                                     final boolean connectedViaAxes, final boolean connectedViaCogs) {

        if (stateFrom.getBlock() instanceof final GiganticCogwheelBlock gigantic
                && stateTo.getBlock() instanceof final ICogWheel cog
                && !cog.isLargeCog()) {

            final Direction.Axis axis = gigantic.getRotationAxis(stateFrom);
            if (cog.getRotationAxis(stateTo) != axis) return 0;
            if (!isValidDiff(axis, diff)) return 0;

            return -5.0f;
        }

        if (stateFrom.getBlock() instanceof final ICogWheel cog
                && !cog.isLargeCog()
                && stateTo.getBlock() instanceof final GiganticCogwheelBlock gigantic) {

            final Direction.Axis axis = gigantic.getRotationAxis(stateTo);
            if (cog.getRotationAxis(stateFrom) != axis) return 0;
            if (!isValidDiff(axis, diff)) return 0;

            return -1f / 5;
        }

        return 0;
    }

    public static boolean isValidDiff(final Direction.Axis axis, final BlockPos diff) {
        return switch (axis) {
            case X ->
                    diff.getX() == 0 && (Math.abs(diff.getY()) == 3 && diff.getZ() == 0 || Math.abs(diff.getZ()) == 3 && diff.getY() == 0);
            case Y ->
                    diff.getY() == 0 && (Math.abs(diff.getX()) == 3 && diff.getZ() == 0 || Math.abs(diff.getZ()) == 3 && diff.getX() == 0);
            case Z ->
                    diff.getZ() == 0 && (Math.abs(diff.getX()) == 3 && diff.getY() == 0 || Math.abs(diff.getY()) == 3 && diff.getX() == 0);
        };
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(this.worldPosition).inflate(1.5);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);

        BlockState prevMaterial = this.material;
        if (compound.contains("Material")) {
            this.material = NbtUtils.readBlockState(this.blockHolderGetter(), compound.getCompound("Material"));
            if (this.material.isAir())
                this.material = Blocks.SPRUCE_PLANKS.defaultBlockState();
            if (clientPacket && prevMaterial != this.material) {
                this.redraw();
                CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> VisualizationHelper.queueUpdate(this));
            }
        }
    }

    @Override
    public void writeSafe(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeSafe(tag, registries);
        tag.put("Material", NbtUtils.writeBlockState(this.material));
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Material", NbtUtils.writeBlockState(this.material));
    }
}