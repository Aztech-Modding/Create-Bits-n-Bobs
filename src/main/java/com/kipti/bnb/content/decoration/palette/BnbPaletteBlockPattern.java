package com.kipti.bnb.content.decoration.palette;

import com.kipti.bnb.CreateBitsnBobs;
import com.simibubi.create.content.decoration.palettes.ConnectedPillarBlock;
import com.simibubi.create.foundation.block.connected.*;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * thank you for your mit licence, this code is mine now >:)
 */
public class BnbPaletteBlockPattern {

    public static final BnbPaletteBlockPattern
            TILES = create("tiles", BnbPatternNameType.SUFFIX, BnbPaletteBlockPartial.ALL_PARTIALS_TILE)
            .blockStateFactory(p -> p::cubeTile);

    public enum BnbPatternNameType {
        PREFIX, SUFFIX, WRAP
    }

    public static final BnbPaletteBlockPattern[] ADDITIONS_TO_BASE = {TILES};

    static final String TEXTURE_LOCATION = "block/palettes/stone_types/%s/%s";

    private BnbPatternNameType nameType;
    private String[] textures;
    private String id;
    private boolean isTranslucent;
    private TagKey<Block>[] blockTags;
    private TagKey<Item>[] itemTags;
    private Optional<Function<String, ConnectedTextureBehaviour>> ctFactory;

    private IPatternBlockStateGenerator blockStateGenerator;
    private NonNullFunction<BlockBehaviour.Properties, ? extends Block> blockFactory;
    private NonNullFunction<NonNullSupplier<Block>, NonNullBiConsumer<DataGenContext<Block, ? extends Block>, RegistrateRecipeProvider>> additionalRecipes;
    private BnbPaletteBlockPartial<? extends Block>[] partials;

    @OnlyIn(Dist.CLIENT)
    private RenderType renderType;

    private static BnbPaletteBlockPattern create(final String name, final BnbPatternNameType nameType,
                                                 final BnbPaletteBlockPartial<?>... partials) {
        final BnbPaletteBlockPattern pattern = new BnbPaletteBlockPattern();
        pattern.id = name;
        pattern.ctFactory = Optional.empty();
        pattern.nameType = nameType;
        pattern.partials = partials;
        pattern.additionalRecipes = $ -> NonNullBiConsumer.noop();
        pattern.isTranslucent = false;
        pattern.blockFactory = Block::new;
        pattern.textures = new String[]{name};
        pattern.blockStateGenerator = p -> p::cubeAll;
        return pattern;
    }

    public IPatternBlockStateGenerator getBlockStateGenerator() {
        return blockStateGenerator;
    }

    public boolean isTranslucent() {
        return isTranslucent;
    }

    public TagKey<Block>[] getBlockTags() {
        return blockTags;
    }

    public TagKey<Item>[] getItemTags() {
        return itemTags;
    }

    public NonNullFunction<BlockBehaviour.Properties, ? extends Block> getBlockFactory() {
        return blockFactory;
    }

    public BnbPaletteBlockPartial<? extends Block>[] getPartials() {
        return partials;
    }

    public String getTexture(final int index) {
        return textures[index];
    }

    public void addRecipes(final NonNullSupplier<Block> baseBlock, final DataGenContext<Block, ? extends Block> c,
                           final RegistrateRecipeProvider p) {
        additionalRecipes.apply(baseBlock)
                .accept(c, p);
    }

    public Optional<Supplier<ConnectedTextureBehaviour>> createCTBehaviour(final String variant) {
        return ctFactory.map(d -> () -> d.apply(variant));
    }

    // Builder

    private BnbPaletteBlockPattern blockStateFactory(final IPatternBlockStateGenerator factory) {
        blockStateGenerator = factory;
        return this;
    }

    private BnbPaletteBlockPattern textures(final String... textures) {
        this.textures = textures;
        return this;
    }

    private BnbPaletteBlockPattern block(final NonNullFunction<BlockBehaviour.Properties, ? extends Block> blockFactory) {
        this.blockFactory = blockFactory;
        return this;
    }

    private BnbPaletteBlockPattern connectedTextures(final Function<String, ConnectedTextureBehaviour> factory) {
        this.ctFactory = Optional.of(factory);
        return this;
    }

    // Model generators

    public IBlockStateProvider cubeAll(final String variant) {
        final ResourceLocation all = toLocation(variant, textures[0]);
        return (ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models()
                .cubeAll(createName(variant), all));
    }

