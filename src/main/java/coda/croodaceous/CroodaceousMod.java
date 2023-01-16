package coda.croodaceous;

import coda.croodaceous.common.entities.BearowlEntity;
import coda.croodaceous.common.entities.LiyoteEntity;
import coda.croodaceous.common.entities.RamuEntity;
import coda.croodaceous.registry.*;
import com.mojang.logging.LogUtils;
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
		e.put(CEEntities.LIYOTE.get(), LiyoteEntity.createAttributes().build());
		e.put(CEEntities.BEAROWL.get(), BearowlEntity.createAttributes().build());
		e.put(CEEntities.RAMU.get(), RamuEntity.createAttributes().build());
	}

	private void commonSetup(final FMLCommonSetupEvent e) {
		e.enqueueWork(() -> {
			CEStructures.init();
			CEStructurePieces.init();
		});
	}

}