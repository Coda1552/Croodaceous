package com.anar4732.croodaceous.client.model;

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
    public ResourceLocation getAnimationFileLocation(T entity) {
        return animation;
    }

    @Override
    public ResourceLocation getModelLocation(T entity) {
        return model;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }

    @Override
    public void setLivingAnimations(T entity, Integer uniqueID, @Nullable AnimationEvent customPredicate) {
        super.setLivingAnimations(entity, uniqueID, customPredicate);

        IBone root = getAnimationProcessor().getBone("root");

        if (entity.isBaby()) {
            root.setScaleX(0.5F);
            root.setScaleY(0.5F);
            root.setScaleZ(0.5F);
            // root.setPositionY(-2F);
        }
    }
}