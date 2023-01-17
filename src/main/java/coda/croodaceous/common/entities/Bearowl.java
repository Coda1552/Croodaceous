package coda.croodaceous.common.entities;

import coda.croodaceous.registry.CEEntities;
import coda.croodaceous.registry.CEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.HitResult;
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

public class Bearowl extends Animal implements IAnimatable {
	private static final EntityDataAccessor<Boolean> DATA_SLEEPING = SynchedEntityData.defineId(Bearowl.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_ROARING = SynchedEntityData.defineId(Bearowl.class, EntityDataSerializers.BOOLEAN);
	private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
	private boolean roaring;
	private int attackAnimationAttr;
	private int roarTicks;
	public boolean sleeping;
	private BlockPos homePos;

	public Bearowl(EntityType<? extends Bearowl> type, Level level) {
		super(type, level);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, this::isTarget));
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Bearowl.class, 10000, false, false, e -> true));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 80)
				.add(Attributes.MOVEMENT_SPEED, 0.25D)
				.add(Attributes.ATTACK_DAMAGE, 8)
				.add(Attributes.FOLLOW_RANGE, 64);
	}

	@Override
	public boolean checkSpawnRules(LevelAccessor pLevel, MobSpawnType pSpawnReason) {
		return pLevel.getBlockState(this.getOnPos().below()).getFluidState().isEmpty();
	}

	private boolean isTarget(LivingEntity livingEntity) {
		return livingEntity.getBbWidth() <= 2 && livingEntity.getBbHeight() <= 2 && !(livingEntity instanceof Bearowl);
	}

	private PlayState animControllerMain(AnimationEvent<?> e) {
		if (this.swingTime > 0) {
			if (attackAnimationAttr == 0) {
				e.getController().setAnimation(new AnimationBuilder().addAnimation("animation.bearowl.swipe_right",  ILoopType.EDefaultLoopTypes.LOOP));
			} else {
				e.getController().setAnimation(new AnimationBuilder().addAnimation("animation.bearowl.swipe_left",  ILoopType.EDefaultLoopTypes.LOOP));
			}
			return PlayState.CONTINUE;
		} else {
			attackAnimationAttr = random.nextInt(2);
		}

		if (this.roaring) {
			e.getController().setAnimation(new AnimationBuilder().addAnimation("animation.bearowl.roar", ILoopType.EDefaultLoopTypes.LOOP));
		} else if (e.isMoving()) {
			if (this.isSprinting()) {
				e.getController().setAnimation(new AnimationBuilder().addAnimation("animation.bearowl.run", ILoopType.EDefaultLoopTypes.LOOP));
			} else {
				e.getController().setAnimation(new AnimationBuilder().addAnimation("animation.bearowl.walk", ILoopType.EDefaultLoopTypes.LOOP));
			}
		} else if (sleeping) {
			e.getController().setAnimation(new AnimationBuilder().addAnimation("animation.bearowl.sleep", ILoopType.EDefaultLoopTypes.LOOP));
		} else {
			e.getController().setAnimation(new AnimationBuilder().addAnimation("animation.bearowl.idle", ILoopType.EDefaultLoopTypes.LOOP));
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
	public void tick() {
		super.tick();
		updateSwingTime();
		if (!level.isClientSide) {
			if (this.homePos == null) {
				this.homePos = this.blockPosition();
			}
			if (this.isBearowlSleeping()) {
				this.getNavigation().stop();
				this.goalSelector.disableControlFlag(Goal.Flag.JUMP);
				this.goalSelector.disableControlFlag(Goal.Flag.MOVE);
				this.goalSelector.disableControlFlag(Goal.Flag.LOOK);
				this.targetSelector.disableControlFlag(Goal.Flag.TARGET);
				sleeping = true;
			} else {
				this.goalSelector.enableControlFlag(Goal.Flag.JUMP);
				this.goalSelector.enableControlFlag(Goal.Flag.MOVE);
				this.goalSelector.enableControlFlag(Goal.Flag.LOOK);
				this.targetSelector.enableControlFlag(Goal.Flag.TARGET);
			}
			if (wantsSleep() && !isBearowlSleeping() && this.getTarget() == null) {
				this.getNavigation().moveTo(homePos.getX(), homePos.getY(), homePos.getZ(), 1.0D);
			}
			if (!wantsSleep()) {
				this.sleeping = false;
			}
			if (this.tickCount == 1) {
				this.setLastHurtByMob(null);
			}
			if (this.getTarget() != null && this.getTarget().distanceTo(this) > 16F) {
				if (roarTicks < 40) {
					this.roaring = true;
					this.setSprinting(false);
					this.goalSelector.disableControlFlag(Goal.Flag.MOVE);
					this.getNavigation().stop();
					roarTicks++;
				} else {
					this.roaring = false;
					this.setSprinting(true);
					this.goalSelector.enableControlFlag(Goal.Flag.MOVE);
				}
			} else if (this.getTarget() == null || sleeping) {
				this.goalSelector.enableControlFlag(Goal.Flag.MOVE);
				this.setSprinting(false);
				this.roaring = false;
				roarTicks = 0;
			}
			this.entityData.set(DATA_SLEEPING, this.sleeping);
			this.entityData.set(DATA_ROARING, this.roaring);
		} else {
			this.sleeping = this.entityData.get(DATA_SLEEPING);
			this.roaring = this.entityData.get(DATA_ROARING);
		}
	}

	@Override
	public void travel(Vec3 pTravelVector) {
		if (isSprinting()) {
			float speedMod = (float) this.getMoveControl().getSpeedModifier();
			setSpeed(speedMod + 0.15F);
		}
		super.travel(pTravelVector);
	}

	@Override
	public float getStepHeight() {
		return 1.1F;
	}

	public boolean isBearowlSleeping() {
		return wantsSleep() && isOnHomePos() || sleeping;
	}

	private boolean wantsSleep() {
		return this.level.isDay() && this.getTarget() == null && this.getLastHurtByMobTimestamp() + 300 < this.tickCount;
	}

	@Override
	protected float tickHeadTurn(float p_21538_, float p_21539_) {
		return sleeping ? 0.0F : super.tickHeadTurn(p_21538_, p_21539_);
	}

	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
		if (pReason != MobSpawnType.STRUCTURE) {
			this.homePos = this.getOnPos().above();
		}
		return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
	}

	@Nullable
	@Override
	public Bearowl getBreedOffspring(ServerLevel p_146743_, AgeableMob p_146744_) {
		return CEEntities.BEAROWL.get().create(p_146743_);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag pCompound) {
		super.readAdditionalSaveData(pCompound);
		this.homePos = new BlockPos(pCompound.getInt("HomePosX"), pCompound.getInt("HomePosY"), pCompound.getInt("HomePosZ"));
		this.sleeping = pCompound.getBoolean("Sleeping");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag pCompound) {
		super.addAdditionalSaveData(pCompound);
		if (this.homePos != null) {
			pCompound.putInt("HomePosX", this.homePos.getX());
			pCompound.putInt("HomePosY", this.homePos.getY());
			pCompound.putInt("HomePosZ", this.homePos.getZ());
		}
		pCompound.putBoolean("Sleeping", this.sleeping);
	}

	private boolean isOnHomePos() {
		return this.homePos != null && this.getOnPos().above().distSqr(this.homePos) < 16;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_SLEEPING, false);
		this.entityData.define(DATA_ROARING, false);
	}

	@Override
	public boolean hurt(DamageSource pSource, float pAmount) {
		this.sleeping = false;
		if (pSource.getEntity() instanceof Bearowl) {
			this.knockback(0.5F, pSource.getEntity().getX() - this.getX(), pSource.getEntity().getZ() - this.getZ());
			return false;
		}
		return super.hurt(pSource, pAmount);
	}

	@Override
	public void push(Entity pEntity) {
		this.sleeping = false;
		this.setLastHurtByMob(null);
		super.push(pEntity);
	}

	@Override
	public ItemStack getPickedResult(HitResult target) {
		return new ItemStack(CEItems.BEAROWL_SPAWN_EGG.get());
	}

	@Override
	public void swing(InteractionHand pHand) {
		this.swing(pHand, false);
	}

	@Override
	public int getCurrentSwingDuration() {
		return 20;
	}

	// TODO: Custom Sounds

}