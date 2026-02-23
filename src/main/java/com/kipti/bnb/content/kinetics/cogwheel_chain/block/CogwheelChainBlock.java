package com.kipti.bnb.content.kinetics.cogwheel_chain.block;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour.CogwheelChainBehaviour;
import com.kipti.bnb.content.kinetics.cogwheel_chain.shape.CogwheelChainBreakerHelper;
import com.kipti.bnb.registry.content.BnbBlockEntities;
import com.kipti.bnb.registry.content.blocks.BnbChainBlocks;
import com.kipti.bnb.registry.content.blocks.BnbKineticBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.decoration.encasing.EncasableBlock;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.IHaveBigOutline;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CogwheelChainBlock extends RotatedPillarKineticBlock
        implements IBE<CogwheelChainBlockEntity>, SpecialBlockItemRequirement, EncasableBlock, ICogwheelChainBlock, IHaveBigOutline { //TODO : waterlog state
    private static final List<CogwheelChainBlock> ALL_CHAIN_BLOCKS = new ArrayList<>();
    private static final Lazy<Map<Block, CogwheelChainBlock>> DEFAULT_CHAIN_BLOCKS_BY_SOURCE = Lazy.of(() -> {
        Map<Block, CogwheelChainBlock> map = new java.util.HashMap<>();
        for (CogwheelChainBlock chainBlock : ALL_CHAIN_BLOCKS) {
            BlockEntry<?> source = chainBlock.sourceBlock.get();
            map.put(source.get(), chainBlock);
        }
        return map;
    });

    protected CogwheelChainBlock(final boolean large, final Properties properties, final Supplier<BlockEntry<?>> sourceBlock) {
        super(properties);
        this.isLarge = large;
        this.sourceBlock = sourceBlock;
        ALL_CHAIN_BLOCKS.add(this);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemInteractionResult result = tryEncase(state, level, pos, stack, player, hand, hitResult);
        if (result.consumesAction())
            return result;

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(final @NotNull BlockState state, final @NotNull HitResult target, final @NotNull LevelReader level, final @NotNull BlockPos pos, final @NotNull Player player) {
        return sourceBlock.get().asStack();
    }

    public static @Nullable BlockState getChainState(final BlockState existingState, final boolean large, final Direction.Axis axis) {
        final Block chainBlock = DEFAULT_CHAIN_BLOCKS_BY_SOURCE.get().get(existingState.getBlock());
        if (chainBlock == null)
            return null;
        return chainBlock.defaultBlockState().setValue(AXIS, axis);
    }

    public boolean isLargeChainCog() {
        return isLarge;
    }

    private final boolean isLarge;
    private final Supplier<BlockEntry<?>> sourceBlock;

    public static CogwheelChainBlock smallFlanged(final Properties properties) {
        return new CogwheelChainBlock(false, properties, () -> BnbKineticBlocks.SMALL_EMPTY_FLANGED_COGWHEEL);
    }

    public static CogwheelChainBlock largeFlanged(final Properties properties) {
        return new CogwheelChainBlock(true, properties, () -> BnbKineticBlocks.LARGE_EMPTY_FLANGED_COGWHEEL);
    }

    @Override
    public InteractionResult onSneakWrenched(final BlockState state, final UseOnContext context) {
        final Level world = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final Player player = context.getPlayer();

        if (!world.isClientSide()) {
            CogwheelChainBreakerHelper.breakChain(world, pos, player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull VoxelShape getShape(final BlockState state, final @NotNull BlockGetter worldIn, final @NotNull BlockPos pos, final @NotNull CollisionContext context) {
        return (isLarge ? AllShapes.LARGE_GEAR : AllShapes.SMALL_GEAR).get(state.getValue(AXIS));
    }

    @Override
    public boolean canSurvive(final @NotNull BlockState state, final @NotNull LevelReader worldIn, final @NotNull BlockPos pos) {
        return CogWheelBlock.isValidCogwheelPosition(ICogWheel.isLargeCog(state), worldIn, pos, state.getValue(AXIS));
    }

    @Override
    public @NotNull BlockState playerWillDestroy(final Level level, final @NotNull BlockPos pos, final @NotNull BlockState state, final @NotNull Player player) {
        if (!level.isClientSide && player.hasInfiniteMaterials()) {
            SuperBlockEntityBehaviour.getOptional(level, pos, CogwheelChainBehaviour.TYPE)
                    .ifPresent(CogwheelChainBehaviour::clearStoredChains);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(final BlockState state, final Level world, final BlockPos pos, final BlockState newState, final boolean isMoving) {
        IBE.onRemove(state, world, pos, newState);
    }

    @Override
    public Class<CogwheelChainBlockEntity> getBlockEntityClass() {
        return CogwheelChainBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CogwheelChainBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.COGWHEEL_CHAIN.get();
    }

    @Override
    public Direction.Axis getRotationAxis(final BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public boolean hasShaftTowards(final LevelReader world, final BlockPos pos, final BlockState state, final Direction face) {
        return face.getAxis() == state.getValue(AXIS);
    }

    public final float getGeometryChainShift() {
        return BnbChainBlocks.SMALL_FLANGED_COGWHEEL_CHAIN.is(this) ? 1f / 8f : 0f;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(final @NotNull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public ItemRequirement getRequiredItems(final BlockState state, @Nullable final BlockEntity be) {
        return ItemRequirement.of(sourceBlock.get().getDefaultState(), be);
    }

    public BlockState getSourceBlockState() {
        return sourceBlock.get().getDefaultState();
    }

    @Override
    public boolean isLargeCog() {
        return isLargeChainCog();
    }
}

