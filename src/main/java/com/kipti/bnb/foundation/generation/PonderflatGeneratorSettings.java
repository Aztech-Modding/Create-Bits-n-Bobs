package com.kipti.bnb.foundation.generation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public record PonderflatGeneratorSettings(int cellSize, Block blockLight, Block blockDark, CellStyle cellStyle) {

    public static final Codec<PonderflatGeneratorSettings> CODEC = RecordCodecBuilder.<PonderflatGeneratorSettings>create(
                    p_209800_ -> p_209800_.group(
                                    Codec.intRange(1, 16)
                                            .fieldOf("cell_size")
                                            .orElse(1)
                                            .forGetter(PonderflatGeneratorSettings::cellSize),
                                    BuiltInRegistries.BLOCK.byNameCodec()
                                            .fieldOf("block_light")
                                            .orElse(Blocks.SNOW_BLOCK)
                                            .forGetter(PonderflatGeneratorSettings::blockLight),
                                    BuiltInRegistries.BLOCK.byNameCodec()
                                            .fieldOf("block_dark")
                                            .orElse(Blocks.WHITE_CONCRETE)
                                            .forGetter(PonderflatGeneratorSettings::blockDark),
                                    StringRepresentable.fromEnum(CellStyle::values)
                                            .fieldOf("cell_style")
                                            .orElse(CellStyle.BORDERED)
                                            .forGetter(PonderflatGeneratorSettings::cellStyle)
                            )
                            .apply(p_209800_, PonderflatGeneratorSettings::new)
            )
            .stable();

    public enum CellStyle implements StringRepresentable {
        BORDERED,
        FLAT,
        RINGS;

        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public PonderflatGeneratorSettings() {
        this(1, Blocks.SNOW_BLOCK, Blocks.WHITE_CONCRETE, CellStyle.BORDERED);
    }

    public BlockState getBlockState(final int x, final int y, final int z) {
        if (this.cellSize == 1)
            return this.getBlockStateSimple(x, y, z);
        else
            return switch (this.cellStyle) {
                case RINGS -> this.getRingStyleCell(x, y, z);
                case BORDERED -> this.getBorderedStyleCell(x, y, z);
                default -> this.getBlockStateSimple(x, y, z);
            };
    }

    private BlockState getRingStyleCell(final int x, final int y, final int z) {
        final boolean odd = (Math.floorDiv(x, this.cellSize) + y + Math.floorDiv(z, this.cellSize)) % 2 == 0;

        final double xToCellCenter = Math.abs(((this.cellSize - 1) / 2.0) - this.periodMod(x, this.cellSize));
        final double zToCellCenter = Math.abs(((this.cellSize - 1) / 2.0) - this.periodMod(z, this.cellSize));

        final int factor = (int) Math.max(xToCellCenter, zToCellCenter);
        return (factor % 2 == 0) ^ odd ? this.blockLight.defaultBlockState() : this.blockDark.defaultBlockState();
    }

    private BlockState getBorderedStyleCell(final int x, final int y, final int z) {
        final boolean odd = (Math.floorDiv(x, this.cellSize) + y + Math.floorDiv(z, this.cellSize)) % 2 == 0;

        final double radius = (this.cellSize - 1) / 2.0;
        final double xToCellCenter = Math.abs(radius - this.periodMod(x, this.cellSize));
        final double zToCellCenter = Math.abs(radius - this.periodMod(z, this.cellSize));

        final boolean border = Math.max(xToCellCenter, zToCellCenter) >= radius - (radius <= 5 ? 0.5 : 1);
        return border ^ odd ? this.blockLight.defaultBlockState() : this.blockDark.defaultBlockState();
    }

    private double periodMod(final int i, final int period) {
        return (period + (i % period)) % period;
    }

    private BlockState getBlockStateSimple(final int x, final int y, final int z) {
        return ((Math.floorDiv(x, this.cellSize) + y + Math.floorDiv(z, this.cellSize)) % 2 == 0) ? this.blockLight.defaultBlockState() : this.blockDark.defaultBlockState();
    }
}

