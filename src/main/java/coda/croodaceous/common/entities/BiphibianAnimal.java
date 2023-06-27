package coda.croodaceous.common.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public abstract class BiphibianAnimal extends Animal implements FlyingAnimal {

    // FLYING //
    protected boolean wantsToFly;

    // NAVIGATION //
    protected final PathNavigation groundNavigation;
    protected final PathNavigation flyingNavigation;
    protected final MoveControl flyingMoveControl;
    protected final MoveControl walkingMoveControl;
    protected boolean isNavigationDirty;

    public BiphibianAnimal(EntityType<? extends BiphibianAnimal> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.FENCE, -1.0F);
        this.flyingMoveControl = createFlyingMoveControl();
        this.walkingMoveControl = createGroundMoveControl();
        this.moveControl = this.walkingMoveControl;
        this.groundNavigation = createNavigation(level);
        this.flyingNavigation = createFlyingNavigation(level);
        this.navigation = groundNavigation;
        this.wantsToFly = false;
    }

    //// BIPHIBIAN ////

    protected abstract MoveControl createFlyingMoveControl();

    protected abstract MoveControl createGroundMoveControl();

    protected abstract PathNavigation createFlyingNavigation(final Level pLevel);

    abstract double getMaxWalkingDistance();

    public boolean prefersToFly(final double x, final double y, final double z) {
        if(this.level.getBlockState(new BlockPos(x, y - 1, z)).isAir()) {
            return true;
        }
        final double maxWalkingDis = getMaxWalkingDistance();
        return this.distanceToSqr(x, y, z) > (maxWalkingDis * maxWalkingDis) || y > (this.getEyeY() + this.getStepHeight() + 0.5D);
    }

    protected boolean isSlowFalling() {
        return true;
    }

    //// METHODS ////

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.updateNavigation();
    }

    protected void updateNavigation() {
        if(this.isNavigationDirty) {
            this.isNavigationDirty = false;
            // update navigation and move control
            final BlockPos oldTargetPos = this.getNavigation().getTargetPos();
            final boolean oldInProgress = this.getNavigation().isInProgress();
            final double oldSpeedModifier = this.getNavigation().speedModifier;
            if(this.wantsToFly) {
                this.moveControl = flyingMoveControl;
                this.navigation = flyingNavigation;
            } else {
                this.moveControl = walkingMoveControl;
                this.navigation = groundNavigation;
                this.setNoGravity(false);
            }
            // update navigation path
            if(oldInProgress && oldTargetPos != null) {
                final Vec3 vec = Vec3.atBottomCenterOf(oldTargetPos);
                this.getNavigation().moveTo(vec.x(), vec.y(), vec.z(), oldSpeedModifier);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        // slow falling
        if(isSlowFalling() && this.getDeltaMovement().y() < 0.0D) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.6D, 1.0D));
        }
    }

    //// FLYING ANIMAL ////

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {}

    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        return new GroundPathNavigation(this, pLevel);
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader worldIn) {
        if(wantsToFly() && worldIn.getBlockState(pos).isAir()) {
            return 10.0F;
        }
        return super.getWalkTargetValue(pos, worldIn);
    }

    @Override
    public boolean isFlying() {
        return wantsToFly() && !isOnGround();
    }

    public boolean wantsToFly() {
        return this.wantsToFly;
    }

    public void setWantsToFly(final boolean isFlying) {
        this.isNavigationDirty |= this.wantsToFly != isFlying;
        this.wantsToFly = isFlying;
    }

    //// NBT ////

    private static final String KEY_WANTS_TO_FLY = "WantsToFly";

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.setWantsToFly(pCompound.getBoolean(KEY_WANTS_TO_FLY));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean(KEY_WANTS_TO_FLY, wantsToFly());
    }
}