package coda.croodaceous.common.entities;

import coda.croodaceous.registry.CEBlocks;
import coda.croodaceous.registry.CEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
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

public class Jackrobat extends Animal implements IAnimatable, FlyingAnimal {

    // LEADER //
    @Nullable
    private Jackrobat leader;
    private int groupSize;
    // EGG //
    private static final int EAT_EGG_COOLDOWN = 100;
    private static final float EAT_EGG_AMOUNT = 0.15F;
    private float remainingEgg;
    private int eatEggCooldown;

    // TARGET //
    private static final Predicate<LivingEntity> IS_LIYOTE_WITH_EGG = (e) -> !e.isInvulnerable() && e instanceof Liyote liyote && liyote.getMainHandItem().is(CEItems.RAMU_EGG.get());

    // ANIMATIONS //
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public Jackrobat(EntityType<? extends Jackrobat> type, Level worldIn) {
        super(type, worldIn);
        this.moveControl = new FlyingMoveControl(this, 20, false);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.FENCE, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        // TODO balance
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.FLYING_SPEED, 0.85D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    public static boolean canSpawn(EntityType<? extends Jackrobat> entityType, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(CEBlocks.DESOLATE_SAND.get()) && level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0D));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Liyote.class, 8.0F, 1.8D, 2.0D, e -> this.isHoldingEgg()));
        this.goalSelector.addGoal(3, new Jackrobat.EatingGoal(this, 1.2F, 8.0F));
        this.goalSelector.addGoal(4, new Jackrobat.WanderGoal(this, 1.0D));
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
                this.setItemInHand(InteractionHand.MAIN_HAND, liyote.getMainHandItem());
                this.remainingEgg = 1.0F;
                liyote.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
            return true;
        }
        return false;
    }

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

    @Override
    public AgeableMob getBreedOffspring(ServerLevel p_241840_1_, AgeableMob p_241840_2_) {
        return null;
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        return 0.45F;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if(!level.isClientSide() && eatEggCooldown > 0) {
            eatEggCooldown--;
        }
    }

    @Override
    public void remove(RemovalReason pReason) {
        if(getLeader() != null) {
            getLeader().removeFollower(this);
        }
        super.remove(pReason);
    }

    //// FLYING ////

    @Override
    protected PathNavigation createNavigation(Level worldIn) {
        FlyingPathNavigation flyingpathnavigator = new FlyingPathNavigation(this, worldIn);
        flyingpathnavigator.setCanOpenDoors(false);
        flyingpathnavigator.setCanFloat(true);
        flyingpathnavigator.setCanPassDoors(true);
        return flyingpathnavigator;
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader worldIn) {
        return worldIn.getBlockState(pos).isAir() ? 10.0F : 0.0F;
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean isFlying() {
        return !this.onGround/* || (this.getDeltaMovement().lengthSqr() > 0.06D)*/;
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
        entity.eatHeldEgg(EAT_EGG_AMOUNT);
        this.eatEggCooldown = EAT_EGG_COOLDOWN;
        this.playSound(SoundEvents.GENERIC_EAT, getSoundVolume() + 0.1F, getVoicePitch());
    }

    public boolean canEatEgg() {
        return this.eatEggCooldown <= 0;
    }

    //// NBT ////

    private static final String KEY_LEADER = "Leader";
    private static final String KEY_GROUP_SIZE = "GroupSize";
    private static final String KEY_EAT_EGG_COOLDOWN = "EggCooldown";

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        groupSize = pCompound.getInt(KEY_GROUP_SIZE);
        eatEggCooldown = pCompound.getInt(KEY_EAT_EGG_COOLDOWN);
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
        if(getLeader() != null) {
            pCompound.putUUID(KEY_LEADER, getLeader().getUUID());
        }
    }


    //// ANIMATIONS ////

    private PlayState animControllerMain(AnimationEvent<?> e) {
        // TODO jackrobat animations
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

    /**
     * Adapted from the standard wander goal to move Jackrobats toward their leader when too far away
     */
    private static class WanderGoal extends WaterAvoidingRandomStrollGoal {

        private final Jackrobat entity;

        public WanderGoal(Jackrobat pMob, double pSpeedModifier) {
            super(pMob, pSpeedModifier);
            this.entity = pMob;
        }

        @Override
        protected Vec3 getPosition() {
            if(this.entity.getLeader() != null && !this.entity.getLeader().position().closerThan(this.entity.position(), this.entity.getLeader().groupSize * 0.75D)) {
                BlockPos targetPos = this.entity.getLeader().getNavigation().getTargetPos();
                if(targetPos != null) {
                    double radius = this.entity.getLeader().getGroupSize() * 0.5D;
                    double dX = (this.entity.getRandom().nextDouble() - 0.5D) * 2.0D * radius;
                    double dZ = (this.entity.getRandom().nextDouble() - 0.5D) * 2.0D * radius;
                    return Vec3.atBottomCenterOf(targetPos).add(dX, 0, dZ);
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
            List<Jackrobat> list = entity.level.getEntitiesOfClass(Jackrobat.class, aabb);
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
        private LivingEntity target;

        public TargetWithLeaderGoal(Jackrobat entity) {
            super(entity, false, false);
            this.entity = entity;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if(this.entity.getTarget() == null && this.entity.getLeader() != null) {
                this.target = this.entity.getLeader().getTarget();
            }
            return this.target != null;
        }

        @Override
        public void start() {
            this.entity.setTarget(this.target);
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
            this.target = this.entity.level.getNearestEntity(Jackrobat.class, this.selector, this.entity, this.entity.getX(), this.entity.getY(), this.entity.getZ(), aabb);
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