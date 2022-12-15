package coda.croodaceous.common.blocks;

import coda.croodaceous.registry.CEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;

public class CESandBlock extends SandBlock {

    public CESandBlock(int pDustColor, Properties pProperties) {
        super(pDustColor, pProperties);
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable) {
        BlockState plant = plantable.getPlant(world, pos.relative(facing));

        if (plant.getBlock() == Blocks.CACTUS){
            return state.is(CEBlocks.DESOLATE_SAND.get());
        }
        else {
            return super.canSustainPlant(state, world, pos, facing, plantable);
        }
    }
}
