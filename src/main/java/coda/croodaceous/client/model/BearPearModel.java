package coda.croodaceous.client.model;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.entities.BearPear;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class BearPearModel extends GeoModel<BearPear> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CroodaceousMod.MOD_ID, "textures/entity/bear_pear/bear_pear.png");
    private static final ResourceLocation MODEL = new ResourceLocation(CroodaceousMod.MOD_ID, "geo/entity/bear_pear.geo.json");
    private static final ResourceLocation ANIMATIONS = new ResourceLocation(CroodaceousMod.MOD_ID, "animations/entity/bear_pear.animation.json");

    @Override
    public ResourceLocation getModelResource(BearPear animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(BearPear animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(BearPear animatable) {
        return ANIMATIONS;
    }
    @Override
    public void setCustomAnimations(BearPear animatable, long instanceId, AnimationState<BearPear> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        // locate bones
        final var tailBone = this.getBone("tail");
        final var rootBone = this.getBone("root");

        if (tailBone.isPresent() && rootBone.isPresent()) {
            var tail = tailBone.get();
            var root = rootBone.get();

            // rotate tail and body when swinging
            final float swingingAmount = animatable.getSwingingAmount();
            if(swingingAmount > 0) {
                final float lerpedSwingingAmount = Mth.lerp(animationState.getPartialTick() * BearPear.DELTA_SWINGING, Math.max(swingingAmount - BearPear.DELTA_SWINGING, 0), swingingAmount);
                final float swingingProgress = -Mth.cos(lerpedSwingingAmount * (3.5F) * (float)Math.PI);
                final float swingingAngle = Mth.DEG_TO_RAD * 30.0F * animatable.getSwingingStrength() * Mth.sin(swingingAmount * 0.5F * (float)Math.PI);
                final float deltaX = 0;
                final float deltaZ = 0;
                root.setPivotY(20.0F);
                root.setRotX(animatable.getSwingingDirection().x * swingingProgress * swingingAngle + deltaX);
                root.setRotZ(animatable.getSwingingDirection().y * swingingProgress * swingingAngle + deltaZ);
            } else {
                root.setPivotY(13.0F);
            }
            // stretch and offset tail when dropping
            final float dropDistance = (float) animatable.getDroppingDistanceOffset(animationState.getPartialTick());
            // for testing:
            // final float dropDistance = 3.0F * (Mth.cos((animatable.tickCount + animationEvent.getPartialTick()) * 0.125F) * 0.5F + 0.5F);
            if(dropDistance > 0) {
                // tail Y size = 6
                // tail Y position = 7
                final float scaleY = 1.0F + (dropDistance * 16.0F / 6.0F);
                tail.setPosY((dropDistance * 16.0F));
                tail.setScaleY(scaleY);
            } else {
                tail.setScaleY(1.0F);
            }
        }
    }

}
