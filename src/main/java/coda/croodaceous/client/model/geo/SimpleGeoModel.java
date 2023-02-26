package coda.croodaceous.client.model.geo;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SimpleGeoModel<T extends LivingEntity & IAnimatable> extends AnimatedGeoModel<T> {
    private final ResourceLocation texture;
    private final ResourceLocation model;
    private final ResourceLocation animation;

    public SimpleGeoModel(String modId, String name) {
        this.texture = new ResourceLocation(modId, "textures/entity/" + name + ".png");
        this.model = new ResourceLocation(modId, "geo/" + name + ".geo.json");
        this.animation = new ResourceLocation(modId, "animations/" + name + ".animation.json");
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

    @Override
    public void setCustomAnimations(T entity, int uniqueID, AnimationEvent customPredicate) {
        super.setCustomAnimations(entity, uniqueID, customPredicate);

        IBone root = getAnimationProcessor().getBone("root");

        if (entity.isBaby()) {
            root.setScaleX(0.5F);
            root.setScaleY(0.5F);
            root.setScaleZ(0.5F);
        }
    }
}