package coda.croodaceous.common.entities;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.entities.goal.BiphibianWanderGoal;
import coda.croodaceous.registry.CEEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class TurtleDove extends BiphibianAnimal implements GeoEntity {

    // ANIMAL //
    private static final TagKey<Item> IS_FOOD = ForgeRegistries.ITEMS.tags().createTagKey(new ResourceLocation(CroodaceousMod.MOD_ID, "turtle_dove_food"));
    private static final float FALL_IN_LOVE_CHANCE = 0.125F;
    private static final int MAX_WANDER_DISTANCE = 64;

    // GECKOLIB //
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation ANIM_FLIGHT = RawAnimation.begin().thenLoop("animation.turtle_dove.flight");
    private static final RawAnimation ANIM_CRAWL = RawAnimation.begin().thenLoop("animation.turtle_dove.crawl");
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("animation.turtle_dove.idle");
    public float yBodyRollO;
    public float yBodyRoll;

    // GOALS //
    private BiphibianWanderGoal wanderGoal;

    public TurtleDove(EntityType<? extends TurtleDove> entityType, Level level) {
        super(entityType, level);
        this.setWantsToFly(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 76.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.MOVEMENT_SPEED,0.20F)
                .add(Attributes.FLYING_SPEED, 0.49D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D);
    }

    public static boolean canSpawn(EntityType<? extends TurtleDove> entityType, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0D, Ingredient.of(IS_FOOD), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(7, this.wanderGoal = new BiphibianWanderGoal(this, 0.9D, 0.76D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class,8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    protected void customServerAiStep() {
        // trigger wander goal
        if(this.getNavigation().isDone() && this.wantsToFly()) {
            this.wanderGoal.trigger();
        }
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();
        // calculate flight animation
        if(this.level().isClientSide()) {
            this.yBodyRollO = this.yBodyRoll;
            final float deltaRoll = 0.05F;
            final float deltaYRot = Mth.wrapDegrees(yHeadRot - yBodyRot);
            final float minDeltaYRot = 7;
            final boolean rollToCenter = !isFlying() || getDeltaMovement().horizontalDistanceSqr() < 0.0002F || Mth.abs(deltaYRot) < minDeltaYRot;
            if(rollToCenter) {
                if(this.yBodyRoll > 0) {
                    this.yBodyRoll -= deltaRoll;
                } else {
                    this.yBodyRoll += deltaRoll;
                }
            } else {
                this.yBodyRoll = Mth.clamp(this.yBodyRoll + deltaRoll * Mth.sign(deltaYRot), -1.0F, 1.0F);
            }
        }
    }

    @Override
    public int getHeadRotSpeed() {
        return 5;
    }

    @Override
    public int getMaxHeadYRot() {
        return 90;
    }

    @Override
    public void setYBodyRot(float pOffset) {
        super.setYBodyRot(pOffset);
    }

    @Override
    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pDimensions) {
        return pDimensions.height * 0.5F;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        final SpawnGroupData data = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
        // update home position
        if(!this.hasRestriction()) {
            BlockPos.MutableBlockPos terrainHeight = blockPosition().mutable();
            while(this.level().getBlockState(terrainHeight).blocksMotion() && !this.level().isOutsideBuildHeight(terrainHeight.move(Direction.DOWN)));
            this.restrictTo(terrainHeight, MAX_WANDER_DISTANCE);
        }
        return data;
    }

    //// ANIMAL ////

    @Override
    public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
        return CEEntities.TURTLE_DOVE.get().create(pLevel);
    }

    @Override
    public boolean isFood(ItemStack pStack) {
        return pStack.is(IS_FOOD);
    }

    @Override
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (this.isFood(itemstack)) {
            int i = this.getAge();
            if (!this.level().isClientSide && i == 0 && this.canFallInLove()) {
                this.usePlayerItem(pPlayer, pHand, itemstack);
                if(this.getRandom().nextFloat() < FALL_IN_LOVE_CHANCE) {
                    this.setInLove(pPlayer);
                } else {
                    this.level().broadcastEntityEvent(this, EntityEvent.TAMING_FAILED);
                }
                return InteractionResult.SUCCESS;
            }

            if (this.isBaby()) {
                this.usePlayerItem(pPlayer, pHand, itemstack);
                this.ageUp(getSpeedUpSecondsWhenFeeding(-i), true);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (this.level().isClientSide) {
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    //// BIPHIBIAN ANIMAL ////

    @Override
    protected MoveControl createFlyingMoveControl() {
        return new FlyingMoveControl(this, 4, false);
    }

    @Override
    protected MoveControl createGroundMoveControl() {
        return new MoveControl(this);
    }

    @Override
    double getMaxWalkingDistance() {
        return isBaby() ? 8.0D : 7.0D;
    }

    @Override
    protected PathNavigation createFlyingNavigation(final Level pLevel) {
        final FlyingPathNavigation nav = new FlyingPathNavigation(this, pLevel);
        nav.setCanPassDoors(false);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(false);
        return nav;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return true;
    }

    //// SOUNDS ////

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.TURTLE_AMBIENT_LAND;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.TURTLE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.TURTLE_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return super.getSoundVolume();
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() - 0.1F;
    }

    //// NBT ////

    private static final String KEY_RESTRICTION = "Restriction";
    private static final String KEY_WANDER_DISTANCE = "WanderDistance";

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if(pCompound.contains(KEY_RESTRICTION)) {
            final BlockPos restrictCenter = NbtUtils.readBlockPos(pCompound.getCompound(KEY_RESTRICTION));
            final int restrictDistance = pCompound.getInt(KEY_WANDER_DISTANCE);
            restrictTo(restrictCenter, restrictDistance);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        if(hasRestriction()) {
            pCompound.put(KEY_RESTRICTION, NbtUtils.writeBlockPos(getRestrictCenter()));
            pCompound.putInt(KEY_WANDER_DISTANCE, (int) getRestrictRadius());
        }
    }

    //// ANIMATIONS ////

    private PlayState animationPredicate(AnimationState<TurtleDove> event) {
        final boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 0.0004F;
        if(!onGround() && Math.abs(this.getDeltaMovement().y()) > 0.0002F) {
            event.getController().setAnimation(ANIM_FLIGHT);
        } else if(isMoving) {
            event.getController().setAnimation(ANIM_CRAWL);
        } else {
            event.getController().setAnimation(ANIM_IDLE);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 4, this::animationPredicate));

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}