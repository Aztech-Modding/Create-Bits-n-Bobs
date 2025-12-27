package com.kipti.bnb.content.decoration.light.headlamp;

import com.kipti.bnb.content.decoration.light.founation.LightBlock;
import com.kipti.bnb.registry.BnbBlockEntities;
import com.kipti.bnb.registry.BnbBlocks;
import com.kipti.bnb.registry.BnbShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO SpecialBlockItemRequirement
public class HeadlampBlock extends LightBlock implements IBE<HeadlampBlockEntity> {

    public HeadlampBlock(Properties p_52591_) {
        super(p_52591_, BnbShapes.LIGHTBULB_SHAPE);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer == null) {
            return;
        }

        Vec3 location = getPlayerLocationInBlock(pos, placer);
        withBlockEntityDo(level, pos, (headlampBlockEntity) -> headlampBlockEntity.placeHeadlampIntoBlock(
                location.subtract(pos.getCenter()), state.getValue(FACING)));
    }

    private static @NotNull Vec3 getPlayerLocationInBlock(BlockPos pos, @NotNull LivingEntity placer) {
        double range = placer.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;
        HitResult hitResult = placer.pick(range, 1, false);
        Vec3 location = hitResult.getLocation();
        Vec3 direction = placer.getLookAngle();

        AABB hitBox = new AABB(pos);
        location = hitBox.clip(location.add(direction.scale(2)), location).orElse(location);
        return location;
    }

    private Vec3 getPlayerLocationInBlockExact(BlockPos pos, Level level, @NotNull LivingEntity placer) {
        double range = placer.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;
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
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof DyeItem dyeItem) {
            if (level.getBlockEntity(pos) instanceof HeadlampBlockEntity headlampBlockEntity) {
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
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (newState != null && newState.is(state.getBlock())) {
            super.onRemove(state, level, pos, newState, movedByPiston);
            return; // Block is being replaced by the same block, do nothing
        }
        if (level.getBlockEntity(pos) instanceof HeadlampBlockEntity headlampBlockEntity) {
            ItemStack additionalResources = BnbBlocks.HEADLAMP.asStack().copyWithCount(Math.clamp(headlampBlockEntity.getExistingPlacements().size() - 1, 0, 3));
            if (!additionalResources.isEmpty())
                HeadlampBlock.popResource(level, pos, additionalResources);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Vec3 location = getPlayerLocationInBlockExact(context.getClickedPos(), context.getLevel(), context.getPlayer());
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof HeadlampBlockEntity headlampBlockEntity &&
                headlampBlockEntity.removeNearestHeadlamp(location.subtract(context.getClickedPos().getCenter()), state.getValue(FACING))) {
            if (!context.getLevel().isClientSide && !context.getPlayer().isCreative()) {
                context.getPlayer().addItem(BnbBlocks.HEADLAMP.asStack());
            }
            IWrenchable.playRemoveSound(context.getLevel(), context.getClickedPos());
            return InteractionResult.SUCCESS;
        }
        return super.onSneakWrenched(state, context);
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return level.getBlockEntity(pos) instanceof HeadlampBlockEntity headlampBlockEntity ?
                headlampBlockEntity.getShape(state, level, pos, context) :
                Shapes.block();
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
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
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return super.getVisualShape(state, level, pos, context);
    }
}
