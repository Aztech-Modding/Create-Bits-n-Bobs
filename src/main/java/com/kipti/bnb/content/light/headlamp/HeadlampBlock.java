package com.kipti.bnb.content.light.headlamp;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.light.founation.LightBlock;
import com.kipti.bnb.registry.BnbBlockEntities;
import com.kipti.bnb.registry.BnbBlocks;
import com.kipti.bnb.registry.BnbShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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

import java.util.Arrays;

//TODO SpecialBlockItemRequirement
public class HeadlampBlock extends LightBlock implements IBE<HeadlampBlockEntity>, IWrenchable{

    public HeadlampBlock(Properties p_52591_) {
        super(p_52591_, BnbShapes.LIGHTBULB_SHAPE);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer == null) {
            return;
        }

        Vec3 location = getPlayerLocationInBlock(pos, placer);
        withBlockEntityDo(level, pos, (headlampBlockEntity) -> headlampBlockEntity.placeHeadlampIntoBlock(
                location.subtract(pos.getCenter()), state.getValue(FACING)));
    }

    private static @NotNull Vec3 getPlayerLocationInBlock(BlockPos pos, @NotNull LivingEntity placer) {
        double range = 10;
        HitResult hitResult = placer.pick(range, 1, false);
        Vec3 location = hitResult.getLocation();
        Vec3 direction = placer.getLookAngle();

        AABB hitBox = new AABB(pos);
        location = hitBox.clip(location.add(direction.scale(2)), location).orElse(location);
        return location;
    }

    private Vec3 getPlayerLocationInBlockExact(BlockPos pos, Level level, @NotNull LivingEntity placer) {
        double range = 10;
        HitResult hitResult = placer.pick(range, 1, false);
        Vec3 location = hitResult.getLocation();
        Vec3 direction = placer.getLookAngle();

        BlockHitResult clip = level.getBlockState(pos).getShape(level, pos).clip(location, direction.scale(2), pos);
        if (clip == null) {
            return location;
        }
        return clip.getLocation();

    }

    @Override
    public boolean canBeReplaced(@NotNull BlockState state, BlockPlaceContext useContext) {
        if (useContext.isSecondaryUseActive())
            return false;
        ItemStack stack = useContext.getItemInHand();
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() != this)
            return super.canBeReplaced(state, useContext);
        Vec3 location = useContext.getClickLocation();

        BlockPos pos = useContext.getClickedPos();
        if (useContext.getLevel().getBlockEntity(pos) instanceof HeadlampBlockEntity headlampBlockEntity) {
            return headlampBlockEntity.canPlaceHeadlampIntoBlock(location.subtract(pos.getCenter()), state.getValue(FACING));
        }
        return super.canBeReplaced(state, useContext);
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext pContext) {
        BlockState stateForPlacement = super.getStateForPlacement(pContext);
        if (stateForPlacement == null)
            return null;

        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        BlockState blockState = level.getBlockState(pos);
        HeadlampBlockEntity fpbe = getBlockEntity(level, pos);

        Vec3 location = pContext.getClickLocation().subtract(pos.getCenter());
        Direction facing = stateForPlacement.getValue(FACING);

        if (blockState.is(this) && location != null && fpbe != null) {
            if (!level.isClientSide()) {
                Vec3 finalLocation = location;
                withBlockEntityDo(level, pos, (headlampBlockEntity) -> headlampBlockEntity
                        .placeHeadlampIntoBlock(finalLocation, facing));
                level.playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS);
            }
            stateForPlacement = blockState;
        }
        return stateForPlacement;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, Player player, @NotNull InteractionHand interactionHand, @NotNull BlockHitResult blockHitResult) {
        ItemStack stack = player.getItemInHand(interactionHand);
        if (stack.getItem() instanceof DyeItem dyeItem) {
            if (level.getBlockEntity(blockPos) instanceof HeadlampBlockEntity headlampBlockEntity) {
                headlampBlockEntity.placeDyeColorIntoBlock(
                    dyeItem.getDyeColor(),
                    blockHitResult.getLocation().subtract(blockPos.getCenter()),
                    blockState.getValue(FACING)
                );
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        Vec3 location = getPlayerLocationInBlockExact(pos, level, player);
        if (!player.isCrouching() && level.getBlockEntity(pos) instanceof HeadlampBlockEntity headlampBlockEntity &&
                headlampBlockEntity.removeNearestHeadlamp(location.subtract(pos.getCenter()), state.getValue(FACING))) {
            if (!level.isClientSide && !player.isCreative()) {
                HeadlampBlock.popResource(level, pos, BnbBlocks.HEADLAMP.asStack());
            }
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if (newState != null && newState.is(state.getBlock())) {
            super.onRemove(state, level, pos, newState, movedByPiston);
            return; // Block is being replaced by the same block, do nothing
        }
        if (level.getBlockEntity(pos) instanceof HeadlampBlockEntity headlampBlockEntity) {
            ItemStack additionalResources = BnbBlocks.HEADLAMP.asStack().copyWithCount(Math.min(Math.max(headlampBlockEntity.getExistingPlacements().size() - 1, 0), 3));
            if (!additionalResources.isEmpty())
                HeadlampBlock.popResource(level, pos, additionalResources);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        HeadlampBlockEntity headlampBlockEntity = (HeadlampBlockEntity) context.getLevel().getBlockEntity(pos);
        Vec3 location = getPlayerLocationInBlockExact(context.getClickedPos(), context.getLevel(), context.getPlayer());

        if (!(world instanceof ServerLevel serverLevel))
            return InteractionResult.SUCCESS;

        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, world.getBlockState(pos), player);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled())
            return InteractionResult.SUCCESS;

        if (player != null && !player.isCreative()) {
            Block.getDrops(state, serverLevel, pos, world.getBlockEntity(pos), player, context.getItemInHand()).forEach(itemStack -> player.getInventory().placeItemBackInInventory(itemStack));
        }

        state.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY, true);

        // middle headlamps is sometimes weird.
        if (headlampBlockEntity.getExistingPlacements().toArray().length > 1) {
            headlampBlockEntity.removeNearestHeadlamp(location.subtract(context.getClickedPos().getCenter()), state.getValue(FACING));
        } else {
            world.destroyBlock(pos, false);
        }


        IWrenchable.playRemoveSound(world, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return level.getBlockEntity(pos) instanceof HeadlampBlockEntity headlampBlockEntity ?
                headlampBlockEntity.getShape(state, level, pos, context) :
                Shapes.block();
    }

    @Override
    public @NotNull BlockState rotate(@NotNull BlockState state, @NotNull Rotation rotation) {
        return super.rotate(state, rotation)
                .setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public boolean hasDynamicShape() {
        return true;
    }

    @Override
    public Class<HeadlampBlockEntity> getBlockEntityClass() {
        return HeadlampBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends HeadlampBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.HEADLAMP.get();
    }

    @Override
    public @NotNull VoxelShape getVisualShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return super.getVisualShape(state, level, pos, context);
    }
}
