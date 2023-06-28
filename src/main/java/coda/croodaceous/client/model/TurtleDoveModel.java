package coda.croodaceous.client.model;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.entities.TurtleDove;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
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
        final float zRot = calculateRoll(animatable, instanceId, animationEvent);
        final IBone bone = getBone(boneName);
        bone.setRotationZ(zRot);
    }

    protected float calculateRoll(final T entity, final int instanceId, final AnimationEvent animationEvent) {
        if(!entity.isFlying()) {
            return 0;
        }
        final AnimationData manager = entity.getFactory().getOrCreateAnimationData(instanceId);
        final int unpausedMultiplier = !Minecraft.getInstance().isPaused() || manager.shouldPlayWhilePaused ? 1 : 0;
        final float headYRot = getHeadRotations(entity, instanceId, animationEvent).y;
        final float bodyYRot = Mth.wrapDegrees(Mth.lerp(animationEvent.getPartialTick(), entity.yBodyRotO, entity.yBodyRot)) * Mth.DEG_TO_RAD;
        final float horizontalSpeedSq = new Vec2((float) (entity.xo - entity.getX()), (float) (entity.zo - entity.getZ())).lengthSquared();
        final float multiplier = Mth.clamp(15.0F * horizontalSpeedSq, 0.0F, 1.0F);
        final float maxRotation = Mth.PI / 4.0F;
        return Mth.clamp(multiplier * (headYRot - bodyYRot), -maxRotation, maxRotation) * unpausedMultiplier;
    }
}
