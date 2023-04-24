package coda.croodaceous.common.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
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
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
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
import java.util.OptionalInt;

public class BearPear extends Animal implements IAnimatable {

    private static final EntityDataAccessor<Optional<BlockPos>> DATA_HANGING_POS = SynchedEntityData.defineId(BearPear.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<OptionalInt> DATA_DROPPING_DISTANCE = SynchedEntityData.defineId(BearPear.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);

    // EVENTS //
    private static final byte EVENT_START_DROPPING = (byte) 9;

    // DROPPING //
    /** The number of ticks between drop attacks, not including the time spent dropping **/
    public static final int DROP_ATTACK_COOLDOWN = 30;
    /** The maximum number of blocks to attempt to drop **/
    public static final int MAX_DROPPING_DISTANCE = 8;
    /** The percent of drop attack to add each tick **/
    public static final float DELTA_DROPPING = 0.04F;
    /** The current drop attack progress from 0 to 1 **/
    private float droppingPercent = 0.0F;

    // SWINGING //
    /** The percent of swinging amount to subtract each tick **/
    public static final float DELTA_SWINGING = 0.0185F;
    /** The current swing progress from 0 to 1 **/
    private float swingingAmount = 0.0F;
    /** The size of the swing angle from 0 to 1 **/
    private float swingingStrength = 0.0F;
    /** The x and z components of the swing direction **/
    private Vec2 swingingDirection = Vec2.ZERO;

    // ANIMATIONS //
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    // CONSTANTS //
    private static final TagKey<Block> SUPPORTS_BEAR_PEAR = BlockTags.LEAVES;

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
        getEntityData().define(DATA_DROPPING_DISTANCE, OptionalInt.empty());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BearPear.DropAttackGoal(this, DROP_ATTACK_COOLDOWN + (int) (1.0F / DELTA_DROPPING)));
        this.goalSelector.addGoal(2, new BearPear.AttackGoal(this, 2.0F));
        this.goalSelector.addGoal(5, new BearPear.LookAwayFromPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new BearPear.LookAwayFromPlayerGoal(this, Jackrobat.class, 8.0F));
        this.goalSelector.addGoal(6, new BearPear.RandomLookGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(1, new BearPearTargetGoal<>(this, Player.class));
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
            Vec3 hangingVec = Vec3.atBottomCenterOf(hangingPos.get())
                    .subtract(0, 0.125D + getBbHeight(), 0)
                    .subtract(0, getDroppingDistanceOffset(1.0F), 0);
            setPosRaw(hangingVec.x(), hangingVec.y(), hangingVec.z());
        }
        // update hanging position
        if(!level.isClientSide() && (tickCount + getId()) % 20 == 1) {
            if(hangingPos.isPresent()) {
                // validate hanging position
                if(!canHangOn(level, hangingPos.get())) {
                    setHangingPos(null);
                    stopDropping();
                }
            } else {
                // locate hanging position
                findHangingPos(position());
            }
        }
        // update dropping
        if(isDropping()) {
            droppingPercent = Math.min(droppingPercent + DELTA_DROPPING, 1.0F);
            if(!level.isClientSide() && droppingPercent >= 1.0F) {
                stopDropping();
            }
        }
        // update swinging
        this.swingingAmount = Math.max(this.swingingAmount - DELTA_SWINGING, 0);
        if(level.isClientSide() && swingingAmount < 0.1F && isHanging() && !isDropping()) {
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
        if(droppingPercent > 0.0F && droppingPercent < 0.65F && tickCount % 3 == 0) {
            performDropAttack();
        }
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
        final double cullY = isDropping() ? 8.0D : 0.25D;
        return super.getBoundingBoxForCulling().inflate(0.5D, cullY, 0.5D);
    }

