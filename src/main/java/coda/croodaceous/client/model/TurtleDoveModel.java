package coda.croodaceous.client.model;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.entities.TurtleDove;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

public class TurtleDoveModel<T extends TurtleDove> extends SimpleGeoModel<T> {

    public TurtleDoveModel() {
        super(CroodaceousMod.MOD_ID, "turtle_dove");
    }

    @Override
    public void setCustomAnimations(T animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);
        rotateHeadBones(animatable, instanceId, animationEvent, "neck", "head");
    }
}
