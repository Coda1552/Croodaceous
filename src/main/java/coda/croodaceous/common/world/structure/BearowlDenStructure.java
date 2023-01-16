package coda.croodaceous.common.world.structure;

import coda.croodaceous.registry.CEStructures;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;

public class BearowlDenStructure extends Structure {
    public static final Codec<BearowlDenStructure> CODEC = BearowlDenStructure.simpleCodec(BearowlDenStructure::new);

    public BearowlDenStructure(StructureSettings config) {
        super(config);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        return BearowlDenStructure.onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG, collector -> this.addPieces(collector, context));
    }

    private void addPieces(StructurePiecesBuilder collector, Structure.GenerationContext context) {
        Rotation blockRotation = Rotation.getRandom(context.random());
        BlockPos blockPos = new BlockPos(context.chunkPos().getMinBlockX(), 64, context.chunkPos().getMinBlockZ());
        BearowlDenGenerator.addParts(context.structureTemplateManager(), blockPos, blockRotation, collector, context.random());
    }

    @Override
    public StructureType<?> type() {
        return CEStructures.BEAROWL_DEN;
    }


}