    @Override
    public void knockback(double pStrength, double pX, double pZ) {
        if(!isHanging()) {
            super.knockback(pStrength, pX, pZ);
        }
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

    @Override
    public void handleEntityEvent(byte pId) {
        switch(pId) {
            case EVENT_START_DROPPING:
                droppingPercent = 0.0F;
                swingingAmount = 0.0F;
                break;
            default:
                super.handleEntityEvent(pId);
        }
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
        BlockState stateBelow = level.getBlockState(pos.below(1));
        if(stateBelow.getMaterial().blocksMotion() || !stateBelow.getFluidState().isEmpty()) {
            return false;
        }
        // all checks passed
        return true;
    }

    //// DROPPING ////

    public boolean isDropping() {
        return getDroppingDistance().isPresent();
    }

    public OptionalInt getDroppingDistance() {
        return getEntityData().get(DATA_DROPPING_DISTANCE);
    }

    public void stopDropping() {
        getEntityData().set(DATA_DROPPING_DISTANCE, OptionalInt.empty());
        droppingPercent = 0.0F;
    }

    public void startDropping(final int distance) {
        droppingPercent = 0.0F;
        if(!level.isClientSide()) {
            final OptionalInt value = distance > 0 ? OptionalInt.of(distance) : OptionalInt.empty();
            getEntityData().set(DATA_DROPPING_DISTANCE, value);
            level.broadcastEntityEvent(this, EVENT_START_DROPPING);
        }
    }

    public double getDroppingDistanceOffset(final float partialTick) {
        final int targetDistance = getEntityData().get(DATA_DROPPING_DISTANCE).orElse(0);
        final float lerpedProgress = Mth.lerp(partialTick * DELTA_DROPPING, Math.max(0, droppingPercent - DELTA_DROPPING), droppingPercent);
        final double lerpedDistance = Mth.sin(lerpedProgress * (float) Math.PI) * targetDistance;
        return lerpedDistance;
    }

    /**
     * @param target the target to attempt to drop attack
     * @return the distance to drop, or -1 if the target is out of range
     */
    public int calculateDropAttackDistance(final LivingEntity target) {
        final Vec3 position = this.position();
        final Vec3 targetPosition = target.getEyePosition();
        // verify horizontal distance
        if(position.vectorTo(targetPosition).horizontalDistanceSqr() > this.getMeleeAttackRangeSqr(target)) {
            return -1;
        }
        // determine vertical distance
        final double deltaY = position.y() - targetPosition.y();
        if(deltaY < 1.0D || deltaY > MAX_DROPPING_DISTANCE) {
            return -1;
        }
        // determine dropping distance
        final int droppingDistance = (int) Math.ceil(deltaY);
        // verify passable blocks
        BlockPos.MutableBlockPos checkPos = blockPosition().mutable();
        for(int y = 0; y < droppingDistance; y++) {
            BlockState block = level.getBlockState(checkPos.move(Direction.DOWN));
            BlockPathTypes pathTypes = block.getBlockPathType(level, checkPos, this);
            if(block.getMaterial().blocksMotion() || (pathTypes != null && pathTypes.getDanger() != null)) {
                return -1;
            }
        }
        // all checks passed
        return droppingDistance;
    }

    protected void performDropAttack() {
        // collect intersecting entities
        final AABB aabb = getBoundingBox().inflate(2.0D);
        final TargetingConditions conditions = TargetingConditions.forCombat().ignoreLineOfSight().selector(this::isWithinMeleeAttackRange);
        final List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, aabb, e -> conditions.test(this, e));
        targets.forEach(this::doHurtTarget);
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
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
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

    private static class DropAttackGoal extends Goal {

        private final BearPear entity;
        private int droppingDistance;

        private final int maxCooldown;
        private int cooldown;

        public DropAttackGoal(BearPear pMob, int maxCooldown) {
            this.entity = pMob;
            this.maxCooldown = maxCooldown;
            this.cooldown = maxCooldown / 2;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
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
            if(!entity.isHanging() || entity.isDropping()) {
                return false;
            }
            if(null == this.entity.getTarget()) {
                return false;
            }
            // determine dropping distance
            droppingDistance = entity.calculateDropAttackDistance(entity.getTarget());
            return true;
        }

        @Override
        public void start() {
            this.cooldown = maxCooldown;
            this.entity.startDropping(droppingDistance);
            this.droppingDistance = 0;
            if(this.entity.getTarget() != null) {
                this.entity.getLookControl().setLookAt(this.entity.getTarget());
                this.entity.markHurt();
            }
        }

        @Override
        public boolean canContinueToUse() {
            return false;
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

    private static class BearPearTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {

        public BearPearTargetGoal(BearPear pMob, Class<T> pTargetType) {
            super(pMob, pTargetType, 10, true, false, e -> pMob.calculateDropAttackDistance(e) > 0);
        }

        @Override
        public boolean canUse() {
            return !mob.level.isDay() && super.canUse();
        }
    }

}