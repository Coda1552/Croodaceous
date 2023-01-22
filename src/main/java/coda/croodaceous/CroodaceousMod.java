package coda.croodaceous;

import coda.croodaceous.common.entities.Bearowl;
import coda.croodaceous.common.entities.FangFly;
import coda.croodaceous.common.entities.Liyote;
import coda.croodaceous.common.entities.Ramu;
import coda.croodaceous.registry.*;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CroodaceousMod.MOD_ID)
public class CroodaceousMod {
	public static final String MOD_ID = "croodaceous";
	private static final Logger LOGGER = LogUtils.getLogger();

	public CroodaceousMod() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;

		bus.addListener(this::registerAttributes);
		bus.addListener(this::commonSetup);

		CEEntities.ENTITIES.register(bus);
		CEItems.ITEMS.register(bus);
		CEBlocks.BLOCKS.register(bus);
		CEPoiTypes.POIS.register(bus);
		CEFeatures.FEATURES.register(bus);

	}
	
	private void registerAttributes(final EntityAttributeCreationEvent e) {
		e.put(CEEntities.LIYOTE.get(), Liyote.createAttributes().build());
		e.put(CEEntities.BEAROWL.get(), Bearowl.createAttributes().build());
		e.put(CEEntities.RAMU.get(), Ramu.createAttributes().build());
		e.put(CEEntities.FANG_FLY.get(), FangFly.createAttributes().build());
	}

	private void commonSetup(final FMLCommonSetupEvent e) {
		e.enqueueWork(() -> {
			CEStructures.init();
			CEStructurePieces.init();

			SpawnPlacements.register(CEEntities.LIYOTE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Liyote::canSpawn);
			SpawnPlacements.register(CEEntities.FANG_FLY.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, FangFly::canSpawn);
			SpawnPlacements.register(CEEntities.RAMU.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Ramu::canSpawn);
		});
	}

}