package com.kipti.bnb.content.trinkets.light.headlamp;

import com.kipti.bnb.content.trinkets.light.foundation.LightBlock;
import com.kipti.bnb.registry.client.BnbShapes;
import com.kipti.bnb.registry.content.BnbBlockEntities;
import com.kipti.bnb.registry.content.blocks.BnbTrinketBlocks;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public class HeadlampBlock extends LightBlock implements IBE<HeadlampBlockEntity>, IWrenchable {

    public HeadlampBlock(final Properties p_52591_) {
        super(p_52591_, BnbShapes.LIGHTBULB_SHAPE);
    }

    @Override
    public void setPlacedBy(final @NotNull Level level, final @NotNull BlockPos pos, final @NotNull BlockState state, @Nullable final LivingEntity placer, final @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer == null) {
            return;
        }

        final Vec3 location = getPlayerLocationInBlock(pos, placer);
        withBlockEntityDo(level, pos, (headlampBlockEntity) -> headlampBlockEntity.placeHeadlampIntoBlock(
                location.subtract(pos.getCenter()), state.getValue(FACING)));
    }

    private static @NotNull Vec3 getPlayerLocationInBlock(final BlockPos pos, @NotNull final LivingEntity placer) {
        final double range = placer.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;
        final HitResult hitResult = placer.pick(range, 1, false);
        Vec3 location = hitResult.getLocation();
        final Vec3 direction = placer.getLookAngle();

        final AABB hitBox = new AABB(pos);
        location = hitBox.clip(location.add(direction.scale(2)), location).orElse(location);
        return location;
    }

    @Override
    public boolean hasDynamicShape() {
        return true;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull final BlockPlaceContext pContext) {
        final AtomicReference<BlockState> stateForPlacement = new AtomicReference<>(super.getStateForPlacement(pContext));
        if (stateForPlacement.get() == null)
            return null;

        final Level level = pContext.getLevel();
        final BlockPos pos = pContext.getClickedPos();
        final BlockState blockState = level.getBlockState(pos);
        final HeadlampBlockEntity fpbe = getBlockEntity(level, pos);

        final Vec3 location = pContext.getClickLocation().subtract(pos.getCenter());
        final Direction facing = stateForPlacement.get().getValue(FACING);

        if (blockState.is(this) && fpbe != null) {
            if (!level.isClientSide()) {
                withBlockEntityDo(level, pos, (headlampBlockEntity) -> {
                    final boolean placed = headlampBlockEntity
                            .placeHeadlampIntoBlock(location, facing);
                    if (!placed) {
                        stateForPlacement.set(null);
                    }
                });
                level.playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS);
            }
            stateForPlacement.set(blockState);
        }
        return stateForPlacement.get();
    }

    @Override
    protected @NotNull VoxelShape getShape(final BlockState state, @NotNull final BlockGetter level, @NotNull final BlockPos pos, @NotNull final CollisionContext context) {
        return level.getBlockEntity(pos) instanceof final HeadlampBlockEntity headlampBlockEntity ?
                headlampBlockEntity.getShape(state, level, pos, context) :
                Shapes.block();
    }

    @Override
    public boolean onDestroyedByPlayer(final @NotNull BlockState state, final @NotNull Level level, final @NotNull BlockPos pos, final @NotNull Player player, final boolean willHarvest, final @NotNull FluidState fluid) {
        final Vec3 location = getPlayerLocationInBlockExact(pos, level, player);
        if (!player.isCrouching() && level.getBlockEntity(pos) instanceof final HeadlampBlockEntity headlampBlockEntity &&
                headlampBlockEntity.removeNearestHeadlamp(location.subtract(pos.getCenter()), state.getValue(FACING))) {
            if (!level.isClientSide && !player.isCreative()) {
                HeadlampBlock.popResource(level, pos, BnbTrinketBlocks.HEADLAMP.asStack());
            }
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    private Vec3 getPlayerLocationInBlockExact(final BlockPos pos, final Level level, @NotNull final LivingEntity placer) {
        final double range = placer.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;
        final HitResult hitResult = placer.pick(range, 1, false);
        final Vec3 location = hitResult.getLocation();
        final Vec3 direction = placer.getLookAngle();

        final BlockHitResult clip = level.getBlockState(pos).getShape(level, pos).clip(location, direction.scale(2), pos);
        if (clip == null) {
            return location;
        }
        return clip.getLocation();

    }

    @Override
    protected void onRemove(final BlockState state, final @NotNull Level level, final @NotNull BlockPos pos, final BlockState newState, final boolean movedByPiston) {
        if (newState.is(state.getBlock())) {
            super.onRemove(state, level, pos, newState, movedByPiston);
            return; // Block is being replaced by the same block, do nothing
        }
        if (level.getBlockEntity(pos) instanceof final HeadlampBlockEntity headlampBlockEntity) {
            final ItemStack additionalResources = BnbTrinketBlocks.HEADLAMP.asStack().copyWithCount(Math.clamp(headlampBlockEntity.getExistingPlacements().size() - 1, 0, 3));
            if (!additionalResources.isEmpty())
                HeadlampBlock.popResource(level, pos, additionalResources);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(final ItemStack stack, final @NotNull BlockState state, final @NotNull Level level, final @NotNull BlockPos pos, final @NotNull Player player, final @NotNull InteractionHand hand, final @NotNull BlockHitResult hitResult) {
        if (stack.getItem() instanceof final DyeItem dyeItem) {
            if (level.getBlockEntity(pos) instanceof final HeadlampBlockEntity headlampBlockEntity) {
                headlampBlockEntity.placeDyeColorIntoBlock(
                        dyeItem.getDyeColor(),
                        hitResult.getLocation().subtract(pos.getCenter()),
                        state.getValue(FACING)
                );
            }
            return ItemInteractionResult.SUCCESS;
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected @NotNull BlockState rotate(final @NotNull BlockState state, final @NotNull Rotation rotation) {
        return super.rotate(state, rotation)
                .setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public boolean canBeReplaced(final @NotNull BlockState state, final BlockPlaceContext useContext) {
        if (useContext.isSecondaryUseActive())
            return false;
        final ItemStack stack = useContext.getItemInHand();
        if (stack.getItem() instanceof final BlockItem blockItem && blockItem.getBlock() != this)
            return super.canBeReplaced(state, useContext);
        final Vec3 location = useContext.getClickLocation();

        final BlockPos pos = useContext.getClickedPos();
        if (useContext.getLevel().getBlockEntity(pos) instanceof final HeadlampBlockEntity headlampBlockEntity) {
            return headlampBlockEntity.canPlaceHeadlampIntoBlock(location.subtract(pos.getCenter()), state.getValue(FACING));
        }
        return super.canBeReplaced(state, useContext);
    }

    @Override
    protected boolean propagatesSkylightDown(final @NotNull BlockState state, final @NotNull BlockGetter level, final @NotNull BlockPos pos) {
        return true;
    }

    @Override
    public InteractionResult onSneakWrenched(final BlockState state, final UseOnContext context) {
        final Level world = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final Player player = context.getPlayer();
        final Vec3 location = getPlayerLocationInBlockExact(context.getClickedPos(), context.getLevel(), context.getPlayer());

        if (!(world instanceof final ServerLevel serverLevel))
            return InteractionResult.SUCCESS;

        final BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, world.getBlockState(pos), player);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled())
            return InteractionResult.SUCCESS;

        if (!player.isCreative()) {
            Block.getDrops(state, serverLevel, pos, world.getBlockEntity(pos), player, context.getItemInHand()).forEach(itemStack -> player.getInventory().placeItemBackInInventory(itemStack));
        }

        state.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY, true);

        withBlockEntityDo(world, pos, (headlampBlockEntity) -> {
            if (headlampBlockEntity.getExistingPlacements().size() > 1) {
                headlampBlockEntity.removeNearestHeadlamp(location.subtract(context.getClickedPos().getCenter()), state.getValue(FACING));
            } else {
                world.destroyBlock(pos, false);
            }
        });

        IWrenchable.playRemoveSound(world, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public Class<HeadlampBlockEntity> getBlockEntityClass() {
        return HeadlampBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends HeadlampBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.HEADLAMP.get();
    }

}

