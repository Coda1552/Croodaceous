package coda.croodaceous.common.blocks;

import coda.croodaceous.registry.CEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;

public class BaobabSaplingBlock extends SaplingBlock {

    public BaobabSaplingBlock(final AbstractTreeGrower treeGrower, Properties properties) {
        super(treeGrower, properties);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.below()).getBlock().equals(CEBlocks.DESOLATE_SAND.get());
    }
}