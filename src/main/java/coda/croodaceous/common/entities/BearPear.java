package coda.croodaceous.common.entities;

import coda.croodaceous.registry.CEBlocks;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
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

public class BearPear extends Animal implements IAnimatable {

    private static final TagKey<Block> SUPPORTS_BEAR_PEAR = BlockTags.LEAVES;

    private static final EntityDataAccessor<Boolean> DATA_HANGING = SynchedEntityData.defineId(BearPear.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private BlockPos hangingPos;
    private float hangingDis0;
    private float hangingDis;

    // ANIMATIONS //
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public BearPear(EntityType<? extends BearPear> type, Level worldIn) {
        super(type, worldIn);
    }

    public static AttributeSupplier.Builder createAttributes() {
        // TODO balance
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    public static boolean canSpawn(EntityType<? extends BearPear> entityType, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        // TODO only allow spawn in a valid position
        return level.getBlockState(pos.below()).is(CEBlocks.DESOLATE_SAND.get()) && level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        getEntityData().define(DATA_HANGING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F)); // TODO look opposite of player to "hide"
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this)); // TODO only random look at night
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
        boolean isHanging = isHanging();
        // update position and movement
        if(isHanging) {
            setDeltaMovement(Vec3.ZERO);
            this.setPosRaw(this.getX(), (double) Mth.floor(this.getY()) - (double)this.getBbHeight(), this.getZ());

        }
        // update hanging position
        if(!level.isClientSide() && (tickCount + getId()) % 20 == 1) {
            if(isHanging) {
                // validate hanging position
                if(!canHangOn(hangingPos)) {
                    setHangingPos(null);
                }
            } else {
                // locate hanging position
                BlockPos position = new BlockPos(position());
                BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
                for(int i = 1; i < 8; i++) {
                    checkPos.setWithOffset(position, 0, i, 0);
                    if(canHangOn(checkPos)) {
                        setHangingPos(checkPos.immutable());
                        // move to hanging position
                        moveTo(Vec3.atBottomCenterOf(checkPos).subtract(0, 1.0F + getBbHeight(), 0));
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
    }

    //// HANGING ////

    public BlockPos getHangingPos() {
        return hangingPos;
    }

    public void setHangingPos(@Nullable BlockPos hangingPos) {
        this.hangingPos = hangingPos;
        this.setNoGravity(hangingPos != null);
        this.getEntityData().set(DATA_HANGING, hangingPos != null);
    }

    public boolean isHanging() {
        return getEntityData().get(DATA_HANGING);
    }

    public boolean canHangOn(final BlockPos pos) {
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
        return false;
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
        return super.getBoundingBoxForCulling().inflate(0, 2.0D, 0);
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
        // TODO detect hanging block, if any
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
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
        if(hangingPos != null) {
            pCompound.put(KEY_HANGING_POS, NbtUtils.writeBlockPos(getHangingPos()));
        }
    }

    //// ANIMATIONS ////

    private PlayState animControllerMain(AnimationEvent<?> e) {
        // TODO bear pear animations
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


}