package coda.croodaceous.common.entities;

import coda.croodaceous.common.entities.goal.StealItemFromPlayerGoal;
import coda.croodaceous.registry.CEBlocks;
import coda.croodaceous.registry.CEEntities;
import coda.croodaceous.registry.CEItems;
import coda.croodaceous.registry.CEPoiTypes;
import coda.croodaceous.common.blocks.RamuNestBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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

import java.util.UUID;
import java.util.function.Predicate;

public class Liyote extends Wolf implements IAnimatable {
	private static final EntityDataAccessor<ItemStack> DATA_EI = SynchedEntityData.defineId(Liyote.class, EntityDataSerializers.ITEM_STACK);
	private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
	private static final AnimationBuilder ANIM_WALK_EAT = new AnimationBuilder().addAnimation("animation.liyote.walk_eat", ILoopType.EDefaultLoopTypes.LOOP);
	private static final AnimationBuilder ANIM_SITTING_EAT = new AnimationBuilder().addAnimation("animation.liyote.sitting_eat", ILoopType.EDefaultLoopTypes.LOOP);
	private static final AnimationBuilder ANIM_IDLE_EAT = new AnimationBuilder().addAnimation("animation.liyote.idle_eat", ILoopType.EDefaultLoopTypes.LOOP);
	private static final AnimationBuilder ANIM_WALK = new AnimationBuilder().addAnimation("animation.liyote.walk", ILoopType.EDefaultLoopTypes.LOOP);
	private static final AnimationBuilder ANIM_SITTING = new AnimationBuilder().addAnimation("animation.liyote.sitting", ILoopType.EDefaultLoopTypes.LOOP);
	private static final AnimationBuilder ANIM_IDLE = new AnimationBuilder().addAnimation("animation.liyote.idle", ILoopType.EDefaultLoopTypes.LOOP);

	private int eatingTicks = 0;
	private ItemStack eatingItem = ItemStack.EMPTY;
	private BlockPos targetNest;

	private static final Predicate<ItemStack> IS_RAMU_EGG = itemStack -> itemStack.is(CEItems.RAMU_EGG.get());

	public Liyote(EntityType<? extends Liyote> type, Level level) {
		super(type, level);
		this.setCanPickUpLoot(true);
		this.setDropChance(EquipmentSlot.MAINHAND, 1F);
	}

