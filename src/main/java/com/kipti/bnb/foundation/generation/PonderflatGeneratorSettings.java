package com.kipti.bnb.foundation.generation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PonderflatGeneratorSettings {

    public static final Codec<PonderflatGeneratorSettings> CODEC = RecordCodecBuilder.<PonderflatGeneratorSettings>create(
                    p_209800_ -> p_209800_.group(
                                    Codec.intRange(1, 16)
                                            .fieldOf("cell_size")
                                            .orElse(1)
                                            .forGetter(PonderflatGeneratorSettings::getCellSize),
                                    BuiltInRegistries.BLOCK.byNameCodec()
                                            .fieldOf("block_light")
                                            .orElse(Blocks.SNOW_BLOCK)
                                            .forGetter(PonderflatGeneratorSettings::getBlockLight),
                                    BuiltInRegistries.BLOCK.byNameCodec()
                                            .fieldOf("block_dark")
                                            .orElse(Blocks.WHITE_CONCRETE)
                                            .forGetter(PonderflatGeneratorSettings::getBlockDark),
                                    StringRepresentable.fromEnum(CellStyle::values)
                                            .fieldOf("cell_style")
                                            .orElse(CellStyle.BORDERED)
                                            .forGetter(PonderflatGeneratorSettings::getCellStyle)
                            )
                            .apply(p_209800_, PonderflatGeneratorSettings::new)
            )
            .stable();

    private final int cellSize;

    private final Block blockLight;
    private final Block blockDark;

    private final CellStyle cellStyle;

    public enum CellStyle implements StringRepresentable {
        BORDERED,
        FLAT,
        RINGS;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase();
        }
    }

    public PonderflatGeneratorSettings(final int cellSize, final Block blockLight, final Block blockDark, final CellStyle cellStyle) {
        this.cellSize = cellSize;
        this.blockLight = blockLight;
        this.blockDark = blockDark;
        this.cellStyle = cellStyle;
    }

    public PonderflatGeneratorSettings() {
        this.cellSize = 1;
        this.blockLight = Blocks.SNOW_BLOCK;
        this.blockDark = Blocks.WHITE_CONCRETE;
        this.cellStyle = CellStyle.BORDERED;
    }

    public Block getBlockLight() {
        return blockLight;
    }

    public Block getBlockDark() {
        return blockDark;
    }

    public int getCellSize() {
        return cellSize;
    }

    public CellStyle getCellStyle() {
        return cellStyle;
    }

    public BlockState getBlockState(final int x, final int y, final int z) {
        if (cellSize == 1)
            return getBlockStateSimple(x, y, z);
        else
            return switch (cellStyle) {
                case RINGS -> getRingStyleCell(x, y, z);
                case BORDERED -> getBorderedStyleCell(x, y, z);
                default -> getBlockStateSimple(x, y, z);
            };
    }

    private BlockState getRingStyleCell(final int x, final int y, final int z) {
        final boolean odd = (Math.floorDiv(x, cellSize) + y + Math.floorDiv(z, cellSize)) % 2 == 0;

        final double xToCellCenter = Math.abs(((cellSize - 1) / 2.0) - periodMod(x, cellSize));
        final double zToCellCenter = Math.abs(((cellSize - 1) / 2.0) - periodMod(z, cellSize));

        final int factor = (int) Math.max(xToCellCenter, zToCellCenter);
        return (factor % 2 == 0) ^ odd ? blockLight.defaultBlockState() : blockDark.defaultBlockState();
    }

    private BlockState getBorderedStyleCell(final int x, final int y, final int z) {
        final boolean odd = (Math.floorDiv(x, cellSize) + y + Math.floorDiv(z, cellSize)) % 2 == 0;

        final double radius = (cellSize - 1) / 2.0;
        final double xToCellCenter = Math.abs(radius - periodMod(x, cellSize));
        final double zToCellCenter = Math.abs(radius - periodMod(z, cellSize));

        final boolean border = Math.max(xToCellCenter, zToCellCenter) >= radius - (radius <= 5 ? 0.5 : 1);
        return border ^ odd ? blockLight.defaultBlockState() : blockDark.defaultBlockState();
    }

    private double periodMod(final int i, final int period) {
        return (period + (i % period)) % period;
    }

    private BlockState getBlockStateSimple(final int x, final int y, final int z) {
        return ((Math.floorDiv(x, cellSize) + y + Math.floorDiv(z, cellSize)) % 2 == 0) ? blockLight.defaultBlockState() : blockDark.defaultBlockState();
    }
}

