package coda.croodaceous.registry;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.world.structure.BearowlDenStructure;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class CEStructures {
    public static void init() {}

    public static final StructureType<BearowlDenStructure> BEAROWL_DEN = register("bearowl_den", BearowlDenStructure.CODEC);

    private static <S extends Structure> StructureType<S> register(String id, Codec<S> codec) {
        return Registry.register(Registry.STRUCTURE_TYPES, new ResourceLocation(CroodaceousMod.MOD_ID, id), () -> codec);
    }
}
