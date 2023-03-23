package coda.croodaceous.common.entities;

import coda.croodaceous.registry.CEBlocks;
import com.mojang.math.Vector4f;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class BearPear extends Animal implements IAnimatable {

    private static final EntityDataAccessor<Optional<BlockPos>> DATA_HANGING_POS = SynchedEntityData.defineId(BearPear.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    private static final TagKey<Block> SUPPORTS_BEAR_PEAR = BlockTags.LEAVES;

    public static final float DELTA_SWINGING = 0.015F;
    private float swingingAmount = 0.0F;
    private float swingingStrength = 0.0F;
    private Vec2 swingingDirection = Vec2.ZERO;

    // ANIMATIONS //
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public BearPear(EntityType<? extends BearPear> type, Level worldIn) {
        super(type, worldIn);
        this.lookControl = new NoResetLookControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        // TODO balance
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    public static boolean canSpawn(EntityType<? extends BearPear> entityType, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        // only allow spawn in a valid position
        if(level.getRawBrightness(pos, 0) <= 8) {
            return false;
        }
        for(BlockPos p : BlockPos.betweenClosed(pos, pos.above(7))) {
            if(canHangOn(level, p)) {
                return true;
            }
        }
        return true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        getEntityData().define(DATA_HANGING_POS, Optional.empty());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BearPear.AttackGoal(this, 2.0F));
        this.goalSelector.addGoal(5, new BearPear.LookAwayFromPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new BearPear.LookAwayFromPlayerGoal(this, Jackrobat.class, 8.0F));
        this.goalSelector.addGoal(6, new BearPear.RandomLookGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this).setAlertOthers());
        //this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true, false));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.OCELOT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.OCELOT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.OCELOT_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel p_241840_1_, AgeableMob p_241840_2_) {
        return null;
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return getBbHeight() * 0.125F;
    }

    @Override
    public void tick() {
        super.tick();
        Optional<BlockPos> hangingPos = getHangingPos();
        // update position and movement
        if(hangingPos.isPresent()) {
            setDeltaMovement(Vec3.ZERO);
            Vec3 hangingVec = Vec3.atBottomCenterOf(hangingPos.get()).subtract(0, 1.0F + getBbHeight(), 0);
            setPosRaw(hangingVec.x(), hangingVec.y(), hangingVec.z());
        }
        // update hanging position
        if(!level.isClientSide() && (tickCount + getId()) % 20 == 1) {
            if(hangingPos.isPresent()) {
                // validate hanging position
                if(!canHangOn(level, hangingPos.get())) {
                    setHangingPos(null);
                }
            } else {
                // locate hanging position
                findHangingPos(position());
            }
        }
        // update swinging
        this.swingingAmount = Math.max(this.swingingAmount - DELTA_SWINGING, 0);
        if(level.isClientSide() && swingingAmount < 0.1F && isHanging()) {
            // locate intersecting entity with the largest horizontal motion
            final List<Entity> entities = level.getEntitiesOfClass(Entity.class, this.getBoundingBox(), e -> e != this && e.getDeltaMovement().horizontalDistanceSqr() > 0.00002F);
            entities.sort(Comparator.comparing(e -> e.getDeltaMovement().horizontalDistanceSqr()));
            // start swinging in accordance to the given entity
            if(!entities.isEmpty()) {
                startSwinging(entities.get(entities.size() - 1));
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
    }

    //// HANGING ////

    public Optional<BlockPos> findHangingPos(final Vec3 position) {
        BlockPos blockPosition = new BlockPos(position);
        for(BlockPos pos : BlockPos.betweenClosed(blockPosition.above(1), blockPosition.above(8))) {
            if(canHangOn(level, pos)) {
                final BlockPos hangingPos = pos.immutable();
                setHangingPos(hangingPos);
                // move to hanging position
                moveTo(Vec3.atBottomCenterOf(pos).subtract(0, 1.0F + getBbHeight(), 0));
                return Optional.of(hangingPos);
            }
        }
        return Optional.empty();
    }

    public Optional<BlockPos> getHangingPos() {
        return getEntityData().get(DATA_HANGING_POS);
    }

    public void setHangingPos(@Nullable BlockPos hangingPos) {
        this.getEntityData().set(DATA_HANGING_POS, Optional.ofNullable(hangingPos));
        this.setNoGravity(hangingPos != null);
    }

    public boolean isHanging() {
        return getHangingPos().isPresent();
    }

    public static boolean canHangOn(final LevelAccessor level, final BlockPos pos) {
        final BlockState blockState = level.getBlockState(pos);
        // validate block
        if(!blockState.is(SUPPORTS_BEAR_PEAR)) {
            return false;
        }
        // validate space below block
        for(int i = 1; i <= 2; i++) {
            BlockState stateBelow = level.getBlockState(pos.below(i));
            if(stateBelow.getMaterial().blocksMotion() || !stateBelow.getFluidState().isEmpty()) {
                return false;
            }
        }
        // all checks passed
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void push(Entity pEntity) {
        // update attack target
        if ((pEntity instanceof Player entity)
                && TargetingConditions.DEFAULT.test(this, entity)
                && (this.getRandom().nextInt(!level.isDay() ? 10 : 40) == 0)) {
            this.setTarget(entity);
        }
    }

    @Override
    protected void doPush(Entity pEntity) {
        return;
    }

    @Override
    protected void pushEntities() {
        return;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return super.getBoundingBoxForCulling().inflate(0.5D, 2.0D, 0.5D);
    }

    @Override
    public void knockback(double pStrength, double pX, double pZ) {
        return;
    }

    @Override
    public Entity changeDimension(ServerLevel pDestination, ITeleporter teleporter) {
        setHangingPos(null);
        return super.changeDimension(pDestination, teleporter);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @org.jetbrains.annotations.Nullable SpawnGroupData pSpawnData, @org.jetbrains.annotations.Nullable CompoundTag pDataTag) {
        // locate hanging position
        findHangingPos(position());
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    @Override
    public int getHeadRotSpeed() {
        return 30;
    }

    //// SWINGING ////

    protected void startSwinging(final Entity entity) {
        final Vec2 deltaPos = new Vec2((float) (this.getY() - entity.getY()), (float) (this.getX() - entity.getX()));
        final float motionSq = (float) entity.getDeltaMovement().horizontalDistanceSqr();
        startSwinging(deltaPos, motionSq);
    }

    protected void startSwinging(final Vec2 deltaPos, final float motionSq) {
        final float strengthMultiplier = 8.0F;
        final float strengthSign = (isAggressive() ? -1.0F : 1.0F);
        final float strength = strengthSign * Mth.clamp((float) Mth.sqrt(motionSq) * strengthMultiplier, 0, 1.0F);
        setSwinging(strength, deltaPos.normalized());
    }

    public float getSwingingAmount() {
        return swingingAmount;
    }

    public float getSwingingStrength() {
        return swingingStrength;
    }

    public Vec2 getSwingingDirection() {
        return swingingDirection;
    }

    public void setSwinging(final float strength, final Vec2 direction) {
        this.swingingAmount = 1.0F;
        this.swingingStrength = strength;
        this.swingingDirection = direction;
    }

    public void resetSwinging() {
        this.swingingAmount = 0.0F;
        this.swingingStrength = 0.0F;
        this.swingingDirection = Vec2.ZERO;
    }

    //// NBT ////

    private static final String KEY_HANGING_POS = "HangingPos";

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if(pCompound.contains(KEY_HANGING_POS)) {
            setHangingPos(NbtUtils.readBlockPos(pCompound.getCompound(KEY_HANGING_POS)));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        getHangingPos().ifPresent(blockPos -> pCompound.put(KEY_HANGING_POS, NbtUtils.writeBlockPos(blockPos)));
    }

    //// ANIMATIONS ////

    private PlayState animControllerMain(AnimationEvent<?> e) {
        // TODO bear pear animations, if any
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

    //// LOOK CONTROL ////

    private static class NoResetLookControl extends LookControl {

        public NoResetLookControl(Mob pMob) {
            super(pMob);
        }

        @Override
        protected boolean resetXRotOnTick() {
            return false;
        }

        @Override
        public void tick() {
            this.lookAtCooldown = 1;
            super.tick();
        }
    }

    //// GOALS ////

    protected void lookAwayFrom(final Entity entity) {
        Vec3 deltaVec = entity.getEyePosition().subtract(this.getEyePosition());
        Vec3 lookAtVec = this.getEyePosition().subtract(deltaVec);
        this.getLookControl().setLookAt(lookAtVec.x(), lookAtVec.y(), lookAtVec.z());
    }

    private static class LookAwayFromPlayerGoal extends LookAtPlayerGoal {

        private final BearPear entity;

        public LookAwayFromPlayerGoal(BearPear pMob, Class<? extends LivingEntity> pLookAtType, float pLookDistance) {
            super(pMob, pLookAtType, pLookDistance, 1.0F);
            this.entity = pMob;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return this.entity.isHanging() && super.canUse();
        }

        @Override
        public void tick() {
            if(this.lookAt != null && this.lookAt.isAlive()) {
                this.entity.lookAwayFrom(this.lookAt);
                --this.lookTime;
            }
        }

        @Override
        public void start() { }

        @Override
        public void stop() {
            this.lookAt = null;
        }
    }

    private static class AttackGoal extends Goal {

        private final BearPear mob;
        private final float lookAtDistanceSq;

        public int ticksUntilNextAttack;
        private final int attackInterval = 20;
        private long lastCanUseCheck;
        private static final long COOLDOWN_BETWEEN_CAN_USE_CHECKS = 20L;

        public AttackGoal(BearPear pMob, float lookAtDistance) {
            this.mob = pMob;
            this.lookAtDistanceSq = lookAtDistance * lookAtDistance;
            setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            long i = this.mob.level.getGameTime();
            if (i - this.lastCanUseCheck < COOLDOWN_BETWEEN_CAN_USE_CHECKS) {
                return false;
            } else {
                this.lastCanUseCheck = i;
                LivingEntity livingentity = this.mob.getTarget();
                if (livingentity == null) {
                    return false;
                } else if (!livingentity.isAlive()) {
                    return false;
                } else {
                    return this.getAttackReachSqr(livingentity) >= this.mob.getEyePosition().distanceToSqr(livingentity.getX(), livingentity.getEyeY(), livingentity.getZ());
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity == null) {
                return false;
            } else if (!livingentity.isAlive()) {
                return false;
            } else {
                return !(livingentity instanceof Player) || !livingentity.isSpectator() && !((Player)livingentity).isCreative();
            }
        }

        @Override
        public void start() {
            this.mob.setAggressive(true);
            this.ticksUntilNextAttack = 0;
        }

        @Override
        public void stop() {
            LivingEntity livingentity = this.mob.getTarget();
            if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
                this.mob.setTarget(null);
            }
            this.mob.setAggressive(false);
        }

        @Override
        public void tick() {
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity != null) {
                double disSq = this.mob.distanceToSqr(livingentity.getX(), livingentity.getEyeY(), livingentity.getZ());
                if(disSq < lookAtDistanceSq) {
                    this.mob.getLookControl().setLookAt(livingentity, 180.0F, 30.0F);
                    this.mob.markHurt();
                } else {
                    this.mob.lookAwayFrom(livingentity);
                }

                this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
                this.checkAndPerformAttack(livingentity, disSq);
            }
        }

        protected void checkAndPerformAttack(LivingEntity pEnemy, double pDistToEnemySqr) {
            double d0 = this.getAttackReachSqr(pEnemy);
            if (pDistToEnemySqr <= d0 && isTimeToAttack()) {
                this.resetAttackCooldown();
                this.mob.swing(InteractionHand.MAIN_HAND);
                this.mob.doHurtTarget(pEnemy);
            }

        }

        protected void resetAttackCooldown() {
            this.ticksUntilNextAttack = this.adjustedTickDelay(attackInterval);
        }

        protected boolean isTimeToAttack() {
            return this.ticksUntilNextAttack <= 0;
        }

        protected double getAttackReachSqr(LivingEntity pAttackTarget) {
            return (this.mob.getBbWidth() * 1.5F * this.mob.getBbWidth() * 1.5F + pAttackTarget.getBbWidth());
        }
    }

    private static class RandomLookGoal extends RandomLookAroundGoal {

        private final BearPear entity;

        public RandomLookGoal(BearPear pMob) {
            super(pMob);
            this.entity = pMob;
        }

        @Override
        public boolean canUse() {
            return !this.entity.level.isDay() && this.entity.isHanging() && super.canUse();
        }
    }

}