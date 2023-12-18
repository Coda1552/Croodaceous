package coda.croodaceous.client.model;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.entities.TurtleDove;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationState;

public class TurtleDoveModel<T extends TurtleDove> extends SimpleGeoModel<T> {

    public TurtleDoveModel() {
        super(CroodaceousMod.MOD_ID, "turtle_dove");
    }

    @Override
    public void setCustomAnimations(T animatable, long instanceId, AnimationState<T> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        rotateHeadBones(animationState.getData(DataTickets.ENTITY_MODEL_DATA), "neck", "head");
        rotateBody(animatable, instanceId, animationState, "body");
    }

    protected void rotateBody(final T animatable, final long instanceId, final AnimationState<T> animationEvent, final String boneName) {
        final float zRot = calculateRoll(animatable, instanceId, animationEvent);
        final var bone = getBone(boneName);
        //bone.setRotationZ(zRot);
    }

    protected float calculateRoll(final T entity, final long instanceId, final AnimationState<T> event) {
        final float bodyRollPercent = Mth.lerp(event.getPartialTick(), entity.yBodyRollO, entity.yBodyRoll);
        final float maxRotation = Mth.PI / 4.0F;
        return bodyRollPercent * maxRotation;
    }
}
