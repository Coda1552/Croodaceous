package coda.croodaceous.client.model;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.entities.Jackrobat;
import net.minecraft.util.Mth;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;

public class JackrobatModel<T extends Jackrobat> extends SimpleGeoModel<T> {

    public JackrobatModel() {
        super(CroodaceousMod.MOD_ID, "jackrobat");
    }

    @Override
    public void setCustomAnimations(T animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);
        // calculate head rotation
        final float xRot = -Mth.lerp(animationEvent.getPartialTick(), animatable.xRotO, animatable.getXRot());
        final float yBodyRot = Mth.rotLerp(animationEvent.getPartialTick(), animatable.yBodyRotO, animatable.yBodyRot);
        final float yHeadRot = Mth.rotLerp(animationEvent.getPartialTick(), animatable.yHeadRotO, animatable.yHeadRot);
        final float yRot = -(yHeadRot - yBodyRot);
        // rotate head bone
        final IBone head = this.getBone("Head");
        head.setRotationX((float) Math.toRadians(xRot));
        head.setRotationY((float) Math.toRadians(yRot));
    }
}
