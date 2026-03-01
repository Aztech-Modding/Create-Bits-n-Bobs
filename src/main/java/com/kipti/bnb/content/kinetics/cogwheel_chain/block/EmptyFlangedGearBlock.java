package com.kipti.bnb.content.kinetics.cogwheel_chain.block;

import com.kipti.bnb.registry.client.BnbShapes;
import com.kipti.bnb.registry.content.BnbBlockEntities;
import com.simibubi.create.content.decoration.encasing.EncasableBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class EmptyFlangedGearBlock extends RotatedPillarKineticBlock implements IBE<KineticBlockEntity>, EncasableBlock, IExclusiveCogwheelChainBlock {

    final private boolean isLarge;

    public EmptyFlangedGearBlock(final Properties properties, final boolean large) {
        super(properties);
        this.isLarge = large;
    }

    @Override
    protected ItemInteractionResult useItemOn(final ItemStack stack, final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hitResult) {
        final ItemInteractionResult result = tryEncase(state, level, pos, stack, player, hand, hitResult);
        if (result.consumesAction())
            return result;

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected @NotNull VoxelShape getShape(final BlockState state, final @NotNull BlockGetter level, final @NotNull BlockPos pos, final @NotNull CollisionContext context) {
        return (isLarge ? BnbShapes.LARGE_FLANGED_GEAR : BnbShapes.SMALL_FLANGED_GEAR).get(state.getValue(AXIS));
    }

    public static EmptyFlangedGearBlock small(final Properties properties) {
        return new EmptyFlangedGearBlock(properties, false);
    }

    public static EmptyFlangedGearBlock large(final Properties properties) {
        return new EmptyFlangedGearBlock(properties, true);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(final @NotNull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public Direction.Axis getRotationAxis(final BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public boolean hasShaftTowards(final LevelReader world, final BlockPos pos, final BlockState state, final Direction face) {
        return state.getValue(AXIS) == face.getAxis();
    }


    @Override
    public Class<KineticBlockEntity> getBlockEntityClass() {
        return KineticBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.SIMPLE_KINETIC.get();
    }

    @Override
    public boolean isLargeCog() {
        return isLarge;
    }
}