    public IBlockStateProvider cubeTile(final String variant) {
        final ResourceLocation all = toLocation(variant, textures[0]);
        final ResourceLocation flipped = toLocation(variant, textures[0] + "_flipped");
        return (ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models()
                .cube(createName(variant), all, all, flipped, flipped, all, all)
                .texture("particle", all));
    }

    public IBlockStateProvider cubeBottomTop(final String variant) {
        final ResourceLocation side = toLocation(variant, textures[0]);
        final ResourceLocation bottom = toLocation(variant, textures[1]);
        final ResourceLocation top = toLocation(variant, textures[2]);
        return (ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models()
                .cubeBottomTop(createName(variant), side, bottom, top));
    }

    public IBlockStateProvider pillar(final String variant) {
        final ResourceLocation side = toLocation(variant, textures[0]);
        final ResourceLocation end = toLocation(variant, textures[1]);

        return (ctx, prov) -> prov.getVariantBuilder(ctx.getEntry())
                .forAllStatesExcept(state -> {
                            final Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
                            if (axis == Direction.Axis.Y)
                                return ConfiguredModel.builder()
                                        .modelFile(prov.models()
                                                .cubeColumn(createName(variant), side, end))
                                        .uvLock(false)
                                        .build();
                            return ConfiguredModel.builder()
                                    .modelFile(prov.models()
                                            .cubeColumnHorizontal(createName(variant) + "_horizontal", side, end))
                                    .uvLock(false)
                                    .rotationX(90)
                                    .rotationY(axis == Direction.Axis.X ? 90 : 0)
                                    .build();
                        }, BlockStateProperties.WATERLOGGED, ConnectedPillarBlock.NORTH, ConnectedPillarBlock.SOUTH,
                        ConnectedPillarBlock.EAST, ConnectedPillarBlock.WEST);
    }

    public IBlockStateProvider cubeColumn(final String variant) {
        final ResourceLocation side = toLocation(variant, textures[0]);
        final ResourceLocation end = toLocation(variant, textures[1]);
        return (ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models()
                .cubeColumn(createName(variant), side, end));
    }

    // Utility

    protected String createName(final String variant) {
        if (nameType == BnbPatternNameType.WRAP) {
            final String[] split = id.split("_");
            if (split.length == 2) {
                final String formatString = "%s_%s_%s";
                return String.format(formatString, split[0], variant, split[1]);
            }
        }
        final String formatString = "%s_%s";
        return nameType == BnbPatternNameType.SUFFIX ? String.format(formatString, variant, id) : String.format(formatString, id, variant);
    }

    protected static ResourceLocation toLocation(final String variant, final String texture) {
        return CreateBitsnBobs.asResource(
                String.format(TEXTURE_LOCATION, texture, variant + (texture.equals("cut") ? "_" : "_cut_") + texture));
    }

    protected static CTSpriteShiftEntry ct(final String variant, final CTs texture) {
        final ResourceLocation resLoc = texture.srcFactory.apply(variant);
        final ResourceLocation resLocTarget = texture.targetFactory.apply(variant);
        return CTSpriteShifter.getCT(texture.type, resLoc,
                ResourceLocation.fromNamespaceAndPath(resLocTarget.getNamespace(), resLocTarget.getPath() + "_connected"));
    }

    @FunctionalInterface
    public static interface IPatternBlockStateGenerator
            extends Function<BnbPaletteBlockPattern, Function<String, IBlockStateProvider>> {
    }

    @FunctionalInterface
    public static interface IBlockStateProvider
            extends NonNullBiConsumer<DataGenContext<Block, ? extends Block>, RegistrateBlockstateProvider> {
    }

    enum PatternNameType {
        PREFIX, SUFFIX, WRAP
    }

    // Textures with connectability, used by Spriteshifter

    public enum CTs {

        PILLAR(AllCTTypes.RECTANGLE, s -> toLocation(s, "pillar")),
        CAP(AllCTTypes.OMNIDIRECTIONAL, s -> toLocation(s, "cap")),
        LAYERED(AllCTTypes.HORIZONTAL_KRYPPERS, s -> toLocation(s, "layered"));

        public CTType type;
        private Function<String, ResourceLocation> srcFactory;
        private Function<String, ResourceLocation> targetFactory;

        private CTs(final CTType type, final Function<String, ResourceLocation> factory) {
            this(type, factory, factory);
        }

        private CTs(final CTType type, final Function<String, ResourceLocation> srcFactory,
                    final Function<String, ResourceLocation> targetFactory) {
            this.type = type;
            this.srcFactory = srcFactory;
            this.targetFactory = targetFactory;
        }

    }
}
