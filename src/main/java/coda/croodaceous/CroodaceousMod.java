package coda.croodaceous;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import coda.croodaceous.common.network.CENetwork;
import coda.croodaceous.registry.CEBiomeSources;
import coda.croodaceous.registry.CEBiomes;
import coda.croodaceous.registry.CEBlocks;
import coda.croodaceous.registry.CEChunkGenerators;
import coda.croodaceous.registry.CEEntities;
import coda.croodaceous.registry.CEFeatures;
import coda.croodaceous.registry.CEItems;
import coda.croodaceous.registry.CEPoiTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CroodaceousMod.MOD_ID)
public class CroodaceousMod {
	public static final String MOD_ID = "croodaceous";
	public static final Logger LOGGER = LogUtils.getLogger();

	public CroodaceousMod() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;

		bus.addListener(CEEntities::registerAttributes);
		bus.addListener(CEEntities::registerSpawnPlacements);

		CEEntities.ENTITIES.register(bus);
		CEItems.ITEMS.register(bus);
		CEBlocks.BLOCKS.register(bus);
		CEPoiTypes.POIS.register(bus);
		CEFeatures.FEATURES.register(bus);
		CEBiomes.BIOME.register(bus);
		CEBiomeSources.BIOME_SOURCE.register(bus);
		CEChunkGenerators.REGISTER.register(bus);

		CENetwork.register();

	}
}