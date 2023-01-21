package coda.croodaceous.common.entities;

import coda.croodaceous.registry.CEBlocks;
import coda.croodaceous.registry.CEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
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
import java.util.EnumSet;

public class FangFly extends Animal implements IAnimatable, FlyingAnimal {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private int underWaterTicks;

    public FangFly(EntityType<? extends FangFly> type, Level worldIn) {
        super(type, worldIn);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.FENCE, -1.0F);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader worldIn) {
        return worldIn.getBlockState(pos).isAir() ? 10.0F : 0.0F;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PickupAndHurtTargetGoal());
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0D));
        this.goalSelector.addGoal(2, new FangFly.WanderGoal());
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, LivingEntity.class, false));
    }

    @Override
    protected PathNavigation createNavigation(Level worldIn) {
        FlyingPathNavigation flyingpathnavigator = new FlyingPathNavigation(this, worldIn) {
            public boolean isStableDestination(BlockPos pos) {
                return !this.level.getBlockState(pos.below()).isAir();
            }
        };
        flyingpathnavigator.setCanOpenDoors(false);
        flyingpathnavigator.setCanFloat(false);
        flyingpathnavigator.setCanPassDoors(true);
        return flyingpathnavigator;
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    public boolean canAttack(LivingEntity pTarget) {
        return !(pTarget instanceof FangFly) && pTarget.getDimensions(Pose.STANDING).height <= 0.5F && pTarget.getDimensions(Pose.STANDING).width <= 0.5F;
    }

    @Override
    public double getPassengersRidingOffset() {
        return -0.25D;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.ATTACK_DAMAGE, 1.0D).add(Attributes.MOVEMENT_SPEED, 0.2D).add(Attributes.FLYING_SPEED, 0.55D);
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
        if (this.isInWaterOrBubble()) {
            ++this.underWaterTicks;
        } else {
            this.underWaterTicks = 0;
        }

        if (this.underWaterTicks > 20) {
            this.hurt(DamageSource.DROWN, 1.0F);
        }
    }

    @Override
    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }

    @Override
    public ItemStack getPickedResult(HitResult target) {
        return new ItemStack(CEItems.FANG_FLY_SPAWN_EGG.get());
    }

    public static boolean canSpawn(EntityType<? extends FangFly> p_223316_0_, LevelAccessor p_223316_1_, MobSpawnType p_223316_2_, BlockPos p_223316_3_, RandomSource p_223316_4_) {
        return p_223316_1_.getBlockState(p_223316_3_.below()).is(CEBlocks.DESOLATE_SAND.get()) && p_223316_1_.getRawBrightness(p_223316_3_, 0) > 8;
    }

    private PlayState animControllerMain(AnimationEvent<?> e) {
        e.getController().setAnimation(new AnimationBuilder().addAnimation("animation.fang_fly.fly", ILoopType.EDefaultLoopTypes.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 2F, this::animControllerMain));
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (!getPassengers().isEmpty()) {
            ejectPassengers();
        }

        return super.hurt(pSource, pAmount);
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Override
    public boolean isFlying() {
        return true;
    }

    class WanderGoal extends Goal {

        WanderGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public boolean canUse() {
            return FangFly.this.navigation.isDone() && FangFly.this.random.nextInt(10) == 0;
        }

        public boolean canContinueToUse() {
            return FangFly.this.navigation.isInProgress();
        }

        public void start() {
            Vec3 vec3 = this.findPos();
            if (vec3 != null) {
                FangFly.this.navigation.moveTo(FangFly.this.navigation.createPath(new BlockPos(vec3), 1), 1.0D);
            }
        }

        @Nullable
        private Vec3 findPos() {
            Vec3 vec3 = FangFly.this.getViewVector(0.0F);

            Vec3 vec32 = HoverRandomPos.getPos(FangFly.this, 8, 7, vec3.x, vec3.z, ((float) Math.PI / 2F), 3, 1);
            return vec32 != null ? vec32 : AirAndWaterRandomPos.getPos(FangFly.this, 8, 4, -2, vec3.x, vec3.z, (float) Math.PI / 2F);
        }
    }

    class PickupAndHurtTargetGoal extends Goal {

        PickupAndHurtTargetGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public boolean canUse() {
            return FangFly.this.getTarget() != null && FangFly.this.getTarget().isAlive();
        }

        @Override
        public void stop() {
            FangFly.this.navigation.stop();
        }

        @Override
        public void tick() {
            super.tick();
            FangFly fly = FangFly.this;

            if (fly.getTarget() != null) {
                fly.getNavigation().moveTo(fly.getTarget(), 1.0D);

                if (fly.distanceToSqr(fly.getTarget()) <= 1.0) {
                    fly.getTarget().startRiding(fly);
                    fly.navigation.moveTo(getX(), getY() + 2.0D, getZ(), 1.0D);

                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPosition().getX(), blockPosition().getZ());

                    if (position().y() > y + 8) {
                        fly.ejectPassengers();
                    }
                }
            }
        }

    }
}