package coda.croodaceous.client.model;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.entities.TurtleDove;
import net.minecraft.util.Mth;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;

public class TurtleDoveModel<T extends TurtleDove> extends SimpleGeoModel<T> {

    public TurtleDoveModel() {
        super(CroodaceousMod.MOD_ID, "turtle_dove");
    }

    @Override
    public void setCustomAnimations(T animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);
        rotateHeadBones(animatable, instanceId, animationEvent, "neck", "head");
        rotateBody(animatable, instanceId, animationEvent, "body");
    }

    protected void rotateBody(final T animatable, final int instanceId, final AnimationEvent animationEvent, final String boneName) {
        final float multiplier = Mth.clamp(((float)animatable.getDeltaMovement().horizontalDistanceSqr()) * 18.0F, 0.0F, 1.0F);
        final IBone bone = getBone(boneName);
        bone.setRotationZ(bone.getRotationZ() * multiplier);
    }
}
