package com.kipti.bnb.foundation;

import com.kipti.bnb.CreateBitsnBobs;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Repurposed from {@link com.simibubi.create.foundation.block.DyedBlockList}
 */
public class EncasedBlockList<T extends Block> implements Iterable<BlockEntry<T>> {

    public enum CasingMaterial {
        ANDESITE("andesite", AllBlocks.ANDESITE_CASING, "andesite_casing", "gearbox", AllSpriteShifts.ANDESITE_CASING, true),
        BRASS("brass", AllBlocks.BRASS_CASING, "brass_casing", "brass_gearbox", AllSpriteShifts.BRASS_CASING, true),
        INDUSTRIAL_IRON("industrial_iron", AllBlocks.INDUSTRIAL_IRON_BLOCK, "industrial_iron_block", "industrial_iron_gearbox"),
        WEATHERED_IRON("weathered_iron", AllBlocks.WEATHERED_IRON_BLOCK, "weathered_iron_block", "weathered_iron_gearbox"),
        ;

        private final String resourceName;
        private final Supplier<? extends Block> material;

        private final ResourceLocation surfaceTexture;
        private final ResourceLocation gearboxTexture;

        @Nullable
        public final CTSpriteShiftEntry connectedTextureShift;

        CasingMaterial(final String resourceName, final Supplier<? extends Block> material, final String surfaceTexture, final String gearboxTexture) {
            this(resourceName, material, surfaceTexture, gearboxTexture, null, false);
        }

        CasingMaterial(final String resourceName, final Supplier<? extends Block> material, final String surfaceTexture, final String gearboxTexture, final @Nullable CTSpriteShiftEntry connectedTextureShift, final boolean isCreateNamespace) {
            this.resourceName = resourceName;
            this.material = material;
            this.surfaceTexture = isCreateNamespace ? Create.asResource("block/" + surfaceTexture) : CreateBitsnBobs.asResource("block/" + surfaceTexture);
            this.gearboxTexture = isCreateNamespace ? Create.asResource("block/" + gearboxTexture) : CreateBitsnBobs.asResource("block/" + gearboxTexture);
            this.connectedTextureShift = connectedTextureShift;
        }

        public String getResourceName() {
            return resourceName;
        }


        public String asId(final String blockId) {
            return name().toLowerCase() + "_" + blockId;
        }

        public Supplier<Block> getMaterial() {
            return () -> (Block) material.get();
        }

        public ResourceLocation getSurfaceTexture() {
            return surfaceTexture;
        }

        public ResourceLocation getGearboxTexture() {
            return gearboxTexture;
        }

        public <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> withCT(final BiConsumer<BlockBuilder<B, P>, CTSpriteShiftEntry> transformer) {
            return builder -> {
                if (this.connectedTextureShift != null) {
                    transformer.accept(builder, this.connectedTextureShift);
                }
                return builder;
            };

        }

    }

    private final BlockEntry<?>[] values = new BlockEntry<?>[CasingMaterial.values().length];

    public EncasedBlockList(final Function<CasingMaterial, BlockEntry<? extends T>> filler) {
        for (final CasingMaterial casing : CasingMaterial.values()) {
            values[casing.ordinal()] = filler.apply(casing);
        }
    }

    @SuppressWarnings("unchecked")
    public BlockEntry<T> get(final CasingMaterial material) {
        return (BlockEntry<T>) values[material.ordinal()];
    }

    public boolean contains(final Block block) {
        for (final BlockEntry<?> entry : values) {
            if (entry.is(block)) {
                return true;
            }
        }
        return false;
    }

    public boolean isIn(final BlockState state) {
        for (final BlockEntry<?> entry : values) {
            if (state.is(entry)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public BlockEntry<T>[] toArray() {
        return (BlockEntry<T>[]) Arrays.copyOf(values, values.length);
    }

    @Override
    public Iterator<BlockEntry<T>> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < values.length;
            }

            @SuppressWarnings("unchecked")
            @Override
            public BlockEntry<T> next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                return (BlockEntry<T>) values[index++];
            }
        };
    }

}
