package com.kipti.bnb.content.kinetics.cogwheel_chain.types;

import com.kipti.bnb.registry.BnbRegistries;
import com.kipti.bnb.registry.BnbResourceKeys;
import com.simibubi.create.api.registry.SimpleRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class CogwheelChainType {

    public static final StreamCodec<RegistryFriendlyByteBuf, CogwheelChainType> STEAM_CODEC = ByteBufCodecs.registry(BnbResourceKeys.COGWHEEL_CHAIN_TYPE);

    public static final SimpleRegistry<Item, CogwheelChainType> COGWHEEL_TYPE_BY_ITEM = SimpleRegistry.create();

    static {
        COGWHEEL_TYPE_BY_ITEM
                .registerProvider((item) -> BnbRegistries.COGWHEEL_CHAIN_TYPES
                        .holders()
                        .filter(typeHolder -> typeHolder.value().isRelatedItem(item))
                        .findFirst()
                        .map(Holder.Reference::value)
                        .orElse(null));
    }

    public static final ResourceLocation DEFAULT_CHAIN_TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/block/chain.png");

    public enum VertexShape {
        CROSS,
        SQUARE
    }

    //Todo: custom render types / just make this not an enum
    public enum ChainRenderInfo {
        CHAIN(VertexShape.CROSS, 3, 3),
        ROPE(VertexShape.SQUARE, 3, 3),
        BELT(VertexShape.SQUARE, 3, 2),
        ;

        private final VertexShape vertexShape;
        private final int width;
        private final int height;

        ChainRenderInfo(final VertexShape vertexShape, final int width, final int height) {
            this.vertexShape = vertexShape;
            this.width = width;
            this.height = height;
        }

        public VertexShape getVertexShape() {
            return vertexShape;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public boolean isDefaultDimensions() {
            return width == 3 && height == 3;
        }
    }

    private final double costFactor;
    private final ChainRenderInfo chainRenderInfo;
    private final ResourceLocation renderTexture;
    private final Predicate<Item> relatedItem;
    private final Predicate<Block> cogwheelPredicate;
    private final boolean permitsAxisChange;

    public CogwheelChainType(final float costFactor,
                             final ChainRenderInfo chainRenderInfo,
                             final ResourceLocation renderTexture,
                             final Predicate<Item> relatedItem,
                             final Predicate<Block> cogwheelPredicate,
                             final boolean permitsAxisChange) {
        this.costFactor = costFactor;
        this.chainRenderInfo = chainRenderInfo;
        this.renderTexture = renderTexture;
        this.relatedItem = relatedItem;
        this.cogwheelPredicate = cogwheelPredicate;
        this.permitsAxisChange = permitsAxisChange;
    }

    public boolean alwaysCostsOneItem() {
        return this.costFactor == 0.0f;
    }

    public static class Builder {
        private float costFactor = 1.0f;
        private ChainRenderInfo chainRenderInfo = ChainRenderInfo.CHAIN;
        private ResourceLocation renderTexture = DEFAULT_CHAIN_TEXTURE_LOCATION;
        private Predicate<Item> relatedItem = (item) -> item == Items.CHAIN;
        private Predicate<Block> cogwheelPredicate = (block) -> true;
        private boolean permitsAxisChange = true;

        public Builder costFactor(final float costFactor) {
            this.costFactor = costFactor;
            return this;
        }

        public Builder renderType(final ChainRenderInfo chainRenderInfo) {
            this.chainRenderInfo = chainRenderInfo;
            return this;
        }

        public Builder renderTexture(final ResourceLocation renderTexture) {
            this.renderTexture = renderTexture;
            return this;
        }

        public Builder relatedItem(final Supplier<Item> relatedItemSupplier) {
            this.relatedItem = (item) -> item == relatedItemSupplier.get();
            return this;
        }

        public Builder relatedTag(final TagKey<Item> itemTag) {
            this.relatedItem = (item) -> BuiltInRegistries.ITEM
                    .getHolder(BuiltInRegistries.ITEM.getKey(item))
                    .map(itemReference -> itemReference.is(itemTag))
                    .orElse(false);
            return this;
        }

        public Builder setCogwheelPredicate(final Predicate<Block> predicate) {
            this.cogwheelPredicate = predicate;
            return this;
        }

        public Builder permitsAxisChange(final boolean permitsAxisChange) {
            this.permitsAxisChange = permitsAxisChange;
            return this;
        }

        public CogwheelChainType build() {
            return new CogwheelChainType(costFactor, chainRenderInfo, renderTexture, relatedItem, cogwheelPredicate, permitsAxisChange);
        }
    }

    public ResourceLocation getKey() {
        final ResourceLocation key = BnbRegistries.COGWHEEL_CHAIN_TYPES.getKey(this);
        return key == null ? BnbCogwheelChainTypes.CHAIN.getKey().location() : key;
    }

    public String getTranslationKey() {
        final ResourceLocation key = getKey();
        return "cogwheel_chain_type." + key.getNamespace() + "." + key.getPath().replace("/", ".");
    }

    public double getCostFactor() {
        return costFactor;
    } //TODO: implement

    public ChainRenderInfo getRenderType() {
        return chainRenderInfo;
    }

    public ResourceLocation getRenderTexture() {
        return renderTexture;
    }

    private boolean isRelatedItem(final Item item) {
        return relatedItem.test(item);
    }

    public Predicate<Block> getCogwheelPredicate() {
        return cogwheelPredicate;
    }

    public boolean permitsAxisChanges() {
        return permitsAxisChange;
    }

}
