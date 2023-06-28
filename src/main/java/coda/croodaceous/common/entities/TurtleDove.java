package coda.croodaceous.common.entities;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.entities.goal.BiphibianWanderGoal;
import coda.croodaceous.registry.CEEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class TurtleDove extends BiphibianAnimal implements IAnimatable {

    // ANIMAL //
    private static final TagKey<Item> IS_FOOD = ForgeRegistries.ITEMS.tags().createTagKey(new ResourceLocation(CroodaceousMod.MOD_ID, "turtle_dove_food"));
    private static final float FALL_IN_LOVE_CHANCE = 0.125F;

    // GECKOLIB //
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private static final AnimationBuilder ANIM_FLIGHT = new AnimationBuilder().addAnimation("animation.turtle_dove.flight", ILoopType.EDefaultLoopTypes.LOOP);
    private static final AnimationBuilder ANIM_CRAWL = new AnimationBuilder().addAnimation("animation.turtle_dove.crawl", ILoopType.EDefaultLoopTypes.LOOP);
    private static final AnimationBuilder ANIM_IDLE = new AnimationBuilder().addAnimation("animation.turtle_dove.idle", ILoopType.EDefaultLoopTypes.LOOP);

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
                .add(Attributes.FLYING_SPEED, 1.20D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D);
    }

    public static boolean canSpawn(EntityType<? extends TurtleDove> entityType, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if(level.getRawBrightness(pos, 0) <= 8) {
            return false;
        }
        return true;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25D, Ingredient.of(IS_FOOD), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25D));
        this.goalSelector.addGoal(7, this.wanderGoal = new BiphibianWanderGoal(this, 0.9D, 0.76D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class,8.0F));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    protected void customServerAiStep() {
        if(this.getNavigation().isDone() && this.wantsToFly()) {
            this.wanderGoal.trigger();
        }
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public int getHeadRotSpeed() {
        return 5;
    }

    @Override
    public int getMaxHeadYRot() {
        return 80;
    }

    @Override
    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pDimensions) {
        return pDimensions.height * 0.5F;
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
            if (!this.level.isClientSide && i == 0 && this.canFallInLove()) {
                this.usePlayerItem(pPlayer, pHand, itemstack);
                if(this.getRandom().nextFloat() < FALL_IN_LOVE_CHANCE) {
                    this.setInLove(pPlayer);
                } else {
                    this.level.broadcastEntityEvent(this, EntityEvent.TAMING_FAILED);
                }
                return InteractionResult.SUCCESS;
            }

            if (this.isBaby()) {
                this.usePlayerItem(pPlayer, pHand, itemstack);
                this.ageUp(getSpeedUpSecondsWhenFeeding(-i), true);
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }

            if (this.level.isClientSide) {
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
        return isBaby() ? 5.0D : 7.0D;
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

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
    }

    //// ANIMATIONS ////

    private PlayState animationPredicate(AnimationEvent<TurtleDove> event) {
        if(!isOnGround()) {
            event.getController().setAnimation(ANIM_FLIGHT);
        } else if(event.isMoving()) {
            event.getController().setAnimation(ANIM_CRAWL);
        } else {
            event.getController().setAnimation(ANIM_IDLE);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 5F, this::animationPredicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}