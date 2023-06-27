package coda.croodaceous.registry;

import coda.croodaceous.CroodaceousMod;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("unused")
public class CEItems {
    public static final CreativeModeTab TAB = new CreativeModeTab(CroodaceousMod.MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(RAMU_EGG.get());
        }
    };

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CroodaceousMod.MOD_ID);

    // Food
    public static final RegistryObject<Item> RAMU_EGG = ITEMS.register("ramu_egg", () -> new Item(new Item.Properties().tab(TAB).stacksTo(16).food(new FoodProperties.Builder().saturationMod(0.55F).nutrition(6).build())));

    // Spawn Eggs
    public static final RegistryObject<Item> LIYOTE_SPAWN_EGG = ITEMS.register("liyote_spawn_egg", () -> new ForgeSpawnEggItem(CEEntities.LIYOTE, 0xd2761d, 0x595c92, new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> BEAROWL_SPAWN_EGG = ITEMS.register("bearowl_spawn_egg", () -> new ForgeSpawnEggItem(CEEntities.BEAROWL, 0x463830, 0x8c867b, new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> RAMU_SPAWN_EGG = ITEMS.register("ramu_spawn_egg", () -> new ForgeSpawnEggItem(CEEntities.RAMU, 0x222522, 0xb18444, new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> FANG_FLY_SPAWN_EGG = ITEMS.register("fang_fly_spawn_egg", () -> new ForgeSpawnEggItem(CEEntities.FANG_FLY, 0xeb7626, 0x5a3629, new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> JACKROBAT_SPAWN_EGG = ITEMS.register("jackrobat_spawn_egg", () -> new ForgeSpawnEggItem(CEEntities.JACKROBAT, 0x442f54, 0xc14915, new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> TRIP_GERBIL_SPAWN_EGG = ITEMS.register("trip_gerbil_spawn_egg", () -> new ForgeSpawnEggItem(CEEntities.TRIP_GERBIL, 0x393939, 0xe0b936, new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> BEAR_PEAR_SPAWN_EGG = ITEMS.register("bear_pear_spawn_egg", () -> new ForgeSpawnEggItem(CEEntities.BEAR_PEAR, 0xf1982b, 0xb85e25, new Item.Properties().tab(TAB)));
    public static final RegistryObject<Item> TURTLE_DOVE_SPAWN_EGG = ITEMS.register("turtle_dove_spawn_egg", () -> new ForgeSpawnEggItem(CEEntities.TURTLE_DOVE, 0x0, 0x0, new Item.Properties().tab(TAB)));

    // Other
    public static final RegistryObject<BlockItem> DESERT_BAOBAB_BRANCHES = ITEMS.register("desert_baobab_branches", () -> new StandingAndWallBlockItem(CEBlocks.DESERT_BAOBAB_BRANCHES.get(), CEBlocks.DESERT_BAOBAB_WALL_BRANCHES.get(), new Item.Properties().tab(TAB)));

}