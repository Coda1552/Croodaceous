package com.anar4732.croodaceous.registry;

import com.anar4732.croodaceous.CroodaceousMod;
import com.anar4732.croodaceous.client.render.LiyoteRenderer;
import com.anar4732.croodaceous.client.render.RamuRenderer;
import com.anar4732.croodaceous.client.render.SimpleGeoRenderer;
import com.anar4732.croodaceous.common.entities.BearowlEntity;
import com.anar4732.croodaceous.common.entities.LiyoteEntity;
import com.anar4732.croodaceous.common.entities.RamuEntity;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CEEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, CroodaceousMod.ID);

    public static final RegistryObject<EntityType<LiyoteEntity>> ENTITY_LIYOTE =
            ENTITIES.register("liyote", () -> EntityType.Builder.<LiyoteEntity>of(LiyoteEntity::new, MobCategory.CREATURE)
                    .sized(0.75F, 0.6F)
                    .setTrackingRange(16)
                    .updateInterval(1)
                    .build("liyote"));
    
    public static final RegistryObject<EntityType<BearowlEntity>> ENTITY_BEAROWL =
            ENTITIES.register("bearowl", () -> EntityType.Builder.<BearowlEntity>of(BearowlEntity::new, MobCategory.CREATURE)
                    .sized(2.5F, 2.5F)
                    .setTrackingRange(16)
                    .updateInterval(1)
                    .build("bearowl"));
    
    public static final RegistryObject<EntityType<RamuEntity>> ENTITY_RAMU =
            ENTITIES.register("ramu", () -> EntityType.Builder.<RamuEntity>of(RamuEntity::new, MobCategory.CREATURE)
                                                                 .sized(1F, 2F)
                                                                 .setTrackingRange(16)
                                                                 .updateInterval(1)
                                                                 .build("ramu"));
    
    public static void registerAttributes(final EntityAttributeCreationEvent e) {
        e.put(CEEntities.ENTITY_LIYOTE.get(), LiyoteEntity.createAttributes().build());
        e.put(CEEntities.ENTITY_BEAROWL.get(), BearowlEntity.createAttributes().build());
        e.put(CEEntities.ENTITY_RAMU.get(), RamuEntity.createAttributes().build());
    }

    public static void registerRenderers(final FMLClientSetupEvent e) {
        EntityRenderers.register(CEEntities.ENTITY_LIYOTE.get(), LiyoteRenderer::new);
        EntityRenderers.register(CEEntities.ENTITY_BEAROWL.get(), mgr -> new SimpleGeoRenderer<>(mgr, CroodaceousMod.ID, "bearowl"));
        EntityRenderers.register(CEEntities.ENTITY_RAMU.get(), RamuRenderer::new);
    }
}