package coda.croodaceous.registry;

import coda.croodaceous.common.entities.FangFly;
import coda.croodaceous.common.entities.Ramu;
import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.entities.Bearowl;
import coda.croodaceous.common.entities.Liyote;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CEEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CroodaceousMod.MOD_ID);

    public static final RegistryObject<EntityType<Liyote>> LIYOTE = ENTITIES.register("liyote", () -> EntityType.Builder.of(Liyote::new, MobCategory.CREATURE).sized(0.75F, 0.6F).setTrackingRange(16).updateInterval(1).build("liyote"));
    public static final RegistryObject<EntityType<Bearowl>> BEAROWL = ENTITIES.register("bearowl", () -> EntityType.Builder.of(Bearowl::new, MobCategory.CREATURE).sized(2.5F, 2.5F).setTrackingRange(16).updateInterval(1).build("bearowl"));
    public static final RegistryObject<EntityType<Ramu>> RAMU = ENTITIES.register("ramu", () -> EntityType.Builder.of(Ramu::new, MobCategory.CREATURE).sized(1F, 2F).setTrackingRange(16).updateInterval(1).build("ramu"));
    public static final RegistryObject<EntityType<FangFly>> FANG_FLY = ENTITIES.register("fang_fly", () -> EntityType.Builder.of(FangFly::new, MobCategory.CREATURE).sized(0.7F, 0.7F).setTrackingRange(16).updateInterval(1).build("fang_fly"));
}