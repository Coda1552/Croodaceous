package coda.croodaceous.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class SimpleGeoModel<T extends LivingEntity & IAnimatable> extends AnimatedGeoModel<T> {
    private final ResourceLocation texture;
    private final ResourceLocation model;
    private final ResourceLocation animation;

    public SimpleGeoModel(String modId, String name) {
        this.texture = new ResourceLocation(modId, "textures/entity/" + name + ".png");
        this.model = new ResourceLocation(modId, "geo/entity/" + name + ".geo.json");
        this.animation = new ResourceLocation(modId, "animations/entity/" + name + ".animation.json");
    }

    @Override
    public ResourceLocation getAnimationResource(T entity) {
        return animation;
    }

    @Override
    public ResourceLocation getModelResource(T entity) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(T entity) {
        return texture;
    }

    /**
     * Rotates the head bone according to the head rotations
     * @param entity the entity instance
     * @param instanceId the animation instance ID
     * @param animationEvent the animation event
     * @param boneName the name of the head bone
     * @see #rotateHead(LivingEntity, int, AnimationEvent, String, float)
     * @see #rotateHeadBones(LivingEntity, int, AnimationEvent, String...)
     * @see #getHeadRotations(LivingEntity, int, AnimationEvent)
     */
    protected void rotateHead(final T entity, final int instanceId, final AnimationEvent animationEvent, final String boneName) {
        rotateHead(entity, instanceId, animationEvent, boneName, 1.0F);
    }

    /**
     * Rotates the head bone according to the head rotations
     * @param entity the entity instance
     * @param instanceId the animation instance ID
     * @param animationEvent the animation event
     * @param boneName the name of the head bone
     * @param multiplier the amount to multiply the head rotation
     * @see #rotateHead(LivingEntity, int, AnimationEvent, String)
     * @see #rotateHeadBones(LivingEntity, int, AnimationEvent, String...)
     * @see #getHeadRotations(LivingEntity, int, AnimationEvent)
     */
    protected void rotateHead(final T entity, final int instanceId, final AnimationEvent animationEvent, final String boneName, final float multiplier) {
        final IBone bone = this.getBone(boneName);
        final Vec2 rotations = getHeadRotations(entity, instanceId, animationEvent).scale(multiplier);
        bone.setRotationX(bone.getRotationX() + rotations.x);
        bone.setRotationY(bone.getRotationY() + rotations.y);
    }

    /**
     * Rotates multiple bones according to the head rotations
     * @param entity the entity instance
     * @param instanceId the animation instance ID
     * @param animationEvent the animation event
     * @param boneNames the names of each head bone to rotate
     */
    protected void rotateHeadBones(final T entity, final int instanceId, final AnimationEvent animationEvent, final String... boneNames) {
        if(boneNames.length <= 0) {
            return;
        }
        final Vec2 rotations = getHeadRotations(entity, instanceId, animationEvent).scale(1.0F / boneNames.length);
        IBone bone;
        for(String name : boneNames) {
            bone = this.getBone(name);
            bone.setRotationX(bone.getRotationX() + rotations.x);
            bone.setRotationY(bone.getRotationY() + rotations.y);
        }
    }

    /**
     * @param entity the entity instance
     * @param instanceId the animation instance ID
     * @param animationEvent the animation event
     * @return the headPitch and netHeadYaw as a Vec2
     */
    protected Vec2 getHeadRotations(T entity, int instanceId, AnimationEvent animationEvent) {
        EntityModelData extraData = (EntityModelData) animationEvent.getExtraDataOfType(EntityModelData.class).get(0);
        AnimationData manager = entity.getFactory().getOrCreateAnimationData(instanceId);
        int unpausedMultiplier = !Minecraft.getInstance().isPaused() || manager.shouldPlayWhilePaused ? 1 : 0;
        return new Vec2(extraData.headPitch, extraData.netHeadYaw).scale(Mth.DEG_TO_RAD * unpausedMultiplier);
    }
}