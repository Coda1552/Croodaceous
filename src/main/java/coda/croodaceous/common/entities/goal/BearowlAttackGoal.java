package coda.croodaceous.common.entities.goal;

import coda.croodaceous.common.entities.Bearowl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

// Code from Glumbis
public class BearowlAttackGoal extends Goal {
    public Bearowl bearowl;
    public int timer;
    public final int timerEnd;
    public int coolDown;
    public int coolDownEnd;
    public final int frameStart;
    public final int frameEnd;
    public float range;
    public boolean isInRange;

    public BearowlAttackGoal(Bearowl glumboss, int timerEnd, int coolDownEnd, int frameStart, int frameEnd, float range) {
        this.bearowl = glumboss;
        this.timerEnd = timerEnd;
        this.coolDownEnd = coolDownEnd;
        this.frameStart = frameStart;
        this.frameEnd = frameEnd;
        this.range = range;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = bearowl.getTarget();

        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        return bearowl.isAlive() && target != null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return bearowl.getTarget() != null;
    }

    @Override
    public void start() {
        super.start();
        bearowl.setAttacking(true);
        coolDown = 0;
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = bearowl.getTarget();
        if (target != null) {
            if (bearowl.distanceTo(target) < range) {
                isInRange = true;
                bearowl.getNavigation().moveTo(target, 1.0F);
                bearowl.getNavigation().stop();
            }
            else {
                bearowl.getNavigation().moveTo(target, 1.0F);
                isInRange = false;
            }
        }
        if (coolDown <= coolDownEnd) {
            coolDown++;
            timer = 0;
        }
        else {
            if (timer <= timerEnd && isInRange) {
                timer++;
                bearowl.getNavigation().stop();
                if (timer >= frameStart && timer <= frameEnd) {
                    attack();
                }
            }
            else {
                this.coolDown = 0;
                this.timer = 0;
                this.isInRange = false;
            }
        }
    }

    @Override
    public void stop() {
        this.coolDown = 0;
        this.timer = 0;
        this.isInRange = false;
        bearowl.setAttacking(false);

        super.stop();
    }

    public void attack() {
        LivingEntity target = bearowl.getTarget();

        if (target != null && target.isAlive()) {
            this.bearowl.swing(InteractionHand.MAIN_HAND);
            this.bearowl.doHurtTarget(target);
        }
    }
}