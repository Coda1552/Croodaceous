package coda.croodaceous;

import coda.croodaceous.registry.*;
import com.mojang.logging.LogUtils;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
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
		bus.addListener(this::clientSetup);
		bus.addListener(CEEntities::registerAttributes);
		bus.addListener(CEEntities::registerRenderers);

		CEEntities.ENTITIES.register(bus);
		CEItems.ITEMS.register(bus);
		CEBlocks.BLOCKS.register(bus);
		CEPointOfInterestTypes.POIS.register(bus);
		CEFeatures.FEATURES.register(bus);
	}
	
	/**
	 * Set a breakpoint {@link net.minecraft.Util#doPause(String) here} to debug any crash in the IDE
	 */
	private void setup(final FMLCommonSetupEvent event) {
		SharedConstants.IS_RUNNING_IN_IDE = !FMLEnvironment.production;
	}

	private void clientSetup(final FMLClientSetupEvent e) {
		ItemBlockRenderTypes.setRenderLayer(CEBlocks.RAMU_NEST.get(), RenderType.cutout());
	}
	
}