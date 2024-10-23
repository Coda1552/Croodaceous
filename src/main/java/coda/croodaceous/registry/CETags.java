package coda.croodaceous.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.structure.Structure;

public class CETags {
    public static final TagKey<Item> ARCH_ITEMS = createItemTag("archaeology_items");
    public static final TagKey<Structure> FRACTURED_CHUNK_TAG = createStructureTag("fractured_chunk");

    private static TagKey<Item> createItemTag(String pName) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(pName));
    }

    private static TagKey<Structure> createStructureTag(String pName) {
        return TagKey.create(Registries.STRUCTURE, new ResourceLocation(pName));
    }
}
