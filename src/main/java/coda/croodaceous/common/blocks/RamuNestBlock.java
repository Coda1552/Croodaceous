package coda.croodaceous.common.blocks;

import coda.croodaceous.common.entities.Ramu;
import coda.croodaceous.registry.CEBlocks;
import coda.croodaceous.registry.CEEntities;
import coda.croodaceous.registry.CEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class RamuNestBlock extends Block {
	private static final VoxelShape SHAPE = Block.box(0D, 0D, 0D, 16.0D, 8.0D, 16.0D);
	public static final BooleanProperty WITH_EGG = BooleanProperty.create("with_egg");
	public static final IntegerProperty HATCH = BlockStateProperties.HATCH;

	public RamuNestBlock() {
		super(BlockBehaviour.Properties.of(Material.GRASS, MaterialColor.COLOR_YELLOW).strength(0.5F).sound(SoundType.GRASS).randomTicks());
		this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, 0).setValue(WITH_EGG, Boolean.FALSE));
	}

	public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
		if (pState.getValue(WITH_EGG)) {
			int i = pState.getValue(HATCH);

			if (i < 2) {
				pLevel.playSound(null, pPos, SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 0.7F, 0.9F + pRandom.nextFloat() * 0.2F);
				pLevel.setBlock(pPos, pState.setValue(HATCH, i + 1), 2);
			}
			else {
				pLevel.playSound(null, pPos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.7F, 0.9F + pRandom.nextFloat() * 0.2F);
				pLevel.setBlock(pPos, pState.setValue(WITH_EGG, false), 2);

				pLevel.levelEvent(2001, pPos, Block.getId(pState));
				Ramu ramu = CEEntities.RAMU.get().create(pLevel);
				ramu.setAge(-24000);
				ramu.moveTo((double)pPos.getX() + 0.5D, pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, 0.0F, 0.0F);
				pLevel.addFreshEntity(ramu);
			}
		}
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
		if (pState.getValue(WITH_EGG)) {
			if (pLevel.isClientSide) {
				// todo - check sounds, dont think these are playing. are sounds server-sided?
				pLevel.playSound(pPlayer, pPos, SoundEvents.GRASS_FALL, SoundSource.BLOCKS, 1.0F, 1.0F);
				return InteractionResult.SUCCESS;
			}

			pLevel.setBlock(pPos, this.getStateDefinition().any().setValue(WITH_EGG, Boolean.FALSE), 3);
			pPlayer.addItem(new ItemStack(CEItems.RAMU_EGG.get()));
			return InteractionResult.CONSUME;
		}
		else {
			if (pPlayer.getMainHandItem().getItem() == CEItems.RAMU_EGG.get()) {
				if (pLevel.isClientSide) {
					pLevel.playSound(pPlayer, pPos, SoundEvents.GRASS_FALL, SoundSource.BLOCKS, 1.0F, 1.0F);
					return InteractionResult.SUCCESS;
				}

				pLevel.setBlock(pPos, this.getStateDefinition().any().setValue(WITH_EGG, Boolean.TRUE), 3);
				pPlayer.getMainHandItem().shrink(1);
				pLevel.playSound(pPlayer, pPos, SoundEvents.GRASS_FALL, SoundSource.BLOCKS, 1.0F, 1.0F);
				return InteractionResult.CONSUME;
			} else {
				return InteractionResult.PASS;
			}
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
		pBuilder.add(HATCH, WITH_EGG);
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return SHAPE;
	}

	@Override
	public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pSide) {
		return false;
	}
	
	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		pLevel.getEntitiesOfClass(Ramu.class, new AABB(pPos).inflate(64)).forEach(e -> {
			if (e.getNestPos() != null && e.getNestPos().equals(pPos)) {
				e.setNestPos(null);
			}
			if (!pLevel.isClientSide) {
				((ServerLevel) pLevel).getPoiManager().release(pPos);
			}
		});
		super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
	}
	
	@Override
	public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
		ArrayList<ItemStack> drops = new ArrayList<>();
		if (pState.getValue(WITH_EGG)) {
			drops.add(new ItemStack(CEItems.RAMU_EGG.get()));
		}
		drops.add(new ItemStack(CEBlocks.RAMU_NEST.get().asItem()));
		return drops;
	}
	
}