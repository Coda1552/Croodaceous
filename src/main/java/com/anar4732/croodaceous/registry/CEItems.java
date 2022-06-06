package com.anar4732.croodaceous.registry;

import com.anar4732.croodaceous.CroodaceousMod;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CEItems {
    public static final CreativeModeTab TAB = new CreativeModeTab(CroodaceousMod.ID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(RAMU_EGG.get());
        }
    };

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CroodaceousMod.ID);

    // Food
    public static final RegistryObject<Item> RAMU_EGG =
            ITEMS.register("ramu_egg", () -> new Item(new Item.Properties().tab(TAB).stacksTo(16).food(new FoodProperties.Builder().saturationMod(0.55F).nutrition(6).build())));

    // Spawn Eggs
    public static final RegistryObject<Item> ITEM_LIYOTE_SPAWN_EGG =
            ITEMS.register("liyote_spawn_egg", () -> new ForgeSpawnEggItem(CEEntities.ENTITY_LIYOTE, 0xd2761d, 0x595c92, new Item.Properties().tab(TAB)));
    
    // Bearowl Spawn Egg
    public static final RegistryObject<Item> ITEM_BEAROWL_SPAWN_EGG =
            ITEMS.register("bearowl_spawn_egg", () -> new ForgeSpawnEggItem(CEEntities.ENTITY_BEAROWL, 0x000000, 0x000000, new Item.Properties().tab(TAB)));
    
}