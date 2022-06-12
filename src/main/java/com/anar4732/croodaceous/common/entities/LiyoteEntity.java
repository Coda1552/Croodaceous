package com.anar4732.croodaceous.common.entities;

import com.anar4732.croodaceous.common.blocks.RamuNestBlock;
import com.anar4732.croodaceous.registry.CEBlocks;
import com.anar4732.croodaceous.registry.CEEntities;
import com.anar4732.croodaceous.registry.CEItems;
import com.anar4732.croodaceous.registry.CEPointOfInterestTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.UUID;

public class LiyoteEntity extends Wolf implements IAnimatable {
	private static final EntityDataAccessor<ItemStack> DATA_EI = SynchedEntityData.defineId(LiyoteEntity.class, EntityDataSerializers.ITEM_STACK);
	private final AnimationFactory animationFactory = new AnimationFactory(this);
	private int eatingTicks = 0;
	private ItemStack eatingItem = ItemStack.EMPTY;
	private BlockPos targetNest;
	
	public LiyoteEntity(EntityType<? extends LiyoteEntity> type, Level level) {
		super(type, level);
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
				return super.canUse() && !((LiyoteEntity) mob).isTame();
			}
		});
		this.goalSelector.addGoal(12, new AvoidEntityGoal<>(this, RamuEntity.class, 10.0F, 1.0D, 1.2D) {
			@Override
			public boolean canUse() {
				return eatingItem.getItem() == CEItems.RAMU_EGG.get() && super.canUse();
			}
		});
		super.registerGoals();
		this.targetSelector.removeAllGoals();
	}
	
	@Override
	public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
		ItemStack itemstack = pPlayer.getItemInHand(pHand);
		Item item = itemstack.getItem();
		if (this.isTame()) {
			if (this.isFood(itemstack) && this.eatingItem.isEmpty()) {
				if (!pPlayer.getAbilities().instabuild) {
					itemstack.shrink(1);
				}
				
				this.eatingItem = itemstack.copy();
				return InteractionResult.SUCCESS;
			}
			
			InteractionResult interactionresult = super.mobInteract(pPlayer, pHand);
			if ((!interactionresult.consumesAction()) && this.isOwnedBy(pPlayer)) {
				this.setOrderedToSit(!this.isOrderedToSit());
				this.jumping = false;
				this.navigation.stop();
				this.setTarget(null);
				return InteractionResult.SUCCESS;
			}

			return interactionresult;
		} else if (itemstack.is(CEItems.RAMU_EGG.get()) && !this.isAngry()) {
			if (!pPlayer.getAbilities().instabuild) {
				itemstack.shrink(1);
			}
			
			if (this.random.nextInt(2) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, pPlayer)) {
				this.tame(pPlayer);
				this.navigation.stop();
				this.setTarget(null);
				this.setOrderedToSit(true);
				this.level.broadcastEntityEvent(this, (byte) 7);
			} else {
				this.level.broadcastEntityEvent(this, (byte) 6);
			}
			
			return InteractionResult.SUCCESS;
		}
		
		if (item instanceof DyeItem || item == Items.BONE) {
			return InteractionResult.PASS;
		}
		
		return super.mobInteract(pPlayer, pHand);
	}
	
	private PlayState animControllerMain(AnimationEvent<?> e) {
		if (!eatingItem.isEmpty()) {
			e.getController().setAnimation(new AnimationBuilder().addAnimation("animation.liyote.eat", true));
		} else if (e.isMoving()) {
			e.getController().setAnimation(new AnimationBuilder().addAnimation("animation.liyote.walk", true));
		} else if (isInSittingPose()) {
			e.getController().setAnimation(new AnimationBuilder().addAnimation("animation.liyote.sitting", true));
		} else {
			e.getController().setAnimation(new AnimationBuilder().addAnimation("animation.liyote.idle", true));
		}
		return PlayState.CONTINUE;
	}
	
	@Override
	public void registerControllers(AnimationData data) {
		data.addAnimationController(new AnimationController<>(this, "controller", 2F, this::animControllerMain));
	}
	
	@Override
	public AnimationFactory getFactory() {
		return animationFactory;
	}
	
	@Override
	public void tick() {
		super.tick();
		if (!eatingItem.isEmpty() && eatingItem.getItem() != CEItems.RAMU_EGG.get()) {
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
				this.level.broadcastEntityEvent(this, (byte) 18);
				heal(4);
			}
		}
		if (!this.level.isClientSide && !this.isInSittingPose() && eatingItem.isEmpty() && this.random.nextInt(1200) == 0) {
			PoiManager poiManager = ((ServerLevel) level).getPoiManager();
			poiManager.findClosest(CEPointOfInterestTypes.RAMU_NEST.get().getPredicate(), p -> this.level.getBlockState(p).getValue(RamuNestBlock.WITH_EGG), this.getOnPos(), 32, PoiManager.Occupancy.ANY).ifPresent(p -> {
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
	public Wolf getBreedOffspring(ServerLevel p_149088_, AgeableMob p_149089_) {
		LiyoteEntity liyote = new LiyoteEntity(CEEntities.ENTITY_LIYOTE.get(), level);
		UUID uuid = this.getOwnerUUID();
		if (uuid != null) {
			liyote.setOwnerUUID(uuid);
			liyote.setTame(true);
		}
		return liyote;
	}
	
	@Override
	public boolean hurt(DamageSource pSource, float pAmount) {
		if (eatingItem.getItem() == CEItems.RAMU_EGG.get()) {
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
	
}