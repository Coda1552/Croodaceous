package coda.croodaceous.common.entities;

import coda.croodaceous.registry.CEEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
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
import java.util.EnumSet;

public class Bearowl extends Animal implements IAnimatable {

	// DATA //
	private static final EntityDataAccessor<Byte> DATA_STATE = SynchedEntityData.defineId(Bearowl.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Boolean> DATA_HAS_KILLED = SynchedEntityData.defineId(Bearowl.class, EntityDataSerializers.BOOLEAN);

	// STATES //
	private static final byte STATE_IDLE = 0;
	private static final byte STATE_SWIPE_RIGHT = 1;
	private static final byte STATE_SWIPE_LEFT = 2;
	private static final byte STATE_ROAR = 3;
	private static final byte STATE_SLEEP = 4;

	// CLIENT EVENTS //
	private static final byte START_SWIPING_EVENT = 9;
	private static final byte START_ROARING_EVENT = 10;

	// ANIMATIONS //
	private static final int SWIPE_DURATION = 20;
	private static final int ROAR_DURATION = 40;
	private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
	private static final AnimationBuilder ANIM_SWIPE_RIGHT = new AnimationBuilder().addAnimation("animation.bearowl.swipe_right",  ILoopType.EDefaultLoopTypes.LOOP);
	private static final AnimationBuilder ANIM_SWIPE_LEFT = new AnimationBuilder().addAnimation("animation.bearowl.swipe_left",  ILoopType.EDefaultLoopTypes.LOOP);
	private static final AnimationBuilder ANIM_ROAR = new AnimationBuilder().addAnimation("animation.bearowl.roar", ILoopType.EDefaultLoopTypes.LOOP);
	private static final AnimationBuilder ANIM_RUN = new AnimationBuilder().addAnimation("animation.bearowl.run", ILoopType.EDefaultLoopTypes.LOOP);
	private static final AnimationBuilder ANIM_WALK = new AnimationBuilder().addAnimation("animation.bearowl.walk", ILoopType.EDefaultLoopTypes.LOOP);
	private static final AnimationBuilder ANIM_SLEEP = new AnimationBuilder().addAnimation("animation.bearowl.sleep", ILoopType.EDefaultLoopTypes.LOOP);
	private static final AnimationBuilder ANIM_IDLE = new AnimationBuilder().addAnimation("animation.bearowl.idle", ILoopType.EDefaultLoopTypes.LOOP);
	private int animationTime;

	// CONSTANTS //
	private static final int ROAR_COOLDOWN = 380;

	public Bearowl(EntityType<? extends Bearowl> type, Level level) {
		super(type, level);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new Bearowl.SleepingGoal(this));
		this.goalSelector.addGoal(2, new Bearowl.MeleeGoal(this, 1.0D));
		this.goalSelector.addGoal(2, new Bearowl.RoaringGoal(this, ROAR_COOLDOWN));
		this.goalSelector.addGoal(3, new Bearowl.StartSleepingGoal(this, 0.9D));
		this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true, false));
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Bearowl.class, 10000, false, false, e -> true));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 80)
				.add(Attributes.MOVEMENT_SPEED, 0.25D)
				.add(Attributes.ATTACK_DAMAGE, 8)
				.add(Attributes.FOLLOW_RANGE, 64)
				.add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 0.7D);
	}

	@Override
	public boolean checkSpawnRules(LevelAccessor pLevel, MobSpawnType pSpawnReason) {
		return pLevel.getBlockState(this.getOnPos().below()).getFluidState().isEmpty();
	}

	@Override
	public boolean canAttack(LivingEntity pTarget) {
		if(isSleepingState()) {
			return false;
		}
		if(pTarget instanceof Player player) {
			return !player.isCreative() && !player.isSpectator();
		}
		if(pTarget instanceof WaterAnimal) {
			return false;
		}
		return !hasKilled() && pTarget.getBbWidth() <= 2.0D && pTarget.getBbHeight() <= 2.0D && !(pTarget instanceof Bearowl);
	}

	@Override
	public boolean doHurtTarget(Entity pEntity) {
		return super.doHurtTarget(pEntity);
	}

	@Override
	protected void customServerAiStep() {
		super.customServerAiStep();
		// update home pos
		if(!hasRestriction()) {
			restrictTo(this.blockPosition(), (int) getAttributeValue(Attributes.FOLLOW_RANGE));
		}
		// update sleeping
		if (isSleepingState() && !wantsSleep()) {
			setBearowlState(STATE_IDLE);
		}
		// update last hurt by mob on first tick
		if (this.firstTick) {
			this.setLastHurtByMob(null);
		}
		// update has killed
		if (getTarget() != null && !getTarget().isAlive() && getTarget().getLastHurtByMob() != null && getTarget().getLastHurtByMob().is(this)) {
			setHasKilled(true);
		}
		// update sprinting
		if(null == getTarget()) {
			setSprinting(false);
		}
	}

	@Override
	public void tick() {
		super.tick();
		// update animation time
		if(animationTime > 0) {
			--animationTime;
			byte state = getBearowlState();
			onTickAnimation(state);
			// finish animation
			if(animationTime <= 0) {
				onFinishAnimation(state);
			}
		}
		updateSwingTime();
	}

	@Override
	public void travel(Vec3 pTravelVector) {
		if (isSprinting()) {
			float speedMod = (float) this.getMoveControl().getSpeedModifier();
			setSpeed(speedMod + 0.15F);
		}
		super.travel(pTravelVector);
	}

	private boolean wantsSleep() {
		return this.level.isDay() && this.getTarget() == null && (this.tickCount > 300 && this.getLastHurtByMobTimestamp() + 300 < this.tickCount);
	}

	@Override
	protected float tickHeadTurn(float pYRot, float pAnimStep) {
		return isSleepingState() ? 0.0F : super.tickHeadTurn(pYRot, pAnimStep);
	}

	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
		if (pReason != MobSpawnType.STRUCTURE) {
			restrictTo(this.getOnPos().above(), (int) getAttributeValue(Attributes.FOLLOW_RANGE));
		} else {
			clearRestriction();
		}
		return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
	}

	@Nullable
	@Override
	public Bearowl getBreedOffspring(ServerLevel p_146743_, AgeableMob p_146744_) {
		return CEEntities.BEAROWL.get().create(p_146743_);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_STATE, STATE_IDLE);
		this.entityData.define(DATA_HAS_KILLED, false);
	}

	@Override
	public boolean hurt(DamageSource pSource, float pAmount) {
		if(!level.isClientSide() && isSleepingState()) {
			setBearowlState(STATE_IDLE);
		}
		return super.hurt(pSource, pAmount);
	}

	@Override
	public void push(Entity pEntity) {
		if(isSleepingState()) {
			setBearowlState(STATE_IDLE);
		}
		this.setLastHurtByMob(null);
		super.push(pEntity);
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

	//// MELEE ATTACK ////

	public void performMeleeAttack(LivingEntity target) {
		final byte state = getRandom().nextBoolean() ? STATE_SWIPE_RIGHT : STATE_SWIPE_LEFT;
		setStateAndBroadcast(state, START_SWIPING_EVENT);
		animationTime = SWIPE_DURATION;
	}

	public void finishMeleeAttack() {
		final LivingEntity target = getTarget();
		if(target != null && isWithinMeleeAttackRange(target)) {
			doHurtTarget(target);
		}
	}

	//// STATE ////

	public byte getBearowlState() {
		return getEntityData().get(DATA_STATE);
	}

	public void setBearowlState(final byte state) {
		getEntityData().set(DATA_STATE, state);
	}

	public boolean isSleepingState() {
		return getBearowlState() == STATE_SLEEP;
	}

	public boolean isSwiping() {
		final byte state = getBearowlState();
		return state == STATE_SWIPE_LEFT || state == STATE_SWIPE_RIGHT;
	}

	public boolean isIdle() {
		return getBearowlState() == STATE_IDLE;
	}

	public boolean isRoaring() {
		return getBearowlState() == STATE_ROAR;
	}

	public void setStateAndBroadcast(final byte state, final byte event) {
		if(!level.isClientSide()) {
			setBearowlState(state);
			level.broadcastEntityEvent(this, event);
		}
	}

	protected void onTickAnimation(final byte state) {
		// update melee attack
		if(isSwiping() && animationTime == (SWIPE_DURATION - 10) && !level.isClientSide()) {
			finishMeleeAttack();
		}
	}

	protected void onFinishAnimation(final byte state) {
		animationTime = 0;
		if(!level.isClientSide()) {
			// update state
			setBearowlState(STATE_IDLE);
		}
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if(key == DATA_STATE && getBearowlState() == STATE_IDLE) {
			animationTime = 0;
		}
	}

	@Override
	public void handleEntityEvent(byte event) {
		switch(event) {
			case START_SWIPING_EVENT:
				animationTime = SWIPE_DURATION;
				break;
			case START_ROARING_EVENT:
				animationTime = ROAR_DURATION;
				break;
			default:
				super.handleEntityEvent(event);
		}
	}

	//// HAS KILLED ////

	public boolean hasKilled() {
		return getEntityData().get(DATA_HAS_KILLED);
	}

	public void setHasKilled(final boolean hasKilled) {
		getEntityData().set(DATA_HAS_KILLED, hasKilled);
	}

	//// ANIMATIONS ////

	private PlayState animControllerMain(AnimationEvent<?> e) {
		final byte state = getBearowlState();
		switch (state) {
			case STATE_SWIPE_LEFT:
				e.getController().setAnimation(ANIM_SWIPE_LEFT);
				break;
			case STATE_SWIPE_RIGHT:
				e.getController().setAnimation(ANIM_SWIPE_RIGHT);
				break;
			case STATE_ROAR:
				e.getController().setAnimation(ANIM_ROAR);
				break;
			case STATE_SLEEP:
				e.getController().setAnimation(ANIM_SLEEP);
				break;
			case STATE_IDLE: default:
				if (e.isMoving()) {
					if (this.isSprinting()) {
						e.getController().setAnimation(ANIM_RUN);
					} else {
						e.getController().setAnimation(ANIM_WALK);
					}
				} else {
					e.getController().setAnimation(ANIM_IDLE);
				}
				break;
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

	//// NBT ////

	private static final String KEY_STATE = "BearowlState";
	private static final String KEY_HOME_POS = "HomePos";
	private static final String KEY_HAS_KILLED = "HasKilled";

	@Override
	public void readAdditionalSaveData(CompoundTag pCompound) {
		super.readAdditionalSaveData(pCompound);
		if(pCompound.contains("Sleeping")) {
			setBearowlState(STATE_SLEEP);
		}
		if (pCompound.contains(KEY_HOME_POS)) {
			this.restrictTo(NbtUtils.readBlockPos(pCompound.getCompound(KEY_HOME_POS)), (int) getAttributeValue(Attributes.FOLLOW_RANGE));
		}
		this.setHasKilled(pCompound.getBoolean(KEY_HAS_KILLED));
	}

	@Override
	public void addAdditionalSaveData(CompoundTag pCompound) {
		super.addAdditionalSaveData(pCompound);
		if(isSleepingState()) {
			pCompound.putBoolean("Sleeping", true);
		}
		if (hasRestriction()) {
			pCompound.put(KEY_HOME_POS, NbtUtils.writeBlockPos(this.getRestrictCenter()));
		}
		pCompound.putBoolean(KEY_HAS_KILLED, hasKilled());
	}

	//// GOALS ////

	private static class StartSleepingGoal extends MoveToBlockGoal {

		private final Bearowl entity;

		private StartSleepingGoal(final Bearowl entity, final double speedModifier) {
			super(entity, speedModifier, 0, 0);
			setFlags(EnumSet.of(Flag.MOVE));
			this.entity = entity;
		}

		@Override
		public boolean canUse() {
			return entity.tickCount > 1 && entity.wantsSleep() && !entity.isSleepingState()
					&& entity.hasRestriction() && entity.getTarget() == null && findNearestBlock();
		}

		@Override
		public boolean canContinueToUse() {
			return entity.wantsSleep() && entity.getTarget() == null;
		}

		@Override
		public void tick() {
			super.tick();
			if(isReachedTarget() && entity.wantsSleep()) {
				entity.setBearowlState(STATE_SLEEP);
				entity.setHasKilled(false);
			}
		}

		@Override
		public double acceptedDistance() {
			return 4.0D;
		}

		@Override
		protected boolean findNearestBlock() {
			this.blockPos = entity.getRestrictCenter();
			return this.blockPos != BlockPos.ZERO && this.blockPos.closerThan(this.entity.blockPosition(), this.entity.getAttributeValue(Attributes.FOLLOW_RANGE));
		}

		@Override
		protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
			// no op
			return false;
		}
	}

	private static class SleepingGoal extends Goal {

		private final Bearowl entity;

		private SleepingGoal(Bearowl entity) {
			setFlags(EnumSet.allOf(Goal.Flag.class));
			this.entity = entity;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public boolean canUse() {
			return entity.isSleepingState();
		}

		@Override
		public boolean canContinueToUse() {
			return entity.isSleepingState();
		}

		@Override
		public void tick() {
			this.entity.getNavigation().stop();
			this.entity.setSprinting(false);
		}
	}

	private static class MeleeGoal extends MeleeAttackGoal {

		private final Bearowl entity;

		public MeleeGoal(Bearowl entity, double speedModifier) {
			super(entity, speedModifier, true);
			this.entity = entity;
		}

		@Override
		public boolean canUse() {
			return (entity.isIdle() || entity.isSwiping()) && super.canUse();
		}

		@Override
		public boolean canContinueToUse() {
			return (entity.isIdle() || entity.isSwiping()) && super.canContinueToUse();
		}

		@Override
		protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
			if (distToEnemySqr <= this.getAttackReachSqr(enemy) && isTimeToAttack()) {
				this.resetAttackCooldown();
				this.entity.swing(InteractionHand.MAIN_HAND);
				this.entity.performMeleeAttack(enemy);
			}
		}

		@Override
		protected void resetAttackCooldown() {
			super.resetAttackCooldown();
		}
	}

	private static class RoaringGoal extends Goal {

		private final Bearowl entity;
		private final int maxCooldown;
		private int cooldown;

		public RoaringGoal(Bearowl entity, int maxCooldown) {
			this.setFlags(EnumSet.of(Flag.MOVE));
			this.entity = entity;
			this.maxCooldown = maxCooldown;
			this.cooldown = 10;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public boolean canUse() {
			if(cooldown > 0 && --cooldown > 0) {
				return false;
			}
			return entity.getTarget() != null && entity.isIdle() && !entity.getTarget().position().closerThan(entity.position(), 16.0D);
		}

		@Override
		public boolean canContinueToUse() {
			return entity.isRoaring();
		}

		@Override
		public void start() {
			entity.setStateAndBroadcast(STATE_ROAR, START_ROARING_EVENT);
			entity.setSprinting(false);
			entity.animationTime = ROAR_DURATION;
		}

		@Override
		public void tick() {
			this.entity.getNavigation().stop();
		}

		@Override
		public void stop() {
			entity.setSprinting(true);
			cooldown = maxCooldown;
		}
	}

}