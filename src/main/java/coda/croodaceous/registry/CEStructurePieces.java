package coda.croodaceous.registry;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.world.structure.BearowlDenGenerator;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public class CEStructurePieces {
    public static void init() {}

    public static final StructurePieceType BEAROWL_DEN = register(BearowlDenGenerator.Piece::new, "bearowl_den");

    private static StructurePieceType register(StructurePieceType.StructureTemplateType type, String id) {
        return register((StructurePieceType)type, id);
    }

    private static StructurePieceType register(StructurePieceType type, String id) {
        return Registry.register(Registry.STRUCTURE_PIECE, new ResourceLocation(CroodaceousMod.MOD_ID, id), type);
    }

}