package coda.croodaceous;

import coda.croodaceous.common.entities.BearowlEntity;
import coda.croodaceous.common.entities.LiyoteEntity;
import coda.croodaceous.common.entities.RamuEntity;
import coda.croodaceous.registry.CEBlocks;
import coda.croodaceous.registry.CEEntities;
import coda.croodaceous.registry.CEItems;
import coda.croodaceous.registry.CEPoiTypes;
import com.mojang.logging.LogUtils;
import net.minecraft.SharedConstants;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(CroodaceousMod.MOD_ID)
public class CroodaceousMod {
	public static final String MOD_ID = "croodaceous";
	private static final Logger LOGGER = LogUtils.getLogger();

	public CroodaceousMod() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;

		bus.addListener(this::setup);
		bus.addListener(this::registerAttributes);

		CEEntities.ENTITIES.register(bus);
		CEItems.ITEMS.register(bus);
		CEBlocks.BLOCKS.register(bus);
		CEPoiTypes.POIS.register(bus);
		
//		MinecraftForge.EVENT_BUS.register(this);
	}
	
	private void setup(final FMLCommonSetupEvent event) {
		SharedConstants.IS_RUNNING_IN_IDE = !FMLEnvironment.production;
	}

	private void registerAttributes(final EntityAttributeCreationEvent e) {
		e.put(CEEntities.ENTITY_LIYOTE.get(), LiyoteEntity.createAttributes().build());
		e.put(CEEntities.ENTITY_BEAROWL.get(), BearowlEntity.createAttributes().build());
		e.put(CEEntities.ENTITY_RAMU.get(), RamuEntity.createAttributes().build());
	}
	
}