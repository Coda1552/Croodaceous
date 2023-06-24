package coda.croodaceous.common.entities;

import coda.croodaceous.registry.CEEntities;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
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
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
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
    private static final byte EVENT_STOP_HANGING = (byte) 10;

    // DROPPING //
    /** The number of ticks between drop attacks, not including the time spent dropping **/
    public static final int DROP_ATTACK_COOLDOWN = 24;
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
    private static final AnimationBuilder ANIM_HANGING = new AnimationBuilder().addAnimation("animation.bear_pear.hanging", ILoopType.EDefaultLoopTypes.LOOP);
    private static final AnimationBuilder ANIM_GROUNDED = new AnimationBuilder().addAnimation("animation.bear_pear.grounded", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    private static final AnimationBuilder ANIM_WALK = new AnimationBuilder().addAnimation("animation.bear_pear.walk", ILoopType.EDefaultLoopTypes.LOOP);

    // CONSTANTS //
    /** The maximum number of bear pear entities that can share the same hanging position **/
    private static final int MAX_ENTITIES_PER_BLOCK = 4;
    private static final TagKey<Block> SUPPORTS_BEAR_PEAR = BlockTags.LEAVES;
    private static final AttributeModifier DROP_ATTACK_BONUS = new AttributeModifier("Drop attack bonus", 2.0F, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public BearPear(EntityType<? extends BearPear> type, Level worldIn) {
        super(type, worldIn);
        this.lookControl = new NoResetLookControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        // TODO balance
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.5D)
                //.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.29D);
    }

    public static boolean canSpawn(EntityType<? extends BearPear> entityType, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        // only allow spawn in a valid position
        if(level.getRawBrightness(pos, 0) <= 8) {
            return false;
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
        this.goalSelector.addGoal(2, new BearPear.DropAttackGoal(this, DROP_ATTACK_COOLDOWN + (int) (1.0F / DELTA_DROPPING)));
        this.goalSelector.addGoal(4, new BearPear.MoveToLeavesGoal(this, 1.0D, MAX_DROPPING_DISTANCE, MAX_DROPPING_DISTANCE));
        this.goalSelector.addGoal(4, new BearPear.FleeGoal<>(this, Player.class, 6.0F, 1.2F, 1.4F));
        this.goalSelector.addGoal(5, new BearPear.RandomWanderGoal(this, 0.9D));
        this.goalSelector.addGoal(7, new BearPear.LookAwayFromPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new BearPear.LookAwayFromPlayerGoal(this, Jackrobat.class, 8.0F));
        this.goalSelector.addGoal(8, new BearPear.RandomLookGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(1, new BearPearTargetGoal<>(this, Player.class));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if(isAggressive()) {
            // TODO add angry sound
            return SoundEvents.OCELOT_AMBIENT;
        }
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
        return 0.125F;
    }

    @Override
    public void tick() {
        // update hanging position
        if(!level.isClientSide() && (firstTick || (tickCount + getId()) % 20 == 1)) {
            Optional<BlockPos> hangingPos = getHangingPos();
            if(hangingPos.isPresent()) {
                // calculate horizontal distance to hanging position
                final double horizontalDisSq = Vec3.atBottomCenterOf(hangingPos.get()).vectorTo(this.position()).horizontalDistanceSqr();
                final double maxHorizontalDis = 2.0D;
                // validate hanging position
                if(horizontalDisSq < (maxHorizontalDis * maxHorizontalDis) || !canHangOn(hangingPos.get())) {
                    resetHangingPos();
                    stopDropping();
                }
            }
        }
        // update position and movement
        if(isHanging()) {
            setDeltaMovement(Vec3.ZERO);
            updateHangingPosition(getHangingPos().orElse(blockPosition().above()));
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
        super.tick();
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if(droppingPercent > 0.11F && droppingPercent < 0.65F && tickCount % 2 == 0) {
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
        if(!isHanging()) {
            super.doPush(pEntity);
        }
    }

    @Override
    protected void pushEntities() {
        if(!isHanging()) {
            super.pushEntities();
        }
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        final double cullY = isDropping() ? (double)MAX_DROPPING_DISTANCE : 0.25D;
        return super.getBoundingBoxForCulling().inflate(0.5D, cullY, 0.5D);
    }

    @Override
    public void knockback(double pStrength, double pX, double pZ) {
        if(!isHanging()) {
            super.knockback(pStrength, pX, pZ);
        }
    }

    @Override
    public boolean canAttackType(EntityType<?> pType) {
        return pType != this.getType() && super.canAttackType(pType);
    }

    @Override
    public boolean canAttack(LivingEntity pTarget) {
        return this.isHanging() && super.canAttack(pTarget);
    }

    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity target) {
        if(!isHanging()) {
            return super.isWithinMeleeAttackRange(target);
        }
        double disSq = this.distanceToSqr(target.getX(), target.getEyeY(), target.getZ());
        return disSq <= this.getMeleeAttackRangeSqr(target);
    }

    @Override
    public Entity changeDimension(ServerLevel pDestination, ITeleporter teleporter) {
        resetHangingPos();
        return super.changeDimension(pDestination, teleporter);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @org.jetbrains.annotations.Nullable SpawnGroupData pSpawnData, @org.jetbrains.annotations.Nullable CompoundTag pDataTag) {
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
            case EVENT_STOP_HANGING:
                resetHangingPos();
                break;
            default:
                super.handleEntityEvent(pId);
        }
    }

    //// HANGING ////

    /**
     * Searches the blocks above the entity to find a suitable position to start hanging
     * @param position the entity position
     * @return the first valid position to start hanging, if any was found
     */
    public Optional<BlockPos> findHangingPos(final Vec3 position) {
        BlockPos.MutableBlockPos mPos = new BlockPos(position).mutable();
        for(int i = 1; i <= MAX_DROPPING_DISTANCE; i++) {
            mPos.move(Direction.UP);
            if(canHangOn(mPos)) {
                final BlockPos hangingPos = mPos.immutable();
                setHangingPos(hangingPos);
                // move to hanging position
                updateHangingPosition(hangingPos);
                return Optional.of(hangingPos);
            }
        }
        return Optional.empty();
    }

    public Optional<BlockPos> getHangingPos() {
        return getEntityData().get(DATA_HANGING_POS);
    }

    /**
     * Removes the hanging position and enables gravity
     */
    public void resetHangingPos() {
        if(!level.isClientSide()) {
            this.getEntityData().set(DATA_HANGING_POS, Optional.empty());
            this.level.broadcastEntityEvent(this, EVENT_STOP_HANGING);
        }
        this.setNoGravity(false);
        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.08D / 4.0D, 0.0D));
        this.markHurt();
    }

    /**
     * @param hangingPos the updated hanging position
     */
    public void setHangingPos(BlockPos hangingPos) {
        this.getEntityData().set(DATA_HANGING_POS, Optional.ofNullable(hangingPos));
        setNoGravity(true);
    }

    /**
     * @return true if the entity is hanging on a block
     */
    public boolean isHanging() {
        return getHangingPos().isPresent();
    }

    /**
     * @param pos the block position to test
     * @return true if a bear pear can hang under the block at the given position
     */
    public boolean canHangOn(final BlockPos pos) {
        final BlockState blockState = level.getBlockState(pos);
        // validate block
        if(!blockState.is(SUPPORTS_BEAR_PEAR)) {
            return false;
        }
        // validate space below block
        final BlockPos posBelow = pos.below(1);
        BlockState stateBelow = level.getBlockState(posBelow);
        if(stateBelow.getMaterial().blocksMotion() || !stateBelow.getFluidState().isEmpty()) {
            return false;
        }
        // validate no entity cramming
        // TODO this doesn't seem to work
        final int disY = MAX_DROPPING_DISTANCE / 2;
        final AABB aabb = new AABB(posBelow).inflate(0, disY, 0).move(0, -disY, 0);
        final List<BearPear> entityList = level.getEntitiesOfClass(BearPear.class, aabb, e -> e != this && e.isHanging());
        if(entityList.size() > MAX_ENTITIES_PER_BLOCK - 1) {
            return false;
        }
        // all checks passed
        return true;
    }

    /**
     * Moves the entity underneath the given position with an offset
     * for horizontal randomness and vertical dropping progress
     * @param hangingPos the position to hang on
     */
    protected void updateHangingPosition(final BlockPos hangingPos) {
        Vec3 hangingVec = Vec3.atBottomCenterOf(hangingPos)
                .add(getHangingOffset())
                .subtract(0, 0.125D + getBbHeight(), 0)
                .subtract(0, getDroppingDistanceOffset(1.0F), 0);
        setPosRaw(hangingVec.x(), hangingVec.y(), hangingVec.z());
    }

    /**
     * @return a random position offset to apply to the entity when hanging,
     * seeded using its ID and position
     */
    public Vec3 getHangingOffset() {
        final BlockPos pos = getHangingPos().orElse(blockPosition());
        final long seed = (getId() * 3311L + Mth.getSeed(pos));
        final float width = getBbWidth();
        final float deltaWidth = 16.0F - width;
        final float deltaHeight = 0.0125F;
        final float dx = Mth.clamp((((seed & 15L) / 15.0F) - 0.5F) * 0.5F, -deltaWidth, deltaWidth);
        final float dy = (((seed >> 4 & 15L) / 15.0F) - 1.0F) * deltaHeight;
        final float dz = Mth.clamp((((seed >> 8 & 15L) / 15.0F) - 0.5F) * 0.5F, -deltaWidth, deltaWidth);
        return new Vec3(dx, dy, dz);
    }

    //// DROPPING ////

    public boolean isDropping() {
        return getDroppingDistance().isPresent();
    }

    public OptionalInt getDroppingDistance() {
        return getEntityData().get(DATA_DROPPING_DISTANCE);
    }

    public void stopDropping() {
        droppingPercent = 0.0F;
        if(!level.isClientSide()) {
            getEntityData().set(DATA_DROPPING_DISTANCE, OptionalInt.empty());
            setAggressive(false);
        }
    }

    public void startDropping(final int distance) {
        droppingPercent = 0.0F;
        if(!level.isClientSide()) {
            setAggressive(true);
            final OptionalInt value = distance > 0 ? OptionalInt.of(distance) : OptionalInt.empty();
            getEntityData().set(DATA_DROPPING_DISTANCE, value);
            level.broadcastEntityEvent(this, EVENT_START_DROPPING);
        }
    }

    /**
     * @param partialTick the partial tick, pass 1.0 when none is available
     * @return the distance in blocks to offset the entity Y position based on the dropping progress
     */
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
        final TargetingConditions conditions = TargetingConditions.forCombat().ignoreLineOfSight().selector(e -> canAttackType(e.getType()) && canAttack(e) && isWithinMeleeAttackRange(e));
        final List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, aabb, e -> conditions.test(this, e));
        // apply attack modifier
        //this.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(DROP_ATTACK_BONUS);
        // attack each entity
        targets.forEach(this::doHurtTarget);
        // remove attack damage modifier
        //this.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(DROP_ATTACK_BONUS);
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
        } else {
            resetHangingPos();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        getHangingPos().ifPresent(blockPos -> pCompound.put(KEY_HANGING_POS, NbtUtils.writeBlockPos(blockPos)));
    }

    //// ANIMATIONS ////

    private PlayState animControllerMain(AnimationEvent<?> event) {
        if(this.isHanging()) {
            event.getController().setAnimation(ANIM_HANGING);
        } else if(this.getDeltaMovement().horizontalDistanceSqr() > 2.500000277905201E-7D) {
            event.getController().setAnimation(ANIM_WALK);
        } else {
            event.getController().setAnimation(ANIM_GROUNDED);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0F, this::animControllerMain));
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

    private static class RandomWanderGoal extends WaterAvoidingRandomStrollGoal {

        private final BearPear entity;

        public RandomWanderGoal(BearPear pMob, double pSpeedModifier) {
            super(pMob, pSpeedModifier);
            this.entity = pMob;
        }

        @Override
        public boolean canUse() {
            return !entity.isHanging() && super.canUse();
        }
    }

    private static class FleeGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {

        private final BearPear entity;

        public FleeGoal(BearPear pMob, Class<T> pEntityClassToAvoid, float pMaxDistance, double pWalkSpeedModifier, double pSprintSpeedModifier) {
            super(pMob, pEntityClassToAvoid, pMaxDistance, pWalkSpeedModifier, pSprintSpeedModifier);
            this.entity = pMob;
        }


        @Override
        public boolean canUse() {
            return !entity.isHanging() && super.canUse();
        }
    }

    private static class MoveToLeavesGoal extends MoveToBlockGoal {

        private final BearPear entity;

        /**
         * @param pMob the bear pear entity
         * @param pSpeedModifier the movement speed modifier
         * @param pSearchRange the horizontal search range
         * @param verticalSearchRange the vertical search range, automatically adjusted to only look above the entity
         */
        public MoveToLeavesGoal(BearPear pMob, double pSpeedModifier, int pSearchRange, int verticalSearchRange) {
            super(pMob, pSpeedModifier, pSearchRange, (verticalSearchRange / 2) + 1);
            this.entity = pMob;
            this.verticalSearchStart = verticalSearchRange / 2;
        }

        @Override
        public boolean canUse() {
            return !entity.isHanging() && super.canUse();
        }

        @Override
        public void tick() {
            final double acceptedDistance = this.acceptedDistance();
            final double horizontalDistanceSq = Vec3.atCenterOf(blockPos).vectorTo(this.mob.position()).horizontalDistanceSqr();
            final boolean isUnderTarget = this.mob.position().y() < blockPos.getY() && horizontalDistanceSq < (acceptedDistance * acceptedDistance);
            if(isUnderTarget) {
                final BlockPos targetPos = blockPos;
                if(entity.canHangOn(targetPos)) {
                    entity.setHangingPos(targetPos);
                    entity.updateHangingPosition(targetPos);
                    entity.setJumping(false);
                }
                stop();
                return;
            }
            super.tick();
        }

        @Override
        public double acceptedDistance() {
            return super.acceptedDistance();
        }

        @Override
        protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
            return entity.canHangOn(pPos);
        }
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
            this.cooldown = maxCooldown + (entity.getId() * 31) % 25;
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