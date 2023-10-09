package coda.croodaceous.common.world.biome.surfacedecorators;

import java.util.HashMap;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.util.FastNoise;
import coda.croodaceous.registry.CEBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

public class SurfaceDecorators {

	public static final HashMap<ResourceLocation, SurfaceDecorator> BIOME_TO_SURFACE_DECORATOR = new HashMap<>();
	private static final SurfaceDecorator DEFAULT_DECORATOR = new DefaultSurfaceDecorator();
	static FastNoise noise;
	
	static {
		register (new ResourceLocation(CroodaceousMod.MOD_ID, "desolate_desert"),
				new NoiseBasedSurfaceDecorator(CEBlocks.DESOLATE_SAND.get().defaultBlockState(), CEBlocks.DESOLATE_SANDSTONE.get().defaultBlockState(), CEBlocks.DESOLATE_SAND.get().defaultBlockState(), Blocks.TERRACOTTA.defaultBlockState(), Blocks.TERRACOTTA.defaultBlockState(), Blocks.TERRACOTTA.defaultBlockState(), CEBlocks.DESOLATE_SAND.get().defaultBlockState(), CEBlocks.DESOLATE_SANDSTONE.get().defaultBlockState(), CEBlocks.DESOLATE_SAND.get().defaultBlockState(), -0.2D, 0.4D, 4, noise));
		register (new ResourceLocation(CroodaceousMod.MOD_ID, "nesting_grounds"),
				new NoiseBasedSurfaceDecorator(Blocks.ORANGE_TERRACOTTA.defaultBlockState(), Blocks.ORANGE_TERRACOTTA.defaultBlockState(), Blocks.ORANGE_TERRACOTTA.defaultBlockState(), Blocks.TERRACOTTA.defaultBlockState(), Blocks.TERRACOTTA.defaultBlockState(), Blocks.TERRACOTTA.defaultBlockState(), CEBlocks.DESOLATE_SAND.get().defaultBlockState(), CEBlocks.DESOLATE_SANDSTONE.get().defaultBlockState(), CEBlocks.DESOLATE_SAND.get().defaultBlockState(), -0.4D, 0.1D, 4, noise));
		register (new ResourceLocation(CroodaceousMod.MOD_ID, "rocky_ridge"),
				new NoiseBasedSurfaceDecorator(CEBlocks.DESOLATE_SAND.get().defaultBlockState(), CEBlocks.DESOLATE_SANDSTONE.get().defaultBlockState(), CEBlocks.DESOLATE_SAND.get().defaultBlockState(), CEBlocks.DESOLATE_SANDSTONE.get().defaultBlockState(), CEBlocks.DESOLATE_SANDSTONE.get().defaultBlockState(), CEBlocks.DESOLATE_SAND.get().defaultBlockState(), Blocks.BLACK_WOOL.defaultBlockState(), Blocks.BLACK_WOOL.defaultBlockState(), Blocks.BLUE_WOOL.defaultBlockState(), -0.3D, 1, 4, noise));
		register (new ResourceLocation("sparse_jungle"),
				new BasicSurfaceDecorator(Blocks.GRASS_BLOCK.defaultBlockState(), Blocks.DIRT.defaultBlockState(), Blocks.DIRT.defaultBlockState(), 4));
	}
	
	public static void setFastNoise(FastNoise noise) {
		SurfaceDecorators.noise = noise;
	}
	
	public static void register(ResourceLocation biome, SurfaceDecorator decorator) {
		BIOME_TO_SURFACE_DECORATOR.put(biome, decorator);
	}
	
	public static SurfaceDecorator getSurfaceDecorator(ResourceLocation biomeLocation) {
		return BIOME_TO_SURFACE_DECORATOR.getOrDefault(biomeLocation, DEFAULT_DECORATOR);
	}
	
}
