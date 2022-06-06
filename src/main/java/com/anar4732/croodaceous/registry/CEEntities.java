package com.anar4732.croodaceous.registry;

import com.anar4732.croodaceous.CroodaceousMod;
import com.anar4732.croodaceous.common.entities.LiyoteEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
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

}
