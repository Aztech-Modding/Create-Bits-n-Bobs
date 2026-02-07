package com.kipti.bnb.foundation.generation;

import com.kipti.bnb.foundation.client.ShipyardHelper;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class PonderLevelSource extends ChunkGenerator {
    public static final MapCodec<PonderLevelSource> CODEC = RecordCodecBuilder.mapCodec(
            p_255576_ -> p_255576_.group(RegistryOps.retrieveElement(Biomes.THE_VOID)).apply(p_255576_, p_255576_.stable(PonderLevelSource::new))
    );

    public PonderLevelSource(final Holder.Reference<Biome> biome) {
        super(new FixedBiomeSource(biome));
    }

    public PonderLevelSource(final BiomeSource biomeSource, final Function<Holder<Biome>, BiomeGenerationSettings> generationSettingsGetter) {
        super(biomeSource, generationSettingsGetter);
    }

    @Override
    protected @NotNull MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void buildSurface(final @NotNull WorldGenRegion level, final @NotNull StructureManager structureManager, final @NotNull RandomState random, final @NotNull ChunkAccess chunk) {
    }

    @Override
    public void applyBiomeDecoration(final @NotNull WorldGenLevel level, final @NotNull ChunkAccess chunk, final @NotNull StructureManager structureManager) {
    }

    @Override
    public @NotNull CompletableFuture<ChunkAccess> fillFromNoise(final @NotNull Blender blender, final @NotNull RandomState randomState, final @NotNull StructureManager structureManager, final @NotNull ChunkAccess chunk) {
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        if (ShipyardHelper.isProbablyInShipyard(chunk.getPos().getWorldPosition()))
            return CompletableFuture.completedFuture(chunk);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = -64; y < (-64 + 16); y++) {
                    final BlockState blockState = getBlockStateFor(x, y, z);
                    pos.set(
                            chunk.getPos().getBlockX(x),
                            y,
                            chunk.getPos().getBlockX(z)
                    );
                    chunk.setBlockState(pos, blockState, false);
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getBaseHeight(final int x, final int z, final Heightmap.@NotNull Types type, final @NotNull LevelHeightAccessor level, final @NotNull RandomState random) {
        return level.getMinBuildHeight() + 16;
    }

    @Override
    public @NotNull NoiseColumn getBaseColumn(final int x, final int z, final @NotNull LevelHeightAccessor height, final @NotNull RandomState random) {
        return new NoiseColumn(height.getMinBuildHeight(), new BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(final @NotNull List<String> info, final @NotNull RandomState random, final @NotNull BlockPos pos) {
    }

    public static BlockState getBlockStateFor(final int chunkX, final int chunkY, final int chunkZ) {
        return ((chunkX + chunkY + chunkZ) % 2 == 0) ? Blocks.SNOW_BLOCK.defaultBlockState() : Blocks.WHITE_CONCRETE.defaultBlockState();
    }

    @Override
    public void applyCarvers(
            final @NotNull WorldGenRegion level,
            final long seed,
            final @NotNull RandomState random,
            final @NotNull BiomeManager biomeManager,
            final @NotNull StructureManager structureManager,
            final @NotNull ChunkAccess chunk,
            final GenerationStep.@NotNull Carving step
    ) {
    }

    @Override
    public void spawnOriginalMobs(final @NotNull WorldGenRegion level) {
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public int getSeaLevel() {
        return -63;
    }

}
