package coda.croodaceous.registry;

import coda.croodaceous.CroodaceousMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class CEConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> DESERT_BAOBAB = ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(CroodaceousMod.MOD_ID, "desert_baobab"));

}
