package coda.croodaceous.client.model;

import coda.croodaceous.CroodaceousMod;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedGeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import java.util.Optional;

public class SimpleGeoModel<T extends LivingEntity & GeoEntity> extends DefaultedGeoModel<T> {

    public SimpleGeoModel(String name) {
        super(new ResourceLocation(CroodaceousMod.MOD_ID, name));
    }

    @Override
    protected String subtype() {
        return "entity";
    }

    /**
     * Rotates the head bone according to the head rotations
     * @param entity the entity instance
     * @param instanceId the animation instance ID
     * @param animationEvent the animation event
     * @param boneName the name of the head bone
     */
    protected void rotateHead(final T entity, final int instanceId, final EntityModelData animationEvent, final String boneName) {
        rotateHead(animationEvent, boneName, 1.0F);
    }

    /**
     * Rotates the head bone according to the head rotations
     * @param animationEvent the animation event
     * @param boneName the name of the head bone
     * @param multiplier the amount to multiply the head rotation
     */
    protected void rotateHead(final EntityModelData animationEvent, final String boneName, final float multiplier) {
        final Optional<GeoBone> bone = this.getBone(boneName);
        final Vec2 rotations = getHeadRotations(animationEvent).scale(multiplier);
        bone.get().setRotX(bone.get().getRotX() + rotations.x);
        bone.get().setRotY(bone.get().getRotY() + rotations.y);
    }

    /**
     * Rotates multiple bones according to the head rotations
     * @param boneNames the names of each head bone to rotate
     */
    protected void rotateHeadBones(EntityModelData data, final String... boneNames) {
        if (boneNames.length <= 0) {
            return;
        }
        final Vec2 rotations = getHeadRotations(data).scale(1.0F / boneNames.length);
        Optional<GeoBone> bone;
        for(String name : boneNames) {
            bone = this.getBone(name);
            bone.get().setRotX(bone.get().getRotX() + rotations.x);
            bone.get().setRotY(bone.get().getRotY() + rotations.y);
        }
    }

    /**
     * @param data the animation event
     * @return the headPitch and netHeadYaw as a Vec2
     */
    protected Vec2 getHeadRotations(EntityModelData data) {
        int unpausedMultiplier = !Minecraft.getInstance().isPaused() ? 1 : 0;
        return new Vec2(data.headPitch(), data.netHeadYaw()).scale(Mth.DEG_TO_RAD * unpausedMultiplier);
    }
}