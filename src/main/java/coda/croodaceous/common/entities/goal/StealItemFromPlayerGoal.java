package coda.croodaceous.common.entities.goal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;

public class StealItemFromPlayerGoal extends Goal {
	private final Mob mob;
	private final int chance;
	private Player nearestPlayer;
	private boolean stolen;
	
	public StealItemFromPlayerGoal(Mob mob, int chance) {
		super();
		this.mob = mob;
		this.chance = chance;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}
	
	@Override
	public boolean canUse() {
		if (this.chance != 0 && this.mob.getRandom().nextInt(this.chance) != 0) {
			return false;
		} else {
			this.nearestPlayer = this.mob.level.getNearestPlayer(this.mob, 10.0D);
			return this.nearestPlayer != null;
		}
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
		if (this.mob.distanceToSqr(this.nearestPlayer) < 6.25D) {
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
		this.mob.equipItemIfPossible(itemstack);
		this.nearestPlayer.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		this.stolen = true;
		double x = mob.getX() + (mob.getRandom().nextDouble() - 0.5D) * 16.0D;
		double y = Mth.clamp(mob.getY() + (mob.getRandom().nextInt(16) - 8), mob.level.getMinBuildHeight(), (mob.level.getMinBuildHeight() + ((ServerLevel)mob.level).getLogicalHeight() - 1));
		double z = mob.getZ() + (mob.getRandom().nextDouble() - 0.5D) * 16.0D;
		this.mob.getNavigation().moveTo(x, y, z, 2.0D);
	}
	
}