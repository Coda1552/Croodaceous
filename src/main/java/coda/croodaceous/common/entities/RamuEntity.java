package coda.croodaceous.common.entities;

import coda.croodaceous.registry.CEBlocks;
import coda.croodaceous.common.blocks.RamuNestBlock;
import coda.croodaceous.registry.CEEntities;
import coda.croodaceous.registry.CEItems;
import coda.croodaceous.registry.CEPoiTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Optional;

public class RamuEntity extends Animal implements IAnimatable {
	private static final EntityDataAccessor<Boolean> DATA_SITTING = SynchedEntityData.defineId(RamuEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_CE = SynchedEntityData.defineId(RamuEntity.class, EntityDataSerializers.BOOLEAN);
	private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
	private boolean sitting;
	private boolean wantsSit;
	private boolean willLayEgg;
	private boolean carryingEgg;
	private int breadCooldown;
	private BlockPos nestPos;

	public RamuEntity(EntityType<? extends RamuEntity> type, Level level) {
		super(type, level);
	}
	
	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(2, new RamuBreedGoal(this, 1.0D));
		this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, false, false, this::isTarget));
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
	}
	
	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
		              .add(Attributes.MAX_HEALTH, 20)
		              .add(Attributes.MOVEMENT_SPEED, 0.25D)
		              .add(Attributes.ATTACK_DAMAGE, 4)
		              .add(Attributes.FOLLOW_RANGE, 16);
	}
	
	private boolean isTarget(LivingEntity livingEntity) {
		if (livingEntity instanceof RamuEntity) {
			return false;
		}
		if (nestPos == null) {
			return livingEntity.getMainHandItem().getItem() == CEItems.RAMU_EGG.get();
		}
		return livingEntity.getPosition(1F).distanceTo(new Vec3(nestPos.getX(), nestPos.getY(), nestPos.getZ())) < 4 || livingEntity.getMainHandItem().getItem() == CEItems.RAMU_EGG.get();
	}
	
	private PlayState animControllerMain(AnimationEvent<?> e) {
		if (e.isMoving()) {
			if (this.isSprinting()) {
				e.getController().setAnimation(new AnimationBuilder().addAnimation("animation.ramu.charge", ILoopType.EDefaultLoopTypes.LOOP));
			} else {
				e.getController().setAnimation(new AnimationBuilder().addAnimation("animation.ramu.walk", ILoopType.EDefaultLoopTypes.LOOP));
			}
		} else if (sitting) {
			e.getController().setAnimation(new AnimationBuilder().addAnimation("animation.ramu.sit", ILoopType.EDefaultLoopTypes.LOOP));
		} else {
			e.getController().setAnimation(new AnimationBuilder().addAnimation("animation.ramu.idle", ILoopType.EDefaultLoopTypes.LOOP));
		}
		return PlayState.CONTINUE;
	}

	@Override
	public void registerControllers(AnimationData data) {
		data.addAnimationController(new AnimationController<>(this, "controller", 2F, this::animControllerMain));
	}
	
	@Override
	public AnimationFactory getFactory() {
		return factory;
	}

	@Override
	public float getStepHeight() {
		return 1.1F;
	}

	@Override
	public void tick() {
		super.tick();
		if (!level.isClientSide) {
			if (nestPos != null && level.getBlockState(nestPos).getBlock() != CEBlocks.RAMU_NEST.get()) {
				nestPos = null;
			}
			if (this.isSitting()) {
				this.getNavigation().stop();
				this.goalSelector.disableControlFlag(Goal.Flag.JUMP);
				this.goalSelector.disableControlFlag(Goal.Flag.MOVE);
				sitting = true;
			} else {
				this.goalSelector.enableControlFlag(Goal.Flag.JUMP);
				this.goalSelector.enableControlFlag(Goal.Flag.MOVE);
				this.sitting = false;
			}
			if (((wantsSit() && !isSitting()) || willLayEgg) && nestPos != null && this.getTarget() == null) {
				this.getNavigation().moveTo(nestPos.getX(), nestPos.getY(), nestPos.getZ(), 1.0D);
			}
			if (!sitting && this.getTarget() != null && (this.getTarget().getMainHandItem().getItem() == CEItems.RAMU_EGG.get() || this.getTarget().getLastHurtMob() == this)) {
				this.setSprinting(true);
			} else {
				this.setSprinting(false);
			}
			if (this.getTarget() instanceof LiyoteEntity && this.getTarget().getHealth() <= 5F) {
				this.setTarget(null);
			}
			if (this.tickCount % 1200 == 0) {
				this.wantsSit = this.random.nextBoolean();
			}
			if (willLayEgg && isOnNest()) {
				if (!hasEggOnNest()) {
					this.level.setBlock(nestPos, CEBlocks.RAMU_NEST.get().defaultBlockState().setValue(RamuNestBlock.WITH_EGG, true), 3);
					willLayEgg = false;
					carryingEgg = false;
				}
			}
			if (nestPos == null) {
				findNest();
			}
			if (!sitting && !willLayEgg && nestPos != null && !hasEggOnNest()) {
				this.level.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(8F), e -> e.getItem().getItem() == CEItems.RAMU_EGG.get())
				          .stream()
				          .findFirst()
				          .ifPresent(e -> {
					          BlockPos pos = e.getOnPos();
							  this.setTarget(null);
					          this.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 1.0D);
				          });
			}
			if (!sitting && !willLayEgg && nestPos != null && this.getTarget() == null && this.nestPos.distSqr(this.getOnPos()) > 2300) {
				this.getNavigation().moveTo(nestPos.getX(), nestPos.getY(), nestPos.getZ(), 1.0D);
			}
			if (this.getTarget() != null && nestPos != null && this.getTarget().getPosition(1F).distanceTo(new Vec3(nestPos.getX(), nestPos.getY(), nestPos.getZ())) > 4 && this.getTarget().getMainHandItem().getItem() != CEItems.RAMU_EGG.get()) {
				this.setTarget(null);
			}
			if (breadCooldown > 0) {
				breadCooldown--;
			}
			this.entityData.set(DATA_SITTING, this.sitting);
			this.entityData.set(DATA_CE, this.carryingEgg);
		} else {
			this.sitting = this.entityData.get(DATA_SITTING);
			this.carryingEgg = this.entityData.get(DATA_CE);
		}
	}
	
	private boolean hasEggOnNest() {
		return this.level.getBlockState(nestPos).getValue(RamuNestBlock.WITH_EGG);
	}
	
	private void findNest() {
		PoiManager poiManager = ((ServerLevel) level).getPoiManager();
		PoiType pt = CEPoiTypes.RAMU_NEST.get();
		Optional<BlockPos> poi = poiManager.findClosest(e -> {
			assert CEPoiTypes.RAMU_NEST.getKey() != null;

			return e.is(CEPoiTypes.RAMU_NEST.getKey());
		}, this.getOnPos(), 32, PoiManager.Occupancy.HAS_SPACE);
		if (poi.isPresent()) {
			nestPos = poi.get();
			poiManager.take(e -> {
				assert CEPoiTypes.RAMU_NEST.getKey() != null;

				return e.is(CEPoiTypes.RAMU_NEST.getKey());
			}, (p, blockPos) -> true, poi.get(), 32);
			this.getNavigation().moveTo(nestPos.getX(), nestPos.getY(), nestPos.getZ(), 1.0D);
		}
	}
	
	@Override
	public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		ItemStack itemStack = pPlayer.getMainHandItem();
		if (itemStack.getItem() == Items.MELON || itemStack.getItem() == Items.PUMPKIN) {
			if (nestPos != null && !willLayEgg && breadCooldown == 0) {
				itemStack.shrink(1);
				this.setInLove(pPlayer);
				return InteractionResult.CONSUME_PARTIAL;
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void usePlayerItem(Player pPlayer, InteractionHand pHand, ItemStack pStack) {
		if (pStack.is(Items.MELON) || pStack.is(Items.PUMPKIN)) {
			pPlayer.setItemInHand(pHand, new ItemStack(Items.WATER_BUCKET));
		} else {
			super.usePlayerItem(pPlayer, pHand, pStack);
		}
	}

	@Override
	public void spawnChildFromBreeding(ServerLevel p_27564_, Animal p_27565_) {
		willLayEgg = true;
		breadCooldown = 2 * 60 * 20;
	}

	private boolean isSitting() {
		return wantsSit() && isNearNest();
	}

	private boolean wantsSit() {
		return this.getTarget() == null && wantsSit;
	}
	
	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
		/*this.nestPos = this.getOnPos().above();
		if (!level.isClientSide && pReason != MobSpawnType.SPAWN_EGG) {
			pLevel.setBlock(nestPos, CEBlocks.RAMU_NEST.get().defaultBlockState().setValue(RamuNestBlock.WITH_EGG, this.random.nextBoolean()), 3);
		}*/
		return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
	}

	@Nullable
	@Override
	public AgeableMob getBreedOffspring(ServerLevel p_146743_, AgeableMob p_146744_) {
		return CEEntities.ENTITY_RAMU.get().create(p_146743_);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag pCompound) {
		super.readAdditionalSaveData(pCompound);
		this.nestPos = new BlockPos(pCompound.getInt("NestPosX"), pCompound.getInt("NestPosY"), pCompound.getInt("NestPosZ"));
		this.sitting = pCompound.getBoolean("Sitting");
		this.breadCooldown = pCompound.getInt("BreadCooldown");
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag pCompound) {
		super.addAdditionalSaveData(pCompound);
		if (nestPos != null) {
			pCompound.putInt("NestPosX", nestPos.getX());
			pCompound.putInt("NestPosY", nestPos.getY());
			pCompound.putInt("NestPosZ", nestPos.getZ());
		}
		pCompound.putBoolean("Sitting", this.sitting);
		pCompound.putInt("BreadCooldown", this.breadCooldown);
	}
	
	private boolean isNearNest() {
		if (nestPos == null) {
			return false;
		}
		return this.getOnPos().above().distSqr(this.nestPos) < 256;
	}
	
	private boolean isOnNest() {
		if (nestPos == null) {
			return false;
		}
		return this.getOnPos().above().distSqr(this.nestPos) < 4;
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_SITTING, false);
		this.entityData.define(DATA_CE, false);
	}
	
	@Override
	public boolean hurt(DamageSource pSource, float pAmount) {
		this.sitting = false;
		return super.hurt(pSource, pAmount);
	}
	
	@Override
	public void push(Entity pEntity) {
		this.sitting = false;
		this.setLastHurtByMob(null);
		super.push(pEntity);
	}
	
	public BlockPos getNestPos() {
		return nestPos;
	}
	
	public void setNestPos(BlockPos nestPos) {
		this.nestPos = nestPos;
	}
	
	@Override
	protected void tickDeath() {
		if (!level.isClientSide && this.deathTime == 0 && nestPos != null) {
			((ServerLevel) level).getPoiManager().release(nestPos);
		}
		super.tickDeath();
	}
	
	@Override
	public boolean doHurtTarget(Entity pEntity) {
		boolean b = super.doHurtTarget(pEntity);
		if (this.isSprinting() && pEntity instanceof LivingEntity livingEntity && b) {
			float f1 = 2.5F; // Ram knockback
			livingEntity.knockback(f1, Mth.sin(this.getYRot() * ((float)Math.PI / 180F)), -Mth.cos(this.getYRot() * ((float)Math.PI / 180F)));
		}
		return b;
	}
	
	@Override
	public boolean canTakeItem(ItemStack pItemstack) {
		return pItemstack.getItem() == CEItems.RAMU_EGG.get() && !willLayEgg;
	}
	
	@Override
	public boolean wantsToPickUp(ItemStack pStack) {
		return canTakeItem(pStack);
	}

	@Override
	public void onItemPickup(ItemEntity pItem) {
		super.onItemPickup(pItem);
	}

	@Override
	public boolean canPickUpLoot() {
		return true;
	}
	
	@Override
	protected void setItemSlotAndDropWhenKilled(EquipmentSlot p_21469_, ItemStack p_21470_) {
		if (p_21470_.getItem() == CEItems.RAMU_EGG.get()) {
			this.willLayEgg = true;
			this.carryingEgg = true;
		}
	}
	
	public boolean carriesEgg() {
		return carryingEgg;
	}

	private static class RamuBreedGoal extends BreedGoal {
		public RamuBreedGoal(Animal pAnimal, double pSpeedModifier) {
			super(pAnimal, pSpeedModifier);
		}

		@Override
		protected void breed() {
			super.breed();
			this.animal.setInLoveTime(0);
		}
	}
}