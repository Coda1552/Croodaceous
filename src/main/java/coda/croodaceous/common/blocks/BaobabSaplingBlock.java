package coda.croodaceous.common.blocks;

import coda.croodaceous.registry.CEBlocks;
import coda.croodaceous.registry.CEFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Random;

public class BaobabSaplingBlock extends SaplingBlock {

    public BaobabSaplingBlock(Properties properties) {
        super(null, properties);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.below()).getBlock().equals(CEBlocks.DESOLATE_SAND.get());
    }
    
    @Override
    public void advanceTree(ServerLevel world, BlockPos pos, BlockState state, Random rand) {
        if (state.getValue(STAGE) == 0) {
            world.setBlock(pos, state.cycle(STAGE), 4);
        }
        else {
            if (!net.minecraftforge.event.ForgeEventFactory.saplingGrowTree(world, rand, pos)) {
                return;
            }
            CEFeatures.THE_OTHER_ONE.get().place(NoneFeatureConfiguration.INSTANCE, world, world.getChunkSource().getGenerator(), rand, pos);
        }
    }
}