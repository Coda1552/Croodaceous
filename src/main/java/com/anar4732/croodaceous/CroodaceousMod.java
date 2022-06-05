package com.anar4732.croodaceous;

import com.anar4732.croodaceous.rendering.LiyoteRenderer;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(CroodaceousMod.ID)
public class CroodaceousMod {
	public static final String ID = "croodaceous";
	private static final Logger LOGGER = LogUtils.getLogger();
	
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, ID);
	
	public static final RegistryObject<EntityType<LiyoteEntity>> ENTITY_LIYOTE =
			ENTITIES.register("liyote", () -> EntityType.Builder.<LiyoteEntity>of(LiyoteEntity::new, MobCategory.CREATURE)
                    .sized(1F, 1F)
                    .setTrackingRange(16)
                    .updateInterval(1)
                    .build("liyote"));
	
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
	
	public static final RegistryObject<Item> ITEM_LIYOTE_SPAWN_EGG =
			ITEMS.register("liyote_spawn_egg", () -> new ForgeSpawnEggItem(ENTITY_LIYOTE, 0xd2761d, 0x595c92, new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
	
	public CroodaceousMod() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::setup);
		modEventBus.addListener(this::clientSetup);
		modEventBus.addListener(this::registerEntitiyAttribute);
		ENTITIES.register(modEventBus);
		ITEMS.register(modEventBus);
		
//		MinecraftForge.EVENT_BUS.register(this);
	}
	
	private void setup(final FMLCommonSetupEvent event) {
	
	}
	
	private void clientSetup(final FMLClientSetupEvent e) {
		EntityRenderers.register(ENTITY_LIYOTE.get(), m -> new LiyoteRenderer(m));
		
	}
	
	private void registerEntitiyAttribute(final EntityAttributeCreationEvent e) {
		e.put(ENTITY_LIYOTE.get(), LiyoteEntity.createAttributes().build());
	}
	
}