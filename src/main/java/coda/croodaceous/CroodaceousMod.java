package coda.croodaceous;

import coda.croodaceous.common.entities.Bearowl;
import coda.croodaceous.common.entities.FangFly;
import coda.croodaceous.common.entities.Liyote;
import coda.croodaceous.common.entities.Ramu;
import coda.croodaceous.registry.*;
import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.Block;
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

		bus.addListener(CEEntities::registerAttributes);
		bus.addListener(CEEntities::registerSpawnPlacements);
		bus.addListener(this::commonSetup);

		CEEntities.ENTITIES.register(bus);
		CEItems.ITEMS.register(bus);
		CEBlocks.BLOCKS.register(bus);
		CEPoiTypes.POIS.register(bus);
		CEFeatures.FEATURES.register(bus);

	}

	private void commonSetup(final FMLCommonSetupEvent e) {
		// removed STRIPPABLES map modifications in favor of IForgeBlock#getToolModifiedState
	}

}