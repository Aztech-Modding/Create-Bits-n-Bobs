package com.kipti.bnb.content.trinkets.nixie.foundation;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.trinkets.nixie.nixie_board.NixieBoardBlockNixie;
import com.kipti.bnb.registry.content.blocks.BnbTrinketBlocks;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Started off as a generic display block that can be oriented in two directions, but contains code for the displays in general.
 */
public class GenericNixieDisplayBlock extends DirectionalBlock implements IWrenchable {

    public static final MapCodec<GenericNixieDisplayBlock> CODEC = simpleCodec(GenericNixieDisplayBlock::new);

    public static final DirectionProperty ORIENTATION = DirectionProperty.create("orientation");

    public GenericNixieDisplayBlock(final Properties p_52591_) {
        super(p_52591_);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP).setValue(ORIENTATION, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(final @NotNull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, ORIENTATION);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(final BlockPlaceContext context) {
        final Direction surface = context.getClickedFace();
        Direction facing = context.getNearestLookingDirection();
        if (facing.getAxis() == surface.getAxis()) {
            facing = Arrays.stream(Direction.values())
                    .filter(dir -> dir.getAxis() != surface.getAxis())

                    .min(Comparator.comparingDouble(dir -> Vec3.atLowerCornerOf(dir.getNormal())
                            .distanceToSqr(Objects.requireNonNull(context.getPlayer()).getLookAngle())))

                    .orElse(Direction.NORTH);
        }
        final BlockState stateForPlacement = super.getStateForPlacement(context);
        return stateForPlacement == null ? null : stateForPlacement
                .setValue(FACING, surface)
                .setValue(ORIENTATION, facing.getOpposite());
    }

    @Override
    public InteractionResult onWrenched(final BlockState state, final UseOnContext context) {
        final GenericNixieDisplayBlockEntity be = (GenericNixieDisplayBlockEntity) context.getLevel().getBlockEntity(context.getClickedPos());
        final GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions currentOption = be.getCurrentDisplayOption();
        final List<GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions> options = be.getPossibleDisplayOptions();
        final int currentIndex = options.indexOf(currentOption);
        if (currentIndex < 0) {
            CreateBitsnBobs.LOGGER.warn("No valid display option found for {}", be.getBlockPos());
            return InteractionResult.PASS;
        }
        final int nextIndex = (currentIndex + 1) % options.size();
        final GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions nextOption = options.get(nextIndex);
        be.applyToEachElementOfThisStructure((display) -> {
        });

        GenericNixieDisplayTarget.walkNixies(context.getLevel(), context.getClickedPos(), (currentPos, consumedCharsOnRow, blockEntity) -> {
            blockEntity.setPositionOffset(consumedCharsOnRow);
            blockEntity.setDisplayOption(nextOption);
        });
        //Purposefully don't allow default behaviour since that gives access to illegal states
        return InteractionResult.SUCCESS;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(final ItemStack stack,
                                                       final @NotNull BlockState state,
                                                       final @NotNull Level level,
                                                       final @NotNull BlockPos pos,
                                                       final @NotNull Player player,
                                                       final @NotNull InteractionHand hand,
                                                       final @NotNull BlockHitResult hitResult) {
        final boolean display =
                stack.getItem() == Items.NAME_TAG && stack.has(DataComponents.CUSTOM_NAME) || AllBlocks.CLIPBOARD.isIn(stack);
        final DyeColor dye = DyeColor.getColor(stack);

        if (!display && dye == null || (!(level.getBlockEntity(pos) instanceof final GenericNixieDisplayBlockEntity startBlockEntity)))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (dye != null) {
            GenericNixieDisplayTarget.walkNixies(level, pos, (currentPos, consumedCharsOnRow, initialBE) -> {
                final BlockState coloredState = withColor(initialBE.getBlockState(), dye);
                level.setBlock(currentPos, coloredState, 3);

                final BlockEntity be = level.getBlockEntity(currentPos);
                if (be instanceof final GenericNixieDisplayBlockEntity currentBe) {
                    currentBe.inheritDataFrom(initialBE);
                }
            });
            return ItemInteractionResult.SUCCESS;
        }

        Component component = stack.getOrDefault(DataComponents.CUSTOM_NAME, Component.empty());
        @Nullable Component secondRowComponent = null;

        boolean forceFromTop = false;

        if (AllBlocks.CLIPBOARD.isIn(stack)) {
            final List<ClipboardEntry> entries = ClipboardEntry.getLastViewedEntries(stack);
            component = !entries.isEmpty() ? entries.getFirst().text : Component.empty();
            if (entries.size() > 1) {
                secondRowComponent = entries.get(1).text;
                forceFromTop = true;
            }
        }

        if (level.isClientSide)
            return ItemInteractionResult.SUCCESS;

        final String tagUsed = Component.Serializer.toJson(component, level.registryAccess());
        final @Nullable String secondRowTagUsed = secondRowComponent == null ? null :
                Component.Serializer.toJson(secondRowComponent, level.registryAccess());

        final int startLine = forceFromTop ? 0 : getLineForPlacement(state, pos, hitResult, level);

        final GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions currentDisplay = startBlockEntity.getCurrentDisplayOption();
        GenericNixieDisplayTarget.walkNixies(level, pos, (currentPos, consumedCharsOnRow, blockEntity) -> {
            if (blockEntity instanceof final GenericNixieDisplayBlockEntity displayBlockEntity) {
                if (displayBlockEntity.getCurrentDisplayOption() != currentDisplay && displayBlockEntity.getPossibleDisplayOptions().contains(currentDisplay)) {
                    displayBlockEntity.setDisplayOption(currentDisplay);
                }

                displayBlockEntity.displayCustomText(tagUsed, consumedCharsOnRow, startLine);
                if (secondRowTagUsed != null)
                    displayBlockEntity.displayCustomText(secondRowTagUsed, consumedCharsOnRow, 1);
            }
        });

        return ItemInteractionResult.SUCCESS;
    }

    private static BlockState withColor(final BlockState state, final DyeColor color) {
        if (isLargeNixieTube(state.getBlock())) {
            final Block block = color == null ? BnbTrinketBlocks.LARGE_NIXIE_TUBE.get() : BnbTrinketBlocks.DYED_LARGE_NIXIE_TUBE.get(color).get();
            return BlockHelper.copyProperties(state, block.defaultBlockState());
        } else if (isNixieBoard(state.getBlock())) {
            final Block block = color == null ? BnbTrinketBlocks.NIXIE_BOARD.get() : BnbTrinketBlocks.DYED_NIXIE_BOARD.get(color).get();
            return BlockHelper.copyProperties(state, block.defaultBlockState());
        }
        return state;
    }

    public static boolean isLargeNixieTube(final Block block) {
        return BnbTrinketBlocks.DYED_LARGE_NIXIE_TUBE.contains(block) || BnbTrinketBlocks.LARGE_NIXIE_TUBE.is(block);
    }

    public static boolean isNixieBoard(final Block block) {
        return BnbTrinketBlocks.DYED_NIXIE_BOARD.contains(block) || BnbTrinketBlocks.NIXIE_BOARD.is(block);
    }

    private int getLineForPlacement(final BlockState state, final BlockPos blockPos, final BlockHitResult hitResult, final Level level) {
        if (!(state.getBlock() instanceof NixieBoardBlockNixie)) {
            return 0; // Nixie tubes always place on the first line
        }
        final BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof GenericNixieDisplayBlockEntity)) {
            return 0; // Not a Nixie display block entity
        }
        if (((GenericNixieDisplayBlockEntity) blockEntity).currentDisplayOption.lines == 1) {
            return 0; // Single line display, always place on the first line
        }

        final Matrix4f rotation = DoubleOrientedBlockModel.getRotation(state.getValue(FACING), state.getValue(ORIENTATION));
        final Vec3 globalPos = hitResult.getLocation().subtract(blockPos.getCenter());
        final Vector4f localPos = rotation.invert().transform(new Vector4f(globalPos.toVector3f(), 1f));
        if (localPos.y < 0) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    protected @NotNull MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

}

