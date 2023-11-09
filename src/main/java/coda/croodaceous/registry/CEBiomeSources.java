package coda.croodaceous.registry;

import com.mojang.serialization.Codec;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.world.biome.biomesource.CroodaceousBiomeSource;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CEBiomeSources {

	public static final DeferredRegister<Codec<? extends BiomeSource>> BIOME_SOURCE = DeferredRegister.create(Registry.BIOME_SOURCE_REGISTRY, CroodaceousMod.MOD_ID);
	
	public static final RegistryObject<Codec<CroodaceousBiomeSource>> CROODACEOUS_BIOME_SOURCE = BIOME_SOURCE.register("croodaceous_biome_source", () -> CroodaceousBiomeSource.CODEC);

}
