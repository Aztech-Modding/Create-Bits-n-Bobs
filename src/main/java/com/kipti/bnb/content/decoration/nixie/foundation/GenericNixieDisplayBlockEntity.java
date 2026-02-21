package com.kipti.bnb.content.decoration.nixie.foundation;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.decoration.nixie.nixie_board.NixieBoardBlockNixie;
import com.kipti.bnb.mixin_accessor.DynamicComponentMigrator;
import com.kipti.bnb.registry.content.blocks.BnbBlocksBootstrap;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.DynamicComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GenericNixieDisplayBlockEntity extends SmartBlockEntity {

    private static final int MAX_CHARS_IN_BLOCK = 3;

    private static final Logger log = LoggerFactory.getLogger(GenericNixieDisplayBlockEntity.class);

    private Optional<DynamicComponent> customTextTop = Optional.empty();
    private Optional<DynamicComponent> customTextBottom = Optional.empty();
    private int customTextStart = 0;

    private String renderedTextTop = "";
    private String renderedTextBottom = "";

    protected ConfigurableDisplayOptions currentDisplayOption = ConfigurableDisplayOptions.NONE;

    public GenericNixieDisplayBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void initialize() {
        if (level.isClientSide)
            updateDisplayedStrings();
    }

    public void inheritDataFrom(final GenericNixieDisplayBlockEntity be) {
        this.customTextTop = be.customTextTop;
        this.customTextBottom = be.customTextBottom;
        this.customTextStart = be.customTextStart;

        this.currentDisplayOption = be.currentDisplayOption;

        notifyUpdate();
    }

    public ConfigurableDisplayOptions getCurrentDisplayOption() {
        return currentDisplayOption;
    }

    public char getRenderedText(int line, int width) {
        if (line == 0) {
            if (width < renderedTextTop.length()) {
                return renderedTextTop.charAt(width);
            }
        } else if (line == 1) {
            if (width < renderedTextBottom.length()) {
                return renderedTextBottom.charAt(width);
            }
        }
        return ' ';
    }

    public void setPositionOffset(final int consumedCharsOnRow) {
        this.customTextStart = consumedCharsOnRow;
    }

    public enum ConfigurableDisplayOptions {
        NONE(1, 1, () -> ConfigurableDisplayOptionTransform.NONE),
        ALWAYS_UP(1, 1, () -> ConfigurableDisplayOptionTransform.ALWAYS_UP),
        DOUBLE_CHAR(2, 1, () -> ConfigurableDisplayOptionTransform.DOUBLE_CHAR),
        DOUBLE_CHAR_DOUBLE_LINES(3, 2, () -> ConfigurableDisplayOptionTransform.DOUBLE_CHAR_DOUBLE_LINES);

        public final Supplier<ConfigurableDisplayOptionTransform> renderTransform;
        public final int width;
        public final int lines;

        ConfigurableDisplayOptions(final int width, final int lines, final Supplier<ConfigurableDisplayOptionTransform> renderTransform) {
            this.renderTransform = renderTransform;
            this.width = width;
            this.lines = lines;
        }

    }

    public List<ConfigurableDisplayOptions> getPossibleDisplayOptions() {
        return ((IGenericNixieDisplayBlock) getBlockState().getBlock()).getPossibleDisplayOptions();
    }

    public void applyToEachElementOfThisStructure(final Consumer<GenericNixieDisplayBlockEntity> consumer) {
        final GenericNixieDisplayBlockEntity controller = findControllerBlockEntity();
        final Direction facing = getBlockState().getValue(GenericNixieDisplayBlock.FACING);
        final Direction orientation = getBlockState().getValue(GenericNixieDisplayBlock.ORIENTATION);
        final Direction right = DoubleOrientedDirections.getLeft(facing, orientation).getOpposite();
        BlockPos currentPos = controller.getBlockPos();
        for (int i = 0; i < 100; i++) {
            final BlockEntity blockEntity = controller.level.getBlockEntity(currentPos);
            if (blockEntity instanceof final GenericNixieDisplayBlockEntity display && areStatesComprableForConnection(controller.getBlockState(), display.getBlockState())) {
                consumer.accept(display);
            } else {
                break; // No more display found
            }
            currentPos = currentPos.relative(right);
        }
    }

    public void setDisplayOption(final ConfigurableDisplayOptions option) {
        if (currentDisplayOption == option) {
            return; // No change
        }
        currentDisplayOption = option;
        if (level.isClientSide)
            updateDisplayedStrings();
        notifyUpdate();
    }

    /*
     * Apparently, comprable isn't a word, i do not care, I will use it anyway.
     * */
    public static boolean areStatesComprableForConnection(final BlockState state1, final BlockState state2) {
        if (state1 == null || state2 == null) {
            return false;
        }

        final boolean stateOneIsBoard = BnbBlocksBootstrap.NIXIE_BOARD.is(state1.getBlock()) || BnbBlocksBootstrap.DYED_NIXIE_BOARD.contains(state1.getBlock());
        final boolean stateTwoIsBoard = BnbBlocksBootstrap.NIXIE_BOARD.is(state2.getBlock()) || BnbBlocksBootstrap.DYED_NIXIE_BOARD.contains(state2.getBlock());
        final boolean stateOneIsTube = BnbBlocksBootstrap.LARGE_NIXIE_TUBE.is(state1.getBlock()) || BnbBlocksBootstrap.DYED_LARGE_NIXIE_TUBE.contains(state1.getBlock());
        final boolean stateTwoIsTube = BnbBlocksBootstrap.LARGE_NIXIE_TUBE.is(state2.getBlock()) || BnbBlocksBootstrap.DYED_LARGE_NIXIE_TUBE.contains(state2.getBlock());

        if (!(stateOneIsBoard && stateTwoIsBoard) && !(stateOneIsTube && stateTwoIsTube)) return false;
        if (state1.getValue(GenericNixieDisplayBlock.FACING) != state2.getValue(GenericNixieDisplayBlock.FACING)) {
            return false;
        }
        return state1.getValue(GenericNixieDisplayBlock.ORIENTATION) == state2.getValue(GenericNixieDisplayBlock.ORIENTATION);
    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    protected void write(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.write(tag, registries, clientPacket);

        if (customTextTop.isPresent()) {
            final CompoundTag componentTag = new CompoundTag();
            customTextTop.get()
                    .write(componentTag, registries);
            tag.put("CustomTextComponentTop", componentTag);
        }
        if (customTextBottom.isPresent()) {
            final CompoundTag componentTag = new CompoundTag();
            customTextBottom.get()
                    .write(componentTag, registries);
            tag.put("CustomTextComponentBottom", componentTag);
        }

        tag.putInt("CustomTextIndex", customTextStart);
        tag.putString("CurrentDisplayOption", currentDisplayOption.name());
    }

    @Override
    protected void read(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        if (tag.contains("CustomTextComponentTop")) {
            final DynamicComponent component = customTextTop.orElseGet(DynamicComponent::new);
            final CompoundTag componentTag = tag.getCompound("CustomTextComponentTop");
            component.read(worldPosition, componentTag, registries);
            customTextTop = Optional.of(component);
        }

        if (tag.contains("CustomTextComponentBottom")) {
            final DynamicComponent component = customTextBottom.orElseGet(DynamicComponent::new);
            final CompoundTag componentTag = tag.getCompound("CustomTextComponentBottom");
            component.read(worldPosition, componentTag, registries);
            customTextBottom = Optional.of(component);
        }

        customTextStart = tag.getInt("CustomTextIndex");

        final String optionName = tag.getString("CurrentDisplayOption");
        try {
            currentDisplayOption = ConfigurableDisplayOptions.valueOf(optionName);
        } catch (final IllegalArgumentException e) {
            log.warn("Invalid display option '{}' for block entity at {}. Defaulting to NONE.", optionName, getBlockPos());
            currentDisplayOption = ConfigurableDisplayOptions.NONE;
        }

        //Migration: Check for old tag names
        //TODO: remove migration by release 1.0.1
        if (tag.contains("CurrentTextTop")) {
            log.info("Migrating old Nixie display data for block entity at {}", getBlockPos());
            String oldTextTop = tag.getString("CurrentTextTop");
            if (!oldTextTop.isEmpty()) {
                final EndClipping clipping = getEndClipping();
                final DynamicComponent component = new DynamicComponent();
                ((DynamicComponentMigrator) component).bits_n_bobs$setValueToLiteral(oldTextTop, registries);
                customTextTop = Optional.of(component);
                customTextStart = clipping.left;
            } else {
                customTextTop = Optional.empty();
                customTextStart = 0;
            }
        }

        if (tag.contains("CurrentTextBottom")) {
            log.info("Migrating old Nixie display data for block entity at {}", getBlockPos());
            final String oldTextBottom = tag.getString("CurrentTextBottom");
            if (!oldTextBottom.isEmpty()) {
                final DynamicComponent component = new DynamicComponent();
                ((DynamicComponentMigrator) component).bits_n_bobs$setValueToLiteral(oldTextBottom, registries);
                customTextBottom = Optional.of(component);
                final EndClipping clipping = getEndClipping();
                customTextStart = clipping.left;
            } else {
                customTextBottom = Optional.empty();
                customTextStart = 0;
            }
        }

        if (clientPacket || isVirtual())
            updateDisplayedStrings();
    }

    public @NotNull GenericNixieDisplayBlockEntity findControllerBlockEntity() {
        final Direction facing = getBlockState().getValue(GenericNixieDisplayBlock.FACING);
        final Direction orientation = getBlockState().getValue(GenericNixieDisplayBlock.ORIENTATION);
        final Direction left = DoubleOrientedDirections.getLeft(facing, orientation);
        BlockPos leftPos = getBlockPos().relative(left);
        GenericNixieDisplayBlockEntity lastDisplay = this;
        for (int i = 0; i < 100; i++) {
            final BlockEntity blockEntity = level.getBlockEntity(leftPos);
            if (blockEntity instanceof final GenericNixieDisplayBlockEntity display && areStatesComprableForConnection(getBlockState(), display.getBlockState())) {
                lastDisplay = display;
            } else {
                break; // No more display found
            }
            leftPos = leftPos.relative(left);
        }
        return lastDisplay;
    }

    public void displayCustomText(final String tagElement, final int nixiePositionInRow, final int line) {
        final Optional<DynamicComponent> lineText = line == 0 ? customTextTop : customTextBottom;

        if (tagElement == null)
            return;
        if (lineText.filter(d -> d.sameAs(tagElement))
                .isPresent())
            return;

        final DynamicComponent component = lineText.orElseGet(DynamicComponent::new);
        component.displayCustomText(level, worldPosition, tagElement);
        if (line == 0) {
            customTextTop = Optional.of(component);
        } else {
            customTextBottom = Optional.of(component);
        }
        customTextStart = nixiePositionInRow;
        notifyUpdate();
    }

    public int seekWidth() {
        final Direction right = DoubleOrientedDirections.getLeft(
                getBlockState().getValue(GenericNixieDisplayBlock.FACING),
                getBlockState().getValue(GenericNixieDisplayBlock.ORIENTATION)
        ).getOpposite();
        int characterCount = 0;
        for (int i = 0; i < 100; i++) {
            final BlockPos nextPos = getBlockPos().relative(right, i);
            final BlockEntity blockEntity = level.getBlockEntity(nextPos);
            if (!areStatesComprableForConnection(getBlockState(), level.getBlockState(nextPos))) {
                break;
            }
            if (blockEntity instanceof final GenericNixieDisplayBlockEntity currentWalkNixieDisplay) {
                characterCount += currentWalkNixieDisplay.calculateDisplayedCharacterWidth();
            } else {
                CreateBitsnBobs.LOGGER.warn("Found unexpected non-nixie display block entity at {} while seeking width for {}", nextPos, getBlockPos());
                break;
            }
        }
        return characterCount;
    }

    public void updateDisplayedStrings() {
        customTextTop.map(DynamicComponent::resolve)
                .ifPresentOrElse(
                        fullText -> renderedTextTop = charsOrEmpty(fullText, customTextStart),
                        () -> renderedTextTop = "");
        customTextBottom.map(DynamicComponent::resolve)
                .ifPresentOrElse(
                        fullText -> renderedTextBottom = charsOrEmpty(fullText, customTextStart),
                        () -> renderedTextBottom = "");
    }

    private String charsOrEmpty(final String string, final int index) {
        final EndClipping endClipping = getEndClipping();
        return " ".repeat(endClipping.left) +
                (string.length() <= index ? "   " :
                        (string + " ".repeat(GenericNixieDisplayBlockEntity.MAX_CHARS_IN_BLOCK))
                                .substring(index, index + calculateDisplayedCharacterWidth()));
    }

    int calculateDisplayedCharacterWidth() {
        final EndClipping endClipping = getEndClipping();
        return currentDisplayOption.width - endClipping.left - endClipping.right;
    }

    public enum EndClipping {
        NONE(0, 0),
        LEFT(1, 0),
        RIGHT(0, 1),
        BOTH(1, 1);

        public final int left;
        public final int right;

        EndClipping(final int left, final int right) {
            this.left = left;
            this.right = right;
        }
    }

    public EndClipping getEndClipping() {
        if (!(getBlockState().getBlock() instanceof NixieBoardBlockNixie) || currentDisplayOption != ConfigurableDisplayOptions.DOUBLE_CHAR_DOUBLE_LINES) {
            return EndClipping.NONE; // Nixie tubes do not have end clipping
        }
        final boolean left = !getBlockState().getValue(NixieBoardBlockNixie.LEFT);
        final boolean right = !getBlockState().getValue(NixieBoardBlockNixie.RIGHT);
        if (left && right) {
            return EndClipping.BOTH;
        } else if (left) {
            return EndClipping.LEFT;
        } else if (right) {
            return EndClipping.RIGHT;
        }
        return EndClipping.NONE;
    }

}

