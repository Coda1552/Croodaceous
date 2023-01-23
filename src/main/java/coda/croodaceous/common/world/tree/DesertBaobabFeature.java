package coda.croodaceous.common.world.tree;

import coda.croodaceous.common.world.Entry;
import coda.croodaceous.registry.CEBlocks;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DesertBaobabFeature extends Feature<NoneFeatureConfiguration> {
    private static final Function<Direction.Axis, BlockState> WOOD = (axis) -> CEBlocks.DESERT_BAOBAB_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    private static final Function<Direction, BlockState> BRANCH_THING = (direction) -> CEBlocks.DESERT_BAOBAB_WALL_BRANCHES.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, direction);
    private static final BlockState leaves = CEBlocks.DESERT_BAOBAB_LEAVES.get().defaultBlockState();

    public DesertBaobabFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> pContext) {
        WorldGenLevel iSeedReader = pContext.level();
        ChunkGenerator chunkGenerator = pContext.chunkGenerator();
        RandomSource random = pContext.random();
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
                filler.add(new Entry(sideEndPos, trunkLogState));
                if (!addDownwardsTrunk(iSeedReader, filler, sideEndPos)) {
                    return false;
                }
                maxBiggerSides--;
            } else {
                if (!canPlace(iSeedReader, sideStartPos)) {
                    return false;
                }
                filler.add(new Entry(sideStartPos, trunkLogState));
            }
            if (!addDownwardsTrunk(iSeedReader, filler, sideStartPos)) {
                return false;
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
                if (!addDownwardsTrunk(iSeedReader, filler, trunkPos)) {
                    return false;
                }
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
        filler.add(new Entry(splitOffSidePos.above(), trunkLogState));
        makeLeafBlob(leavesFiller, random, upperTrunkTop.above());


        fill(iSeedReader, filler, false);

        int maxAmountOfLeaves = 6 + random.nextInt(8);
        Collections.shuffle(branchFiller);

        fill(iSeedReader, branchFiller.subList(0, maxAmountOfLeaves), true);

        fill(iSeedReader, leavesFiller, true);
        updateLeaves(iSeedReader, filler.stream().map(e -> e.pos).collect(Collectors.toSet()));

        return false;
    }

    public static boolean addDownwardsTrunk(WorldGenLevel level, List<Entry> filler, BlockPos pos) {
        final BlockState log = WOOD.apply(Direction.Axis.Y);
        int i = 0;
        do {
            i++;
            BlockPos trunkPos = pos.below(i);
            if (canPlace(level, trunkPos)) {
                filler.add(new Entry(trunkPos, log));
            } else {
                break;
            }
            if (i > 3) {
                return false;
            }
            if (i > level.getMaxBuildHeight()) {
                break;
            }
        }
        while (true);
        return true;
    }

    public static void addBranches(List<Entry> filler, BlockPos pos) {
        Direction[] directions = new Direction[]{Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST};
        for (Direction direction : directions) {
            filler.add(new Entry(pos.relative(direction), BRANCH_THING.apply(direction)));
        }
    }

    public static void makeLeafBlob(List<Entry> filler, RandomSource rand, BlockPos pos) {
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

    public static void updateLeaves(LevelAccessor pLevel, Set<BlockPos> logPositions) {
        List<Set<BlockPos>> list = Lists.newArrayList();
        for (int j = 0; j < 6; ++j) {
            list.add(Sets.newHashSet());
        }

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (BlockPos pos : Lists.newArrayList(logPositions)) {
            for (Direction direction : Direction.values()) {
                mutable.setWithOffset(pos, direction);
                if (!logPositions.contains(mutable)) {
                    BlockState blockstate = pLevel.getBlockState(mutable);
                    if (blockstate.hasProperty(BlockStateProperties.DISTANCE)) {
                        list.get(0).add(mutable.immutable());
                        pLevel.setBlock(mutable, blockstate.setValue(BlockStateProperties.DISTANCE, 1), 19);
                    }
                }
            }
        }

        for (int l = 1; l < 6; ++l) {
            Set<BlockPos> set = list.get(l - 1);
            Set<BlockPos> set1 = list.get(l);

            for (BlockPos pos : set) {
                for (Direction direction1 : Direction.values()) {
                    mutable.setWithOffset(pos, direction1);
                    if (!set.contains(mutable) && !set1.contains(mutable)) {
                        BlockState blockstate1 = pLevel.getBlockState(mutable);
                        if (blockstate1.hasProperty(BlockStateProperties.DISTANCE)) {
                            int k = blockstate1.getValue(BlockStateProperties.DISTANCE);
                            if (k > l + 1) {
                                BlockState blockstate2 = blockstate1.setValue(BlockStateProperties.DISTANCE, l + 1);
                                pLevel.setBlock(mutable, blockstate2, 19);
                                set1.add(mutable.immutable());
                            }
                        }
                    }
                }
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