package coda.croodaceous.registry;

import com.mojang.serialization.Codec;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.world.chunkgen.CroodaceousChunkGenerator;
import net.minecraft.core.Registry;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CEChunkGenerators {
	public static final DeferredRegister<Codec<? extends ChunkGenerator>> REGISTER = DeferredRegister.create(Registry.CHUNK_GENERATOR_REGISTRY, CroodaceousMod.MOD_ID);
	
	public static final RegistryObject<Codec<CroodaceousChunkGenerator>> CROODACEOUS_CHUNK_GNERATOR = REGISTER.register("croodaceous_chunk_generator", () -> CroodaceousChunkGenerator.CODEC);
	
}
