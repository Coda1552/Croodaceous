package coda.croodaceous.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.DeadBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.Random;

public class DryBushBlock extends DeadBushBlock implements BonemealableBlock {
    public static final IntegerProperty SIZE = BlockStateProperties.AGE_2;

    public DryBushBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SIZE,2));
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(SIZE, 2);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(SIZE);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pPlayer.getItemInHand(pHand).is(Tags.Items.SHEARS) && pState.getValue(SIZE) > 0) {
            pLevel.setBlock(pPos, pState.setValue(SIZE, pState.getValue(SIZE) - 1), 2);
            pLevel.playSound(pPlayer, pPos, SoundEvents.MOSS_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            pPlayer.swing(pHand);

            for (int i = 0; i < 25; i++) {
                pLevel.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, pState), pPos.getX() + 0.5D, pPos.getY() + 0.5D, pPos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
            }

            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }
        else {
            return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
        }
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
        return pState.getValue(SIZE) < 2;
    }

    @Override
    public boolean isBonemealSuccess(Level pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
        if (pState.getValue(SIZE) < 2) {
            pLevel.setBlock(pPos, pState.setValue(SIZE, pState.getValue(SIZE) + 1), 2);
            pLevel.playSound(null, pPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
}
