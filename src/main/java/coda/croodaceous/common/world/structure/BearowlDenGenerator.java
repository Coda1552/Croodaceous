package coda.croodaceous.common.world.structure;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.registry.CEStructurePieces;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class BearowlDenGenerator {
    private static final ResourceLocation REGULAR_TEMPLATES = new ResourceLocation(CroodaceousMod.MOD_ID, "bearowl_den");

    public static void addParts(StructureTemplateManager structureTemplateManager, BlockPos pos, Rotation rotation, StructurePieceAccessor holder, RandomSource random) {
        holder.addPiece(new BearowlDenGenerator.Piece(structureTemplateManager, REGULAR_TEMPLATES, pos, rotation));
    }

    public static class Piece extends TemplateStructurePiece {

        public Piece(StructureTemplateManager manager, ResourceLocation identifier, BlockPos pos, Rotation rotation) {
            super(CEStructurePieces.BEAROWL_DEN, 0, manager, identifier, identifier.toString(), BearowlDenGenerator.Piece.createPlacementData(rotation), pos);
        }

        public Piece(StructureTemplateManager manager, CompoundTag nbt) {
            super(CEStructurePieces.BEAROWL_DEN, nbt, manager, identifier -> BearowlDenGenerator.Piece.createPlacementData(Rotation.valueOf(nbt.getString("Rot"))));
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag nbt) {
            super.addAdditionalSaveData(context, nbt);
            nbt.putString("Rot", this.placeSettings.getRotation().name());
        }

        @Override
        protected void handleDataMarker(String metadata, BlockPos pos, ServerLevelAccessor world, RandomSource random, BoundingBox boundingBox) {
            if ("bearowl".equals(metadata)) {
/*                BearowlEntity bearowl = CEEntities.BEAROWL.get().create(world.getLevel());

                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);

                bearowl.moveTo(pos, 0.0F, 0.0F);
                bearowl.finalizeSpawn(world, world.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
                bearowl.homePos = bearowl.getOnPos().above();

                world.addFreshEntity(bearowl);*/
            }

        }

        private static StructurePlaceSettings createPlacementData(Rotation rotation) {
            return new StructurePlaceSettings().setRotation(rotation).setMirror(Mirror.NONE).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        }

        @Override
        public void postProcess(WorldGenLevel world, StructureManager structureAccessor, ChunkGenerator chunkGenerator, RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            BlockPos blockPos = new BlockPos(chunkPos.getWorldPosition());
            int i = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, blockPos.getX(), blockPos.getZ());
            this.templatePosition = new BlockPos(this.templatePosition.getX(), i + 1, this.templatePosition.getZ());
            super.postProcess(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, pivot);
        }

    }
}