package coda.croodaceous.client.model.geo;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.entities.BearPear;
import net.minecraft.client.Minecraft;
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
        // rotate tail and body when swinging
        final IBone tail = this.getBone("tail");
        final float swingingAmount = entity.getSwingingAmount();
        if(swingingAmount > 0) {
            final float lerpedSwingingAmount = Mth.lerp(animationEvent.getPartialTick() * BearPear.DELTA_SWINGING, Math.max(swingingAmount - BearPear.DELTA_SWINGING, 0), swingingAmount);
            final float swingingProgress = -Mth.cos(lerpedSwingingAmount * (3.5F) * (float)Math.PI);
            final float swingingAngle = Mth.DEG_TO_RAD * 30.0F * entity.getSwingingStrength() * Mth.sin(swingingAmount * 0.5F * (float)Math.PI);
            //final float rotY = Mth.wrapDegrees(Mth.lerp(animationEvent.getPartialTick(), entity.yBodyRotO, entity.yBodyRot)) * Mth.DEG_TO_RAD;
            final float deltaX = 0;//Mth.cos(rotY);
            final float deltaZ = 0;//Mth.sin(rotY);
            tail.setRotationX(entity.getSwingingDirection().x * swingingProgress * swingingAngle + deltaX);
            tail.setRotationZ(entity.getSwingingDirection().y * swingingProgress * swingingAngle + deltaZ);
        } else {
            tail.setRotationX(0);
            tail.setRotationZ(0);
        }
        // rotate root when not hanging
        final IBone root = this.getBone("root");
        if(entity.isHanging()) {
            root.setRotationX(0);
            root.setPositionY(0.0F);
        } else {
            root.setRotationX(90.0F * Mth.DEG_TO_RAD);
            root.setPositionY(3.0F);
        }
    }
}
