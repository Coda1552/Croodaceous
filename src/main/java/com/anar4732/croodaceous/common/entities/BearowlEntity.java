package com.anar4732.croodaceous.common.entities;

import com.anar4732.croodaceous.registry.CEEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;

public class BearowlEntity extends Animal implements IAnimatable {
	private static final EntityDataAccessor<Boolean> DATA_SLEEPING = SynchedEntityData.defineId(BearowlEntity.class, EntityDataSerializers.BOOLEAN);
	private final AnimationFactory animationFactory = new AnimationFactory(this);
	private BlockPos homePos;
	private boolean sleeping;
	
	public BearowlEntity(EntityType<? extends BearowlEntity> type, Level level) {
		super(type, level);
		maxUpStep = 1.1F;
	}
	
	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, this::isTarget));
	}
	
	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
		              .add(Attributes.MAX_HEALTH, 80)
					  .add(Attributes.MOVEMENT_SPEED, 0.25D)
		              .add(Attributes.ATTACK_DAMAGE, 8)
		              .add(Attributes.FOLLOW_RANGE, 64);
	}
	
	private boolean isTarget(LivingEntity livingEntity) {
		return livingEntity.getBbWidth() < 2 && livingEntity.getBbHeight() < 2;
	}
	
	private PlayState animControllerMain(AnimationEvent<?> e) {
		// TODO: Implement animations
		// Debug only placeholder animations
		if (sleeping) { // We use "sleeping" since its synced.
			e.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.sleep", true));
		} else {
			e.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.idle", true));
		}
		return PlayState.CONTINUE;
	}
	
	@Override
	public void registerControllers(AnimationData data) {
		data.addAnimationController(new AnimationController<>(this, "controller", 2F, this::animControllerMain));
	}
	
	@Override
	public AnimationFactory getFactory() {
		return animationFactory;
	}
	
	@Override
	public void tick() {
		super.tick();
		if (!level.isClientSide) {
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
				this.setSprinting(true);
			} else if (this.getTarget() == null || sleeping) {
				this.setSprinting(false);
			}
			this.entityData.set(DATA_SLEEPING, this.sleeping); // Sync sleeping so sleep animation can work
		} else {
			this.sleeping = this.entityData.get(DATA_SLEEPING);
		}
	}

	private boolean isBearowlSleeping() {
		return wantsSleep() && isOnHomePos() || sleeping;
	}

	private boolean wantsSleep() {
		return this.level.isDay() && this.getTarget() == null && this.getLastHurtByMobTimestamp() + 300 < this.tickCount;
	}
	
	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
		this.homePos = this.getOnPos().above();
		return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
	}

	@Nullable
	@Override
	public AgeableMob getBreedOffspring(ServerLevel p_146743_, AgeableMob p_146744_) {
		return CEEntities.ENTITY_BEAROWL.get().create(p_146743_);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag pCompound) {
		super.readAdditionalSaveData(pCompound);
		this.homePos = new BlockPos(pCompound.getInt("HomePosX"), pCompound.getInt("HomePosY"), pCompound.getInt("HomePosZ"));
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag pCompound) {
		super.addAdditionalSaveData(pCompound);
		pCompound.putInt("HomePosX", this.homePos.getX());
		pCompound.putInt("HomePosY", this.homePos.getY());
		pCompound.putInt("HomePosZ", this.homePos.getZ());
	}
	
	private boolean isOnHomePos() {
		return this.getOnPos().above().distSqr(this.homePos) < 16;
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_SLEEPING, false);
	}
	
	@Override
	public boolean hurt(DamageSource pSource, float pAmount) {
		this.sleeping = false;
		return super.hurt(pSource, pAmount);
	}
	
	@Override
	public void push(Entity pEntity) {
		this.sleeping = false;
		this.setLastHurtByMob(null);
		super.push(pEntity);
	}
	
	// TODO: Custom Sounds
	
}