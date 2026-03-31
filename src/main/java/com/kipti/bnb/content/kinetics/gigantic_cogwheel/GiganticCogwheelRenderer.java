package com.kipti.bnb.content.kinetics.gigantic_cogwheel;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.kipti.bnb.registry.client.BnbPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.model.BakedModelHelper;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.StitchedSprite;
import net.createmod.catnip.render.SuperBufferFactory;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.render.SuperByteBufferCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Renderer for the gigantic cogwheel center block entity.
 * Generates wood-type-specific models by replacing template spruce textures
 * with the material's corresponding plank and log sprites.
 */
public class GiganticCogwheelRenderer extends KineticBlockEntityRenderer<GiganticCogwheelBlockEntity> {

	public static final SuperByteBufferCache.Compartment<ModelKey> GIGANTIC_COGWHEEL_CACHE = new SuperByteBufferCache.Compartment<>();

	public static final StitchedSprite SPRUCE_PLANKS_TEMPLATE = new StitchedSprite(ResourceLocation.withDefaultNamespace("block/spruce_planks"));
	public static final StitchedSprite SPRUCE_LOG_TEMPLATE = new StitchedSprite(ResourceLocation.withDefaultNamespace("block/spruce_log"));

	private static final String[] LOG_LOCATIONS = new String[] {
		"x_log", "x_stem", "x_block",
		"wood/log/x"
	};

	public GiganticCogwheelRenderer(Context context) {
		super(context);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(GiganticCogwheelBlockEntity be, BlockState state) {
		ModelKey key = new ModelKey(state, be.material);
		return SuperByteBufferCache.getInstance().get(GIGANTIC_COGWHEEL_CACHE, key, () -> {
			BakedModel model = generateModel(key.material());
			Direction dir = Direction.fromAxisAndDirection(state.getValue(RotatedPillarKineticBlock.AXIS), AxisDirection.POSITIVE);
			PoseStack transform = CachedBuffers.rotateToFaceVertical(dir).get();
			return SuperBufferFactory.getInstance().createForBlock(model, Blocks.AIR.defaultBlockState(), transform);
		});
	}

	public static BakedModel generateModel(BlockState material) {
		return generateModel(BnbPartialModels.GIGANTIC_COGWHEEL.get(), material);
	}

	public static BakedModel generateModel(BakedModel template, BlockState planksBlockState) {
		String wood = plankStateToWoodName(planksBlockState);

		if (wood == null)
			return BakedModelHelper.generateModel(template, sprite -> null);

		ResourceLocation id = RegisteredObjectsHelper.getKeyOrThrow(planksBlockState.getBlock());
		BlockState logBlockState = getLogBlockState(id.getNamespace(), wood);

		Map<TextureAtlasSprite, TextureAtlasSprite> map = new Reference2ReferenceOpenHashMap<>();
		map.put(SPRUCE_PLANKS_TEMPLATE.get(), getSpriteOnSide(planksBlockState, Direction.UP));
		map.put(SPRUCE_LOG_TEMPLATE.get(), getSpriteOnSide(logBlockState, Direction.SOUTH));

		return BakedModelHelper.generateModel(template, map::get);
	}

	@Nullable
	private static String plankStateToWoodName(BlockState planksBlockState) {
		ResourceLocation id = RegisteredObjectsHelper.getKeyOrThrow(planksBlockState.getBlock());
		String path = id.getPath();

		if (path.endsWith("_planks"))
			return (path.startsWith("archwood") ? "blue_" : "") + path.substring(0, path.length() - 7);

		if (path.contains("wood/planks/"))
			return path.substring(12);

		return null;
	}

	private static BlockState getLogBlockState(String namespace, String wood) {
		for (String location : LOG_LOCATIONS) {
			Optional<BlockState> state =
				BuiltInRegistries.BLOCK.getHolder(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(namespace, location.replace("x", wood))))
					.map(Holder::value)
					.map(Block::defaultBlockState);
			if (state.isPresent())
				return state.get();
		}
		return Blocks.OAK_LOG.defaultBlockState();
	}

	private static TextureAtlasSprite getSpriteOnSide(BlockState state, Direction side) {
		BakedModel model = Minecraft.getInstance()
			.getBlockRenderer()
			.getBlockModel(state);
		if (model == null)
			return null;
		RandomSource random = RandomSource.create();
		random.setSeed(42L);
		List<BakedQuad> quads = model.getQuads(state, side, random, ModelData.EMPTY, null);
		if (!quads.isEmpty())
			return quads.get(0).getSprite();
		random.setSeed(42L);
		quads = model.getQuads(state, null, random, ModelData.EMPTY, null);
		for (BakedQuad quad : quads) {
			if (quad.getDirection() == side)
				return quad.getSprite();
		}
		return model.getParticleIcon(ModelData.EMPTY);
	}

	public record ModelKey(BlockState state, BlockState material) {}
}
