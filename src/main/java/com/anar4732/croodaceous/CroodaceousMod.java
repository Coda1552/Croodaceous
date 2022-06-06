package com.anar4732.croodaceous;

import com.anar4732.croodaceous.client.render.LiyoteRenderer;
import com.anar4732.croodaceous.common.entities.LiyoteEntity;
import com.anar4732.croodaceous.registry.CEEntities;
import com.anar4732.croodaceous.registry.CEItems;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CroodaceousMod.ID)
public class CroodaceousMod {
	public static final String ID = "croodaceous";
	private static final Logger LOGGER = LogUtils.getLogger();

	public CroodaceousMod() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::setup);
		bus.addListener(this::clientSetup);
		bus.addListener(this::registerEntitiyAttribute);
		CEEntities.ENTITIES.register(bus);
		CEItems.ITEMS.register(bus);
		
//		MinecraftForge.EVENT_BUS.register(this);
	}
	
	private void setup(final FMLCommonSetupEvent event) {
	
	}
	
	private void clientSetup(final FMLClientSetupEvent e) {
		EntityRenderers.register(CEEntities.ENTITY_LIYOTE.get(), LiyoteRenderer::new);
	}
	
	private void registerEntitiyAttribute(final EntityAttributeCreationEvent e) {
		e.put(CEEntities.ENTITY_LIYOTE.get(), LiyoteEntity.createAttributes().build());
	}
	
}