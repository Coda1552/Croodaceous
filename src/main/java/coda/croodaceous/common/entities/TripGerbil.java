package coda.croodaceous.common.entities;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.network.CENetwork;
import coda.croodaceous.common.network.ClientBoundTripGerbilPartnerPacket;
import coda.croodaceous.registry.CEBlocks;
import coda.croodaceous.registry.CEEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class TripGerbil extends Animal implements IAnimatable {

    // SYNCED DATA //
    private static final EntityDataAccessor<Optional<UUID>> DATA_PARTNER = SynchedEntityData.defineId(TripGerbil.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> DATA_LEADER = SynchedEntityData.defineId(TripGerbil.class, EntityDataSerializers.BOOLEAN);

    // TRIP ATTACK CONDITIONS //
    private static final TagKey<EntityType<?>> TRIP_GERBIL_IMMUNE = ForgeRegistries.ENTITY_TYPES.tags().createTagKey(new ResourceLocation(CroodaceousMod.MOD_ID, "trip_gerbil_immune"));
    private static final Predicate<LivingEntity> CAN_TRIP_PREDICATE = e ->
            !(e instanceof FlyingAnimal || e instanceof WaterAnimal)
            && e.getType() != CEEntities.TRIP_GERBIL.get()
            && !ForgeRegistries.ENTITY_TYPES.tags().getTag(TRIP_GERBIL_IMMUNE).contains(e.getType())
            && !e.isCrouching() && e.getDeltaMovement().horizontalDistanceSqr() > 5.2E-4F && e.hurtTime <= 0;
    private static final TargetingConditions CAN_TRIP_CONDITIONS = TargetingConditions.forCombat().ignoreInvisibilityTesting().ignoreLineOfSight();

    // PARTNER //
    public static final double MAX_PARTNER_DISTANCE = 5.0D;
    @Nullable
    private TripGerbil partner;
    /** Used on the server to indicate the partner entity needs to be loaded from the level the next tick **/
    private boolean isPartnerDirty;


    // ANIMATIONS //
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public TripGerbil(EntityType<? extends TripGerbil> type, Level worldIn) {
        super(type, worldIn);
    }

    public static AttributeSupplier.Builder createAttributes() {
        // TODO balance
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    public static boolean canSpawn(EntityType<? extends TripGerbil> entityType, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(CEBlocks.DESOLATE_SAND.get()) && level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(DATA_PARTNER, Optional.empty());
        this.getEntityData().define(DATA_LEADER, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TripGerbil.FollowLeaderGoal(this, MAX_PARTNER_DISTANCE * 0.5D, 2.0D));
        this.goalSelector.addGoal(2, new TripGerbil.LeaderPanicGoal(this, 2.0D));
        this.goalSelector.addGoal(4, new TripGerbil.LeaderWanderGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
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
        return super.getStandingEyeHeight(poseIn, sizeIn); // 0.45F;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        // sync partner to client
        if(!level.isClientSide() && this.isPartnerDirty && tickCount > 1) {
            this.isPartnerDirty = false;
            final Optional<UUID> oId = getPartnerId();
            if(oId.isPresent()) {
                this.partner = (TripGerbil) ((ServerLevel) this.level).getEntity(oId.get());
            }
            CENetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this), new ClientBoundTripGerbilPartnerPacket(this.getId(), this.partner != null ? this.partner.getId() : -1));
        }
        // attempt to trip nearby entities
        if(!level.isClientSide() && this.isLeader() && this.partner != null) {
            getEntitiesToTrip(this.position(), this.partner.position()).forEach(e -> {
                e.hurt(DamageSource.indirectMobAttack(this, this), 1.0F);
                e.setDeltaMovement(e.getDeltaMovement().multiply(0, 1, 0).add(0, 0.02D, 0));
                e.hurtMarked = true;
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 4, false, false, false));
            });
        }
    }

    @Override
    public void remove(RemovalReason pReason) {
        final TripGerbil partner = getPartner();
        if(getPartnerId().isPresent() && partner != null) {
            setPartnerId(null);
            partner.setPartnerId(null);
            partner.remove(pReason);
        }
        super.remove(pReason);
    }

    //// PARTNER ////

    @Override
    public void travel(Vec3 pTravelVector) {
        if(getPartner() != null && !this.position().closerThan(getPartner().position(), MAX_PARTNER_DISTANCE)) {
            Vec3 vec = getPartner().position().subtract(this.position()).scale(0.085D);
            this.setDeltaMovement(vec);
        }
        super.travel(pTravelVector);
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if(super.hurt(pSource, pAmount)) {
            if(this.getPartner() != null && this.getPartner().hurtTime == 0) {
                this.getPartner().hurt(pSource, pAmount);
            }
            return true;
        }
        return false;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @org.jetbrains.annotations.Nullable SpawnGroupData pSpawnData, @org.jetbrains.annotations.Nullable CompoundTag pDataTag) {
        if(!isPartnerDirty) {
            // spawn partner
            final TripGerbil partner = CEEntities.TRIP_GERBIL.get().create(pLevel.getLevel());
            partner.copyPosition(this);
            pLevel.getLevel().addFreshEntity(partner);
            // assign partner
            this.setPartnerId(partner.getUUID());
            this.setLeader(true);
            partner.setPartnerId(this.getUUID());
            // finalize partner spawn
            partner.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
        }
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        super.onSyncedDataUpdated(pKey);
    }

    public void setClientPartner(final TripGerbil entity) {
        this.partner = entity;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer pServerPlayer) {
        super.startSeenByPlayer(pServerPlayer);
        // send update packet
        int partnerId = getPartner() != null ? getPartner().getId() : -1;
        CENetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new ClientBoundTripGerbilPartnerPacket(this.getId(), partnerId));
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        if(this.isLeader()) {
            return this.getBoundingBox().inflate(MAX_PARTNER_DISTANCE * 2.0D);
        }
        return super.getBoundingBoxForCulling();
    }

    @Nullable
    public TripGerbil getPartner() {
        return this.partner;
    }

    public Optional<UUID> getPartnerId() {
        return getEntityData().get(DATA_PARTNER);
    }

    public void setPartnerId(@Nullable final UUID uuid) {
        getEntityData().set(DATA_PARTNER, Optional.ofNullable(uuid));
        this.isPartnerDirty = true;
    }

    public boolean isLeader() {
        return this.getEntityData().get(DATA_LEADER);
    }

    private void setLeader(final boolean isLeader) {
        this.getEntityData().set(DATA_LEADER, isLeader);
    }

    //// NBT ////

    private static final String KEY_PARTNER = "Partner";
    private static final String KEY_LEADER = "IsLeader";

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        setLeader(pCompound.getBoolean(KEY_LEADER));
        if(pCompound.contains(KEY_PARTNER) && level instanceof ServerLevel serverLevel) {
            // attempt to load partner by UUID
            final UUID partnerId = pCompound.getUUID(KEY_PARTNER);
            this.getEntityData().set(DATA_PARTNER, Optional.of(partnerId));
            this.isPartnerDirty = true;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean(KEY_LEADER, isLeader());
        if(getPartner() != null) {
            pCompound.putUUID(KEY_PARTNER, getPartner().getUUID());
        }
    }

    //// ANIMATIONS ////

    private PlayState animControllerMain(AnimationEvent<?> e) {
        // TODO trip gerbil animations
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

    //// TRIP ATTACK ////

    private List<LivingEntity> getEntitiesToTrip(final Vec3 first, final Vec3 second) {
        final double dY = 0.4D;
        final AABB aabb = fromCorners(first.add(0, -dY, 0), second.add(0, dY, 0));
        final TargetingConditions conditions = CAN_TRIP_CONDITIONS.copy().selector(CAN_TRIP_PREDICATE.and(e -> intersects(first, second, aabb)));
        return level.getNearbyEntities(LivingEntity.class, conditions, this, aabb);
    }

    private static AABB fromCorners(final Vec3 pFirst, final Vec3 pSecond) {
        return new AABB(Math.min(pFirst.x(), pSecond.x()), Math.min(pFirst.y(), pSecond.y()), Math.min(pFirst.z(), pSecond.z()), Math.max(pFirst.x(), pSecond.x()), Math.max(pFirst.y(), pSecond.y()), Math.max(pFirst.z(), pSecond.z()));
    }

    private static boolean intersects(final Vec3 startVec, final Vec3 endVec, final AABB aabb) {
        return aabb.contains(startVec) || aabb.clip(startVec, endVec).isPresent();
    }

    //// GOALS ////

    private static class LeaderPanicGoal extends PanicGoal {
        private final TripGerbil entity;

        public LeaderPanicGoal(TripGerbil pMob, double pSpeedModifier) {
            super(pMob, pSpeedModifier);
            this.entity = pMob;
        }

        @Override
        public boolean canUse() {
            return this.entity.isLeader() && super.canUse();
        }

        @Override
        public void start() {
            super.start();
            if(this.entity.getPartner() != null) {
                final double radius = 1.5D;
                final double dX = (this.entity.getRandom().nextDouble() - 0.5D) * 2.0D * radius;
                final double dZ = (this.entity.getRandom().nextDouble() - 0.5D) * 2.0D * radius;
                this.entity.getPartner().getNavigation().moveTo(this.posX + dX, this.posY, this.posZ + dZ, this.speedModifier);
            }
        }
    }

    private static class FollowLeaderGoal extends Goal {

        private final TripGerbil entity;
        private final double maxDistance;
        private final double speedModifier;
        private final int recalculatePathTimer = 10;

        private FollowLeaderGoal(TripGerbil entity, double maxDistance, double speedModifier) {
            this.entity = entity;
            this.maxDistance = maxDistance;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return !entity.isLeader() && entity.getPartner() != null && !entity.position().closerThan(entity.getPartner().position(), maxDistance);
        }

        @Override
        public void start() {
            if(entity.getPartner() != null) {
                entity.getNavigation().moveTo(entity.getPartner(), speedModifier);
            }
        }

        @Override
        public void tick() {
            if(entity.tickCount % recalculatePathTimer == 0 && entity.getPartner() != null) {
                entity.getNavigation().moveTo(entity.getPartner(), speedModifier);
            }
        }
    }

    /**
     * Adapted from the standard wander goal to only allow the leader to wander
     */
    private static class LeaderWanderGoal extends WaterAvoidingRandomStrollGoal {

        private final TripGerbil entity;

        public LeaderWanderGoal(TripGerbil pMob, double pSpeedModifier) {
            super(pMob, pSpeedModifier);
            this.entity = pMob;
        }

        @Override
        public boolean canUse() {
            return this.entity.isLeader() && super.canUse();
        }

        @Override
        public void start() {
            super.start();
            if(this.entity.getPartner() != null) {
                final double radius = 1.5D;
                final double dX = (this.entity.getRandom().nextDouble() - 0.5D) * 2.0D * radius;
                final double dZ = (this.entity.getRandom().nextDouble() - 0.5D) * 2.0D * radius;
                this.entity.getPartner().getNavigation().moveTo(this.wantedX + dX, this.wantedY, this.wantedZ + dZ, this.speedModifier);
            }
        }
    }
}