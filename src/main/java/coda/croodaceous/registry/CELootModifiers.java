package coda.croodaceous.registry;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.loot.CroodsArchaeologyLootModifier;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CELootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, CroodaceousMod.MOD_ID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> CROODS_ARCHAEOLOGY_LOOT_MODIFIER = LOOT_MODIFIERS.register("croods_archaeology_glm", CroodsArchaeologyLootModifier.CODEC);
}
