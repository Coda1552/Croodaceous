package coda.croodaceous.registry;

import coda.croodaceous.CroodaceousMod;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CEBiomes {

	public static final DeferredRegister<Biome> BIOME = DeferredRegister.create(ForgeRegistries.BIOMES, CroodaceousMod.MOD_ID);

	public static RegistryObject<Biome> ROCKY_RIDGE = BIOME.register("rocky_ridge", CEBiomes::rockyRidge);
	public static RegistryObject<Biome> DESOLATE_DESERT = BIOME.register("desolate_desert", CEBiomes::desolateDesert);
	public static RegistryObject<Biome> NESTING_GROUNDS = BIOME.register("nesting_grounds", CEBiomes::nestingGrounds);

	public static Biome rockyRidge() {
		MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
		BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder();
		return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).temperature(0.8F).downfall(0.4F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(16696674).skyColor(16696674).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
	}

	public static Biome desolateDesert() {
		MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
		BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder();
		return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).temperature(0.9F).downfall(0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(16775106).skyColor(16696674).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();	
	}
	
	public static Biome nestingGrounds() {
		MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
		BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder();
		return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).temperature(0.9F).downfall(0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(16775106).skyColor(16696674).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();	
	}

}
