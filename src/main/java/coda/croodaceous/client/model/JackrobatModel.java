package coda.croodaceous.client.model;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.entities.Jackrobat;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;

public class JackrobatModel<T extends Jackrobat> extends SimpleGeoModel<T> {

    public JackrobatModel() {
        super(CroodaceousMod.MOD_ID, "jackrobat");
    }

    @Override
    public void setCustomAnimations(T animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);
        final IBone bone = this.getBone("Head");
        final Vec2 rotations = getHeadRotations(animatable, instanceId, animationEvent);
        bone.setRotationX(bone.getRotationX() + rotations.x);
        bone.setRotationY(bone.getRotationY() + rotations.y);
    }
}
