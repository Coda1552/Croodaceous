package coda.croodaceous.common.world.tree;

import coda.croodaceous.common.world.Entry;
import coda.croodaceous.registry.CEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class CoolerTreeFeature extends Feature<NoneFeatureConfiguration> {
    private static final Function<Direction.Axis, BlockState> WOOD = (axis) -> Blocks.STRIPPED_BIRCH_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    private static final Function<Direction, BlockState> BRANCH_THING = (direction) -> Blocks.DEAD_BRAIN_CORAL_WALL_FAN.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false).setValue(BlockStateProperties.HORIZONTAL_FACING, direction);
    private static final BlockState leaves = Blocks.BIRCH_LEAVES.defaultBlockState();

    public CoolerTreeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> pContext) {
        WorldGenLevel iSeedReader = pContext.level();
        ChunkGenerator chunkGenerator = pContext.chunkGenerator();
        Random random = pContext.random();
        BlockPos blockPos = pContext.origin();

        //these should really be hashmaps :sobbing:
        ArrayList<Entry> filler = new ArrayList<>();
        ArrayList<Entry> branchFiller = new ArrayList<>();
        ArrayList<Entry> leavesFiller = new ArrayList<>();

        BlockState trunkLogState = WOOD.apply(Direction.Axis.Y);

        int thickTrunkHeight = 3 + random.nextInt(2);
        int maxBiggerSides = 2;
        float biggerSideChance = 0.4f;
        Direction[] directions = new Direction[]{Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST};
        for (Direction direction : directions) {
            BlockState horizontalLogState = WOOD.apply(direction.getAxis());
            BlockPos sideStartPos = blockPos.relative(direction, 2);
            if (maxBiggerSides != 0 && random.nextFloat() < biggerSideChance) {
                BlockPos sideEndPos = blockPos.relative(direction, 3);
                int maxHeight = 1 + (random.nextFloat() < 0.25 ? 1 : 0);
                for (int j = 0; j < thickTrunkHeight; j++) {
                    BlockPos sideTrunkPos = sideStartPos.above(j);
                    int height = Math.max(0, maxHeight - j);
                    if (height != 0) {
                        for (int k = -1; k <= 1; k++) {
                            if (k != 0) {
                                Direction next = direction.getClockWise();
                                BlockPos pos = sideTrunkPos.relative(next, k);
                                if (!canPlace(iSeedReader, pos)) {
                                    return false;
                                }
                                filler.add(new Entry(pos, trunkLogState));
                            }
                        }
                    }
                    if (!canPlace(iSeedReader, sideTrunkPos)) {
                        return false;
                    }
                    filler.add(new Entry(sideTrunkPos, trunkLogState));
                }
                if (!canPlace(iSeedReader, sideEndPos)) {
                    return false;
                }
                filler.add(new Entry(sideEndPos, horizontalLogState));
                maxBiggerSides--;
            } else {
                if (!canPlace(iSeedReader, sideStartPos)) {
                    return false;
                }
                filler.add(new Entry(sideStartPos, horizontalLogState));
            }
        }
        for (int i = 0; i < 9; i++) {
            int xOffset = (i / 3) - 1;
            int zOffset = i % 3 - 1;
            for (int j = 0; j < thickTrunkHeight; j++) {
                BlockPos trunkPos = blockPos.offset(xOffset, j, zOffset);
                if (!canPlace(iSeedReader, trunkPos)) {
                    return false;
                }
                filler.add(new Entry(trunkPos, trunkLogState));
            }
        }
        int upperTrunkHeight = 4 + random.nextInt(6);
        BlockPos upperTrunkBase = blockPos.above(thickTrunkHeight);
        for (int i = 0; i < upperTrunkHeight; i++) {
            BlockPos woodPos = upperTrunkBase.above(i);
            if (!canPlace(iSeedReader, woodPos)) {
                return false;
            }
            filler.add(new Entry(woodPos, trunkLogState));
            if (i != upperTrunkHeight - 1) {
                addBranches(branchFiller, woodPos);
            }
        }
        for (Direction direction : directions) {
            BlockPos sideTrunkBase = upperTrunkBase.relative(direction);
            int sideUpperTrunkHeight = Math.min(2 + random.nextInt(3), upperTrunkHeight - 3);
            for (int i = 0; i < sideUpperTrunkHeight; i++) {
                BlockPos woodPos = sideTrunkBase.above(i);
                if (!canPlace(iSeedReader, woodPos)) {
                    return false;
                }
                filler.add(new Entry(woodPos, trunkLogState));
                addBranches(branchFiller, woodPos);
            }
            if (random.nextBoolean()) {
                BlockPos woodPos = sideTrunkBase.relative(direction.getClockWise());
                if (!canPlace(iSeedReader, woodPos)) {
                    return false;
                }
                filler.add(new Entry(woodPos, trunkLogState));
            }
        }
        BlockPos upperTrunkTop = upperTrunkBase.above(upperTrunkHeight-1);
        Direction splitOffDirection = directions[random.nextInt(directions.length)];
        BlockPos splitOffPos = upperTrunkTop.relative(splitOffDirection);
        Direction splitOffBranchDirection = random.nextBoolean() ? splitOffDirection.getClockWise() : splitOffDirection.getCounterClockWise();
        BlockPos splitOffSidePos = splitOffPos.relative(splitOffBranchDirection);

        if (!canPlace(iSeedReader, splitOffPos)) {
            return false;
        }
        filler.add(new Entry(splitOffPos, BRANCH_THING.apply(splitOffBranchDirection.getOpposite())));

        if (!canPlace(iSeedReader, splitOffSidePos)) {
            return false;
        }
        filler.add(new Entry(splitOffSidePos, trunkLogState));
        makeLeafBlob(leavesFiller, random, upperTrunkTop.above());


        fill(iSeedReader, filler, false);

        int maxAmountOfLeaves = 6 + random.nextInt(8);
        Collections.shuffle(branchFiller);

        fill(iSeedReader, branchFiller.subList(0, maxAmountOfLeaves), true);

        fill(iSeedReader, leavesFiller, true);

        return false;
    }

    public static void addBranches(List<Entry> filler, BlockPos pos) {
        Direction[] directions = new Direction[]{Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST};
        for (Direction direction : directions) {
            filler.add(new Entry(pos.relative(direction), BRANCH_THING.apply(direction)));
        }
    }

    public static void makeLeafBlob(List<Entry> filler, Random rand, BlockPos pos) {
        makeLeafSlice(filler, pos, 3);
        makeLeafSlice(filler, pos.above(1), 2);
    }

    public static void makeLeafSlice(List<Entry> filler, BlockPos pos, int leavesSize) {
        for (int x = -leavesSize; x <= leavesSize; x++) {
            for (int z = -leavesSize; z <= leavesSize; z++) {
                if (Math.abs(x) == leavesSize && Math.abs(z) == leavesSize) {
                    continue;
                }
                BlockPos leavesPos = new BlockPos(pos).offset(x, 0, z);
                filler.add(new Entry(leavesPos, leaves));
            }
        }
    }


    public static void fill(WorldGenLevel reader, List<Entry> filler, boolean careful) {
        for (Entry entry : filler) {
            if (careful && !canPlace(reader, entry.pos)) {
                continue;
            }
            reader.setBlock(entry.pos, entry.state, 3);
        }
    }

    public static boolean canPlace(WorldGenLevel reader, BlockPos pos) {
        //todo implement some more proper 'is outside of world' check, mekanism has one
        if (pos.getY() > reader.getMaxBuildHeight() || pos.getY() < 0) {
            return false;
        }
        return reader.getBlockState(pos).getBlock().equals(CEBlocks.DESERT_BAOBAB_SAPLING.get()) || reader.isEmptyBlock(pos) || reader.getBlockState(pos).getMaterial().isReplaceable();
    }
}