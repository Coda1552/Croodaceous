package com.anar4732.croodaceous;

import com.anar4732.croodaceous.registry.CEBlocks;
import com.anar4732.croodaceous.registry.CEEntities;
import com.anar4732.croodaceous.registry.CEItems;
import com.anar4732.croodaceous.registry.CEPointOfInterestTypes;
import com.mojang.logging.LogUtils;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(CroodaceousMod.ID)
public class CroodaceousMod {
	public static final String ID = "croodaceous";
	private static final Logger LOGGER = LogUtils.getLogger();

	public CroodaceousMod() {
		SharedConstants.IS_RUNNING_IN_IDE = !FMLEnvironment.production;
		
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::setup);
		bus.addListener(this::clientSetup);
		bus.addListener(CEEntities::registerAttributes);
		bus.addListener(CEEntities::registerRenderers);
		CEEntities.ENTITIES.register(bus);
		CEItems.ITEMS.register(bus);
		CEBlocks.BLOCKS.register(bus);
		CEPointOfInterestTypes.POIS.register(bus);
		
//		MinecraftForge.EVENT_BUS.register(this);
	}
	
	private void setup(final FMLCommonSetupEvent event) {
	
	}
	
	private void clientSetup(final FMLClientSetupEvent e) {
		ItemBlockRenderTypes.setRenderLayer(CEBlocks.RAMU_NEST.get(), RenderType.cutout());
	}
	
}