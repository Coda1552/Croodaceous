package coda.croodaceous.common.world.biome.biomesource;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import coda.croodaceous.common.util.FastNoise;
import coda.croodaceous.common.world.chunkgen.CroodaceousChunkGenerator;
import coda.croodaceous.registry.CEBiomes;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager.NoiseBiomeSource;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate.Sampler;

public class CroodaceousBiomeSource extends BiomeSource implements NoiseBiomeSource {
	
	public static final Codec<CroodaceousBiomeSource> CODEC = RecordCodecBuilder.create((source) -> {
		return source.group(RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter((biomeGetter) -> {
			return null;
		})).apply(source, source.stable(CroodaceousBiomeSource::new));
	});
	
	private final Holder<Biome> jungle, mangrove_swamp, river, sparse_jungle, rocky_ridge, desolate_desert, nesting_grounds;
	
	public CroodaceousBiomeSource(Registry<Biome> biome) {
		this(biome.getOrCreateHolderOrThrow(Biomes.JUNGLE), biome.getOrCreateHolderOrThrow(Biomes.MANGROVE_SWAMP), biome.getOrCreateHolderOrThrow(Biomes.RIVER), biome.getOrCreateHolderOrThrow(Biomes.SPARSE_JUNGLE), biome.getOrCreateHolderOrThrow(CEBiomes.ROCKY_RIDGE.getKey()), biome.getOrCreateHolderOrThrow(CEBiomes.DESOLATE_DESERT.getKey()), biome.getOrCreateHolderOrThrow(CEBiomes.NESTING_GROUNDS.getKey()));
	}
	
	public CroodaceousBiomeSource(Holder<Biome> jungle, Holder<Biome> mangroveSwamp, Holder<Biome> river, Holder<Biome> sparseJungle, Holder<Biome> rockyRidge, Holder<Biome> desolateDesert, Holder<Biome> nestingGrounds) {
		super(ImmutableList.of(jungle, mangroveSwamp, river, sparseJungle, rockyRidge, desolateDesert, nestingGrounds));
		this.jungle = jungle;
		this.mangrove_swamp = mangroveSwamp;
		this.river = river;
		this.sparse_jungle = sparseJungle;
		this.rocky_ridge = rockyRidge;
		this.desolate_desert = desolateDesert;
		this.nesting_grounds = nestingGrounds;
	}
	
	@Override
	protected Codec<? extends BiomeSource> codec() {
		return CODEC;
	}
	
	public double calculateNoiseValue(int x, int z, FastNoise noise) {
		float frequency = 0.5F;
		float amplitude = 4.0F;

		double noiseX = noise.GetNoise(x * frequency, z * frequency);
		double noiseZ = noise.GetNoise(x * frequency, z * frequency);

		double combinedNoise = amplitude * (noiseX + noiseZ);

		return combinedNoise;
	}

	@Override
	public Holder<Biome> getNoiseBiome(int x, int y, int z) {
		double noiseValue = calculateNoiseValue(x, z, CroodaceousChunkGenerator.noise);
		if (noiseValue > -1 && noiseValue <= 1) {
			return jungle;
		} else if (noiseValue > -2 && noiseValue <= -1 || noiseValue < 2 && noiseValue >= 1) {
			return sparse_jungle;
		} else if (noiseValue < 6 && noiseValue >= 4.5) {
			return mangrove_swamp;
		} else if (noiseValue < 4.5 && noiseValue >= 3) {
			return nesting_grounds;
		} else if (noiseValue < -4 && noiseValue >= -5.5) {
			return desolate_desert;
		} else if (noiseValue < -5.5 && noiseValue >= -6.5 || noiseValue < -3 && noiseValue >= -4) {
			return rocky_ridge;
		} else return river;
	}

	@Override
	public Holder<Biome> getNoiseBiome(int x, int y, int z, Sampler pSampler) {
		return getNoiseBiome(x, y, z);
	}
	
	

}
