package coda.croodaceous.common.entities;

import coda.croodaceous.common.entities.goal.BiphibianWanderGoal;
import coda.croodaceous.registry.CEEntities;
import coda.croodaceous.registry.CEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
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
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class Jackrobat extends BiphibianAnimal implements IAnimatable {

    // LEADER //
    private static final int MAX_GROUP_SIZE = 24;
    @Nullable
    private Jackrobat leader;
    private int groupSize;
    private Jackrobat.WanderGoal wanderGoal;
    // EGG //
    private static final int EAT_EGG_COOLDOWN = 100;
    private static final float EAT_EGG_AMOUNT = 0.15F;
    private float remainingEgg;
    private int eatEggCooldown;

    // TARGET //
    private static final Predicate<LivingEntity> IS_LIYOTE_WITH_EGG = (e) -> !e.isInvulnerable() && e instanceof Liyote liyote && liyote.getEatingItem().is(CEItems.RAMU_EGG.get());

    // ANIMATIONS //
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private static final AnimationBuilder ANIM_FLY = new AnimationBuilder().addAnimation("animation.jackrobat.fly", ILoopType.EDefaultLoopTypes.LOOP);
    private static final AnimationBuilder ANIM_HOP = new AnimationBuilder().addAnimation("animation.jackrobat.hop", ILoopType.EDefaultLoopTypes.LOOP);
    private static final AnimationBuilder ANIM_IDLE = new AnimationBuilder().addAnimation("animation.jackrobat.idle", ILoopType.EDefaultLoopTypes.LOOP);

    public Jackrobat(EntityType<? extends Jackrobat> type, Level worldIn) {
        super(type, worldIn);
        this.setDropChance(EquipmentSlot.MAINHAND, 1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        // TODO balance
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FLYING_SPEED, 0.85D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    public static boolean canSpawn(EntityType<? extends Jackrobat> entityType, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(BlockTags.SAND) && level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new Jackrobat.AttackGoal(this, 1.0D));
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0D));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Liyote.class, 8.0F, 1.8D, 2.0D, e -> this.isHoldingEgg()));
        this.goalSelector.addGoal(3, new Jackrobat.EatingGoal(this, 1.2F, 8.0F));
        this.goalSelector.addGoal(4, wanderGoal = new Jackrobat.WanderGoal(this, 0.9D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(0, new Jackrobat.FindLeaderGoal(this));
        this.targetSelector.addGoal(1, new Jackrobat.TargetWithLeaderGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Liyote.class, 10, false, false, IS_LIYOTE_WITH_EGG.and(e -> !this.isHoldingEgg())));
    }

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        if(super.doHurtTarget(pEntity)) {
            if(!level.isClientSide() && !isHoldingEgg() && pEntity instanceof Liyote liyote && IS_LIYOTE_WITH_EGG.test(liyote)) {
                this.setItemInHand(InteractionHand.MAIN_HAND, liyote.getEatingItem().copy());
                liyote.setEatingItem(ItemStack.EMPTY);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canAttack(LivingEntity pTarget) {
        if(pTarget instanceof Liyote && !IS_LIYOTE_WITH_EGG.test(pTarget)) {
            return false;
        }
        return super.canAttack(pTarget);
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return 0.45F;
    }

    @Override
    protected void customServerAiStep() {
        // check for distance to leader
        if(!this.hasRemainingEgg() && canEatEgg() && this.getLeader() != null && !this.closerThan(this.getLeader(), getMaxDistanceToLeader(this.getLeader().getGroupSize()))) {
            this.wanderGoal.trigger();
        }
        // eat egg cooldown
        if(eatEggCooldown > 0) {
            eatEggCooldown--;
        }
        // check and stop flying
        if(this.getNavigation().isDone() && this.isFlying() && this.isOnGround()) {
            this.setWantsToFly(false);
            this.isNavigationDirty = true;
        }
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();
        // slow falling
        if(this.getDeltaMovement().y() < 0.0D) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.85D, 1.0D));
        }
    }

    @Override
    public void remove(RemovalReason pReason) {
        if(getLeader() != null) {
            getLeader().removeFollower(this);
        }
        super.remove(pReason);
    }

    @Override
    public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack) {
        super.setItemSlot(pSlot, pStack);
        this.remainingEgg = pStack.is(CEItems.RAMU_EGG.get()) && pSlot == EquipmentSlot.MAINHAND ? 1.0F : 0.0F;
    }

    //// ANIMAL ////

    @Override
    public boolean canBreed() {
        return false;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
        return CEEntities.JACKROBAT.get().create(pLevel);
    }

    //// BIPHIBIAN ANIMAL ////

    @Override
    public boolean prefersToFly(double x, double y, double z) {
        return super.prefersToFly(x, y, z);
    }

    @Override
    protected MoveControl createFlyingMoveControl() {
        return new FlyingMoveControl(this, 20, false);
    }

    @Override
    protected MoveControl createGroundMoveControl() {
        return new MoveControl(this);
    }

    @Override
    protected PathNavigation createFlyingNavigation(final Level pLevel) {
        final FlyingPathNavigation nav = new FlyingPathNavigation(this, pLevel);
        nav.setCanPassDoors(true);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    double getMaxWalkingDistance() {
        return 6.0F;
    }

    //// LEADER ////

    public void setLeader(@Nullable final Jackrobat entity) {
        this.leader = entity;
    }

    @Nullable
    public Jackrobat getLeader() {
        return this.leader;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public void addFollower(Jackrobat entity) {
        if(this == entity) {
            return;
        }
        entity.setLeader(this);
        this.groupSize++;
    }

    public void removeFollower(Jackrobat entity) {
        entity.setLeader(null);
        this.groupSize--;
    }

    public double getMaxDistanceToLeader(final int leaderGroupSize) {
        return 0.6D * Math.PI * leaderGroupSize;
    }

    //// EGG ////

    public boolean isHoldingEgg() {
        return getMainHandItem().is(CEItems.RAMU_EGG.get());
    }

    public boolean hasRemainingEgg() {
        return isHoldingEgg() && remainingEgg > 0;
    }

    public void eatHeldEgg(final float amount) {
        if(!isHoldingEgg()) {
            return;
        }
        // reduce remaining egg
        this.remainingEgg -= amount;
        // play sound
        this.playSound(SoundEvents.GENERIC_EAT, getSoundVolume() + 0.1F, getVoicePitch());
        // check if egg is consumed
        if(this.remainingEgg <= 0) {
            setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            playSound(SoundEvents.PLAYER_BURP, getSoundVolume() + 0.2F, getVoicePitch());
        }
    }

    public void eatOtherEgg(final Jackrobat entity) {
        if(!entity.hasRemainingEgg()) {
            return;
        }
        // send particles
        if(level instanceof ServerLevel serverLevel) {
            Vec3 look = this.calculateViewVector(this.getViewXRot(1F), this.yBodyRot);
            Vec3 pos = getPosition(1F);
            Vec3 vec = pos.add(look.x, look.y, look.z);
            ItemParticleOption particleType = new ItemParticleOption(ParticleTypes.ITEM, entity.getMainHandItem());
            serverLevel.sendParticles(particleType, vec.x, vec.y + getEyeHeight(), vec.z, 6, random.nextFloat() * 0.2D, random.nextDouble() * 0.2D, random.nextDouble() * 0.2D, 0.1D);
        }
        // eat egg
        entity.eatHeldEgg(EAT_EGG_AMOUNT);
        this.eatEggCooldown = EAT_EGG_COOLDOWN;
    }

    public boolean canEatEgg() {
        return this.eatEggCooldown <= 0;
    }

    //// SOUNDS ////

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BEE_LOOP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.BEE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BEE_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    //// NBT ////

    private static final String KEY_LEADER = "Leader";
    private static final String KEY_GROUP_SIZE = "GroupSize";
    private static final String KEY_EAT_EGG_COOLDOWN = "EggCooldown";
    private static final String KEY_REMAINING_EGG = "RemainingEgg";

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        groupSize = pCompound.getInt(KEY_GROUP_SIZE);
        eatEggCooldown = pCompound.getInt(KEY_EAT_EGG_COOLDOWN);
        remainingEgg = pCompound.getFloat(KEY_REMAINING_EGG);
        if(pCompound.contains(KEY_LEADER) && level instanceof ServerLevel serverLevel) {
            // attempt to load leader by UUID
            final UUID leaderId = pCompound.getUUID(KEY_LEADER);
            final Jackrobat leader = (Jackrobat) serverLevel.getEntity(leaderId);
            setLeader(leader);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt(KEY_GROUP_SIZE, groupSize);
        pCompound.putInt(KEY_EAT_EGG_COOLDOWN, eatEggCooldown);
        pCompound.putFloat(KEY_REMAINING_EGG, remainingEgg);
        if(getLeader() != null) {
            pCompound.putUUID(KEY_LEADER, getLeader().getUUID());
        }
    }


    //// ANIMATIONS ////

    private PlayState animControllerMain(AnimationEvent<?> e) {
        // TODO jackrobat animations
        if(!isOnGround()) {
            e.getController().setAnimation(ANIM_FLY);
        } else if(e.isMoving()) {
            e.getController().setAnimation(ANIM_HOP);
        } else {
            e.getController().setAnimation(ANIM_IDLE);
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

    //// GOALS ////

    private static class AttackGoal extends MeleeAttackGoal {

        private final Jackrobat entity;

        private AttackGoal(Jackrobat entity, final double speedModifier) {
            super(entity, speedModifier, true);
            this.entity = entity;
        }

        @Override
        public void start() {
            super.start();
            this.entity.setWantsToFly(true);
        }
    }

    /**
     * Adapted from the standard wander goal to move Jackrobats toward their leader when too far away
     * and to walk toward close targets but fly toward far targets
     */
    private static class WanderGoal extends BiphibianWanderGoal {
        private final Jackrobat entity;

        public WanderGoal(final Jackrobat entity, final double speedModifier) {
            super(entity, speedModifier, 0.42F);
            this.entity = entity;
        }

        @Override
        public boolean canUse() {
            return super.canUse();
        }

        @Override
        protected Vec3 getPosition() {
            final Jackrobat leader = this.entity.getLeader();
            if(leader != null && !leader.position().closerThan(this.entity.position(), this.entity.getMaxDistanceToLeader(leader.getGroupSize()))) {
                BlockPos targetPos = this.entity.getLeader().getNavigation().getTargetPos();
                if(targetPos != null) {
                    final Vec3 targetVec = Vec3.atBottomCenterOf(targetPos);
                    final Vec3 vec = DefaultRandomPos.getPosTowards(this.entity, 10, 7, targetVec, Mth.HALF_PI);
                    if(vec != null) {
                        return vec;
                    }
                }
            }
            return super.getPosition();
        }

    }

    /**
     * Searches for a nearby Jackrobat with the largest group size to assign as the leader of the flock
     */
    private static class FindLeaderGoal extends Goal {

        private final Jackrobat entity;
        private double range;

        private FindLeaderGoal(Jackrobat entity) {
            this.entity = entity;
            setFlags(EnumSet.noneOf(Goal.Flag.class));
        }

        @Override
        public boolean canUse() {
            LivingEntity leader = entity.getLeader();
            this.range = entity.getAttributeValue(Attributes.FOLLOW_RANGE);
            return null == leader || !leader.isAlive() || !leader.position().closerThan(entity.position(), range);
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            // remove follower
            if(entity.getLeader() != null) {
                entity.getLeader().removeFollower(this.entity);
            }
            // locate nearby entities
            AABB aabb = entity.getBoundingBox().inflate(this.range);
            List<Jackrobat> list = entity.level.getEntitiesOfClass(Jackrobat.class, aabb, e -> e != this.entity && e.getGroupSize() < MAX_GROUP_SIZE);
            // verify at least one entity was found
            if(list.isEmpty()) {
                return;
            }
            // sort by the entity with the largest group size
            list.sort(Comparator.comparingInt(Jackrobat::getGroupSize).reversed());
            // add as follower
            list.get(0).addFollower(this.entity);
        }
    }

    /**
     * Periodically checks if this entity has a leader and the leader has an attack target,
     * then updates the entity attack target to match
     */
    private static class TargetWithLeaderGoal extends TargetGoal {

        private final Jackrobat entity;

        public TargetWithLeaderGoal(Jackrobat entity) {
            super(entity, false, false);
            this.entity = entity;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if(this.entity.getTarget() == null && this.entity.getLeader() != null) {
                this.targetMob = this.entity.getLeader().getTarget();
            }
            return this.targetMob != null;
        }

        @Override
        public void start() {
            this.entity.setTarget(this.targetMob);
            super.start();
        }
    }

    private static class EatingGoal extends Goal {

        private final Jackrobat entity;
        @Nullable
        private Jackrobat target;
        private final double speedModifier;
        private final float within;
        private final TargetingConditions selector;
        private final int recalculatePathTimer = 10;

        public EatingGoal(Jackrobat pMob, double pSpeedModifier, float pWithin) {
            this.entity = pMob;
            this.speedModifier = pSpeedModifier;
            this.within = pWithin;
            this.selector = TargetingConditions.forNonCombat()
                    .selector(e -> e instanceof Jackrobat jackrobat && jackrobat.hasRemainingEgg())
                    .ignoreLineOfSight();
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            // check cooldown
            if(!this.entity.canEatEgg()) {
                return false;
            }
            // find target
            final AABB aabb = this.entity.getBoundingBox().inflate(within);
            this.target = this.entity.level.getNearestEntity(Jackrobat.class, this.selector, null, this.entity.getX(), this.entity.getY(), this.entity.getZ(), aabb);
            return this.target != null;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.entity.getNavigation().isDone()
                    && this.target != null && this.target.isAlive() && this.target.hasRemainingEgg()
                    && this.entity.position().closerThan(this.target.position(), within);
        }

        @Override
        public void start() {
            if(this.target != null) {
                this.entity.setWantsToFly(this.entity.prefersToFly(target.getX(), target.getY(), target.getZ()));
                this.entity.getNavigation().moveTo(this.target, this.speedModifier);
            }
        }

        @Override
        public void tick() {
            super.tick();
            if(this.target != null) {
                // recalculate path
                if(this.entity.tickCount % recalculatePathTimer == 0) {
                    this.entity.getNavigation().moveTo(this.target, this.speedModifier);
                }
                // look at target
                this.entity.getLookControl().setLookAt(this.target);
                // verify target has egg
                if(!this.target.hasRemainingEgg()) {
                    stop();
                    return;
                }
                // check target is within distance
                if(this.entity.position().closerThan(this.target.position(), 2.0D)) {
                    // eat egg and stop goal
                    this.entity.eatOtherEgg(this.target);
                    stop();
                    return;
                }
            }
        }

        @Override
        public void stop() {
            this.target = null;
        }
    }
}