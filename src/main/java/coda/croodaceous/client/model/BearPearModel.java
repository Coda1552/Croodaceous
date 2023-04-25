package coda.croodaceous.client.model;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.entities.BearPear;
import net.minecraft.util.Mth;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;

public class BearPearModel extends SimpleGeoModel<BearPear> {

    public BearPearModel() {
        super(CroodaceousMod.MOD_ID, "bear_pear");
    }

    @Override
    public void setCustomAnimations(BearPear entity, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(entity, instanceId, animationEvent);
        // locate bones
        final IBone tail = this.getBone("tail");
        final IBone root = this.getBone("root");
        final IBone body = this.getBone("body");
        // rotate tail and body when swinging
        final float swingingAmount = entity.getSwingingAmount();
        if(swingingAmount > 0) {
            final float lerpedSwingingAmount = Mth.lerp(animationEvent.getPartialTick() * BearPear.DELTA_SWINGING, Math.max(swingingAmount - BearPear.DELTA_SWINGING, 0), swingingAmount);
            final float swingingProgress = -Mth.cos(lerpedSwingingAmount * (3.5F) * (float)Math.PI);
            final float swingingAngle = Mth.DEG_TO_RAD * 30.0F * entity.getSwingingStrength() * Mth.sin(swingingAmount * 0.5F * (float)Math.PI);
            final float deltaX = 0;
            final float deltaZ = 0;
            root.setPivotY(20.0F);
            root.setRotationX(entity.getSwingingDirection().x * swingingProgress * swingingAngle + deltaX);
            root.setRotationZ(entity.getSwingingDirection().y * swingingProgress * swingingAngle + deltaZ);
        } else {
            root.setPivotY(13.0F);
        }
        // stretch and offset tail when dropping
        final float dropDistance = (float) entity.getDroppingDistanceOffset(animationEvent.getPartialTick());
        // for testing:
        // final float dropDistance = 3.0F * (Mth.cos((entity.tickCount + animationEvent.getPartialTick()) * 0.125F) * 0.5F + 0.5F);
        if(dropDistance > 0) {
            // tail Y size = 6
            // tail Y position = 7
            final float scaleY = 1.0F + (dropDistance * 16.0F / 6.0F);
            tail.setPositionY((dropDistance * 16.0F));
            tail.setScaleY(scaleY);
        } else {
            tail.setScaleY(1.0F);
        }
    }
}
