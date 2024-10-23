package coda.croodaceous.common.loot;

import coda.croodaceous.registry.CEItems;
import coda.croodaceous.registry.CETags;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraft.world.level.storage.loot.functions.SetNameFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class CroodsArchaeologyLootModifier extends LootModifier {

    public CroodsArchaeologyLootModifier(LootItemCondition[] condition) {
        super(condition);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ObjectArrayList<ItemStack> ret = new ObjectArrayList<>();
        LootTable table = LootTable.lootTable().withPool(LootPool.lootPool().add(LootItem.lootTableItem(CEItems.PREHISTORIC_PIGMENT.get()).setWeight(2)).add(LootItem.lootTableItem(CEItems.PRIMAL_DEPICTION.get()).setWeight(1).apply(ExplorationMapFunction.makeExplorationMap().setDestination(CETags.FRACTURED_CHUNK_TAG).setMapDecoration(MapDecoration.Type.RED_X).setZoom((byte)1)).apply(SetNameFunction.setName(Component.translatable("filled_map.fractured_chunk"))))).build();

        return ret;
    }

    public static final Supplier<Codec<CroodsArchaeologyLootModifier>> CODEC = () -> RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, CroodsArchaeologyLootModifier::new));

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }

}