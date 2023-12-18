package coda.croodaceous.registry;

import coda.croodaceous.common.entities.BearPear;
import coda.croodaceous.common.entities.FangFly;
import coda.croodaceous.common.entities.Jackrobat;
import coda.croodaceous.common.entities.Ramu;
import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.entities.Bearowl;
import coda.croodaceous.common.entities.Liyote;
import coda.croodaceous.common.entities.TripGerbil;
import coda.croodaceous.common.entities.TurtleDove;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CEEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CroodaceousMod.MOD_ID);

    public static final RegistryObject<EntityType<Liyote>> LIYOTE = ENTITIES.register("liyote", () -> EntityType.Builder.of(Liyote::new, MobCategory.CREATURE).sized(0.75F, 0.6F).clientTrackingRange(10).build("liyote"));
    public static final RegistryObject<EntityType<Bearowl>> BEAROWL = ENTITIES.register("bearowl", () -> EntityType.Builder.of(Bearowl::new, MobCategory.CREATURE).sized(2.5F, 2.5F).clientTrackingRange(8).build("bearowl"));
    public static final RegistryObject<EntityType<Ramu>> RAMU = ENTITIES.register("ramu", () -> EntityType.Builder.of(Ramu::new, MobCategory.CREATURE).sized(1F, 2F).build("ramu"));
    public static final RegistryObject<EntityType<FangFly>> FANG_FLY = ENTITIES.register("fang_fly", () -> EntityType.Builder.of(FangFly::new, MobCategory.CREATURE).sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(1).build("fang_fly"));
    public static final RegistryObject<EntityType<Jackrobat>> JACKROBAT = ENTITIES.register("jackrobat", () -> EntityType.Builder.of(Jackrobat::new, MobCategory.CREATURE).sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(1).build("jackrobat"));
    public static final RegistryObject<EntityType<TripGerbil>> TRIP_GERBIL = ENTITIES.register("trip_gerbil", () -> EntityType.Builder.of(TripGerbil::new, MobCategory.CREATURE).sized(0.5F, 0.5F).build("trip_gerbil"));
    public static final RegistryObject<EntityType<BearPear>> BEAR_PEAR = ENTITIES.register("bear_pear", () -> EntityType.Builder.of(BearPear::new, MobCategory.CREATURE).sized(0.4375F, 0.6875F).updateInterval(1).build("bear_pear"));
    public static final RegistryObject<EntityType<TurtleDove>> TURTLE_DOVE = ENTITIES.register("turtle_dove", () -> EntityType.Builder.of(TurtleDove::new, MobCategory.CREATURE).sized(1.825F, 0.98F).clientTrackingRange(12).updateInterval(1).build("turtle_dove"));

}