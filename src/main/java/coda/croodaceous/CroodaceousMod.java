package coda.croodaceous;

import coda.croodaceous.common.entities.*;
import coda.croodaceous.common.network.CENetwork;
import coda.croodaceous.registry.*;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CroodaceousMod.MOD_ID)
public class CroodaceousMod {
	public static final String MOD_ID = "croodaceous";
	public static final Logger LOGGER = LogUtils.getLogger();

	public CroodaceousMod() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;

		bus.addListener(this::registerAttributes);
		bus.addListener(this::registerSpawnPlacements);

		CEEntities.ENTITIES.register(bus);
		CEItems.ITEMS.register(bus);
		CEBlocks.BLOCKS.register(bus);
		CEPoiTypes.POIS.register(bus);
		CEFeatures.FEATURES.register(bus);

		CENetwork.register();

	}

	private void registerAttributes(final EntityAttributeCreationEvent e) {
		e.put(CEEntities.LIYOTE.get(), Liyote.createAttributes().build());
		e.put(CEEntities.BEAROWL.get(), Bearowl.createAttributes().build());
		e.put(CEEntities.RAMU.get(), Ramu.createAttributes().build());
		e.put(CEEntities.FANG_FLY.get(), FangFly.createAttributes().build());
		e.put(CEEntities.JACKROBAT.get(), Jackrobat.createAttributes().build());
		e.put(CEEntities.TRIP_GERBIL.get(), TripGerbil.createAttributes().build());
		e.put(CEEntities.BEAR_PEAR.get(), BearPear.createAttributes().build());
		e.put(CEEntities.TURTLE_DOVE.get(), TurtleDove.createAttributes().build());
	}

	private void registerSpawnPlacements(final SpawnPlacementRegisterEvent event) {
		event.register(CEEntities.LIYOTE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Liyote::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
		event.register(CEEntities.FANG_FLY.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, FangFly::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
		event.register(CEEntities.RAMU.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Ramu::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
		event.register(CEEntities.JACKROBAT.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Jackrobat::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
		event.register(CEEntities.TRIP_GERBIL.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, TripGerbil::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
		event.register(CEEntities.BEAR_PEAR.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, BearPear::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
		event.register(CEEntities.TURTLE_DOVE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, TurtleDove::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
	}
}