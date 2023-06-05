package coda.croodaceous.common.entities.goal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.function.Predicate;

public class StealItemFromPlayerGoal extends Goal {
	private final TamableAnimal mob;
	private final int chance;
	private Player nearestPlayer;
	private boolean stolen;
	
	public StealItemFromPlayerGoal(TamableAnimal mob, int chance) {
		super();
		this.mob = mob;
		this.chance = chance;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}
	
	@Override
	public boolean canUse() {
		// verify non-tamed
		if(this.mob.isTame()) {
			return false;
		}
		// check random chance
		if (this.chance != 0 && this.mob.getRandom().nextInt(this.chance) != 0) {
			return false;
		}
		final Vec3 pos = this.mob.position();
		final Predicate<Entity> canStealPredicate = e -> e instanceof Player player && !player.isCreative() && !player.isSpectator() && mob.canAttack(player);
		this.nearestPlayer = this.mob.level.getNearestPlayer(pos.x(), pos.y(), pos.z(), 10.0D, canStealPredicate);
		return this.nearestPlayer != null;
	}
	
	@Override
	public boolean canContinueToUse() {
		return !stolen && this.nearestPlayer != null && this.nearestPlayer.isAlive() && this.nearestPlayer.distanceToSqr(this.mob) < 100.0D && !this.nearestPlayer.getMainHandItem().isEmpty();
	}
	
	@Override
	public void stop() {
		super.stop();
		this.nearestPlayer = null;
		this.stolen = false;
	}
	
	@Override
	public void tick() {
		this.mob.getLookControl().setLookAt(this.nearestPlayer, 30.0F, 30.0F);
		if (this.mob.distanceToSqr(this.nearestPlayer) < 6.25D && !nearestPlayer.isCreative()) {
			this.steal();
		} else {
			this.mob.getNavigation().moveTo(this.nearestPlayer, 1.0D);
		}
	}
	
	private void steal() {
		ItemStack itemstack = this.nearestPlayer.getMainHandItem();
		if (itemstack.isEmpty()) {
			return;
		}
		this.mob.equipItemIfPossible(itemstack.split(1));
		this.stolen = true;
		// run away from target
		final int radius = 16;
		Vec3 target = DefaultRandomPos.getPosAway(this.mob, radius, 7, this.nearestPlayer.position());
		if(null == target) {
			target = new Vec3(mob.getX() + (mob.getRandom().nextDouble() - 0.5D) * 2.0D * radius,
				Mth.clamp(mob.getY() + (mob.getRandom().nextInt(radius) - 8), mob.level.getMinBuildHeight(), (mob.level.getMinBuildHeight() + ((ServerLevel)mob.level).getLogicalHeight() - 1)),
				mob.getZ() + (mob.getRandom().nextDouble() - 0.5D) * 2.0D * radius);
		}
		this.mob.getNavigation().moveTo(target.x(), target.y(), target.z(), 2.0D);
	}
	
}