	@Override
	public void lookAt(Entity pEntity, float pMaxYawIncrease, float pMaxPitchIncrease) {
		super.lookAt(pEntity, pMaxYawIncrease, pMaxPitchIncrease);
	}
	
	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(11, new AvoidEntityGoal<>(this, Player.class, 6.0F, 1.0D, 1.2D) {
			@Override
			public boolean canUse() {
				return super.canUse() && !((Liyote) mob).isTame();
			}
		});
		this.goalSelector.addGoal(12, new AvoidEntityGoal<>(this, Ramu.class, 10.0F, 1.0D, 1.2D) {
			@Override
			public boolean canUse() {
				return IS_RAMU_EGG.test(eatingItem) && super.canUse();
			}
		});
		this.goalSelector.addGoal(13, new StealItemFromPlayerGoal(this, 100));
		super.registerGoals();
		this.targetSelector.removeAllGoals();
	}
	
	@Override
	public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
		ItemStack itemstack = pPlayer.getItemInHand(pHand);
		Item item = itemstack.getItem();
		final boolean isFood = isFood(itemstack);
		final boolean hasItem = !this.eatingItem.isEmpty();
		// take food from player
		if (this.isTame()) {
			// take any item from owner
			if(!itemstack.isEmpty() && !hasItem) {
				this.eatingItem = itemstack.split(1);
				return InteractionResult.SUCCESS;
			}
			// drop eating item when given food or ramu egg by owner
			if (isFood && hasItem && isOwnedBy(pPlayer)) {
				spawnAtLocation(this.eatingItem.copy(), 0.25F);
				this.eatingItem = itemstack.split(1);
				return InteractionResult.SUCCESS;
			}

			// toggle sitting behavior
			InteractionResult interactionresult = super.mobInteract(pPlayer, pHand);
			if ((!interactionresult.consumesAction()) && this.isOwnedBy(pPlayer)) {
				this.setOrderedToSit(!this.isOrderedToSit());
				this.jumping = false;
				this.navigation.stop();
				this.setTarget(null);
				return InteractionResult.SUCCESS;
			}

			return interactionresult;
		} else if (IS_RAMU_EGG.test(itemstack) && !this.isAngry()) {
			// attempt to tame when given a ramu egg
			if (!pPlayer.getAbilities().instabuild) {
				itemstack.shrink(1);
			}
			
			if (this.random.nextInt(2) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, pPlayer)) {
				this.tame(pPlayer);
				this.navigation.stop();
				this.setTarget(null);
				this.setOrderedToSit(true);
				this.level.broadcastEntityEvent(this, EntityEvent.TAMING_SUCCEEDED);
			} else {
				this.level.broadcastEntityEvent(this, EntityEvent.TAMING_FAILED);
			}
			
			return InteractionResult.SUCCESS;
		}

		if ((isFood || IS_RAMU_EGG.test(itemstack)) && !hasItem) {
			this.eatingItem = itemstack.split(1);
			return InteractionResult.SUCCESS;
		}

		if (item instanceof DyeItem || item == Items.BONE) {
			return InteractionResult.PASS;
		}
		
		return super.mobInteract(pPlayer, pHand);
	}

	public static boolean canSpawn(EntityType<? extends Liyote> p_223316_0_, LevelAccessor p_223316_1_, MobSpawnType p_223316_2_, BlockPos p_223316_3_, RandomSource p_223316_4_) {
		return p_223316_1_.getBlockState(p_223316_3_.below()).is(BlockTags.SAND) && p_223316_1_.getRawBrightness(p_223316_3_, 0) > 8;
	}

	private PlayState animControllerMain(AnimationEvent<?> e) {
		if (!eatingItem.isEmpty() && this.isFood(eatingItem)) {
			if (e.isMoving()) {
				e.getController().setAnimation(ANIM_WALK_EAT);
			} else if (isInSittingPose()) {
				e.getController().setAnimation(ANIM_SITTING_EAT);
			} else {
				e.getController().setAnimation(ANIM_IDLE_EAT);
			}
		} else if (e.isMoving()) {
			e.getController().setAnimation(ANIM_WALK);
		} else if (isInSittingPose()) {
			e.getController().setAnimation(ANIM_SITTING);
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

	@Override
	public void tick() {
		super.tick();
		if (!eatingItem.isEmpty() && this.isFood(eatingItem)) {
			eatingTicks++;
			if (eatingTicks % 5 == 0 && level.isClientSide) {
				Vec3 look = getRenderViewVector();
				Vec3 pos = getPosition(1F);
				Vec3 vec = pos.add(look.x, look.y, look.z);
				ItemParticleOption type = new ItemParticleOption(ParticleTypes.ITEM, eatingItem);
				for (int i = 0; i < 6; i++) {
					level.addParticle(type, true, vec.x, vec.y + 0.4F, vec.z, (-0.2F + random.nextFloat() / 2.5) * 0.4F, random.nextFloat() / 5, (-0.2F + random.nextFloat() / 2.5) * 0.4F);
				}
			}
			if (eatingTicks % 5 == 0) {
				playSound(SoundEvents.GENERIC_EAT, 8F, 1F);
			}
			if (eatingTicks >= 20 * 3) {
				eatingItem = ItemStack.EMPTY;
				eatingTicks = 0;
				this.level.broadcastEntityEvent(this, EntityEvent.IN_LOVE_HEARTS);
				heal(4);
			}
		}
		if (!this.level.isClientSide() && !this.isTame() && !this.isInSittingPose() && eatingItem.isEmpty() && this.getHealth() > 5F && this.random.nextInt(1200) == 0) {
			PoiManager poiManager = ((ServerLevel) level).getPoiManager();
			poiManager.findClosest(p -> {
				assert CEPoiTypes.RAMU_NEST.getKey() != null;

				return p.is(CEPoiTypes.RAMU_NEST.getKey());
			}, p -> this.level.getBlockState(p).getValue(RamuNestBlock.WITH_EGG), this.getOnPos(), 32, PoiManager.Occupancy.ANY).ifPresent(p -> {
				targetNest = p;
			});
		}
		if (targetNest != null && !this.isInSittingPose()) {
			if (canReachTargetNest()) {
				this.level.setBlock(targetNest, CEBlocks.RAMU_NEST.get().defaultBlockState().setValue(RamuNestBlock.WITH_EGG, false), 3);
				this.targetNest = null;
				this.eatingItem = new ItemStack(CEItems.RAMU_EGG.get());
			} else {
				this.getNavigation().moveTo(targetNest.getX(), targetNest.getY(), targetNest.getZ(), 1.0D);
			}
		}
		if (this.level.isClientSide) {
			eatingItem = this.entityData.get(DATA_EI);
		} else {
			this.entityData.set(DATA_EI, eatingItem);
		}
	}
	
	public ItemStack getEatingItem() {
		return eatingItem;
	}

	public void setEatingItem(final ItemStack item) {
		this.eatingItem = item;
	}
	
	@Override
	public ItemStack getItemBySlot(EquipmentSlot pSlot) {
		if (pSlot == EquipmentSlot.MAINHAND) {
			return this.getEatingItem();
		}
		return super.getItemBySlot(pSlot);
	}
	
	public final Vec3 getRenderViewVector() {
		return this.calculateViewVector(this.getViewXRot(1F), this.yBodyRot);
	}

	@Override
	public void tame(Player pPlayer) {
		super.tame(pPlayer);
		this.targetNest = null;
	}

	@Override
	public Wolf getBreedOffspring(ServerLevel p_149088_, AgeableMob p_149089_) {
		Liyote liyote = new Liyote(CEEntities.LIYOTE.get(), level);
		UUID uuid = this.getOwnerUUID();
		if (uuid != null) {
			liyote.setOwnerUUID(uuid);
			liyote.setTame(true);
		}
		return liyote;
	}
	
	@Override
	public boolean hurt(DamageSource pSource, float pAmount) {
		if (IS_RAMU_EGG.test(eatingItem) && !(pSource.getDirectEntity() instanceof Jackrobat)) {
			this.spawnAtLocation(eatingItem.copy());
			this.eatingItem = ItemStack.EMPTY;
		}
		return super.hurt(pSource, pAmount);
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag pCompound) {
		super.addAdditionalSaveData(pCompound);
		pCompound.put("eatingItem", eatingItem.save(new CompoundTag()));
		pCompound.putInt("eatingTicks", eatingTicks);
		if (targetNest != null) {
			pCompound.putLong("targetNest", targetNest.asLong());
		}
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag pCompound) {
		super.readAdditionalSaveData(pCompound);
		eatingItem = ItemStack.of(pCompound.getCompound("eatingItem"));
		eatingTicks = pCompound.getInt("eatingTicks");
		if (pCompound.contains("targetNest")) {
			targetNest = BlockPos.of(pCompound.getLong("targetNest"));
		}
	}
	
	private boolean canReachTargetNest() {
		if (targetNest == null) {
			return false;
		}
		return this.getOnPos().above().distSqr(this.targetNest) < 4;
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_EI, ItemStack.EMPTY);
	}
	
	@Override
	public boolean canTakeItem(ItemStack pItemstack) {
		return eatingItem.isEmpty() || (!IS_RAMU_EGG.test(eatingItem) && IS_RAMU_EGG.test(pItemstack));
	}
	
	@Override
	public boolean wantsToPickUp(ItemStack pStack) {
		return canTakeItem(pStack);
	}
	
	@Override
	public void onItemPickup(ItemEntity pItem) {
		super.onItemPickup(pItem);
	}
	
	@Override
	public boolean canPickUpLoot() {
		return !isTame() && super.canPickUpLoot();
	}
	
	@Override
	public boolean equipItemIfPossible(ItemStack p_21541_) {
		if (this.canHoldItem(p_21541_)) {
			if (!eatingItem.isEmpty()) {
				this.spawnAtLocation(eatingItem);
			}
			
			eatingItem = p_21541_.copy();
			this.playEquipSound(p_21541_);
			return true;
		} else {
			return false;
		}
	}
	
}