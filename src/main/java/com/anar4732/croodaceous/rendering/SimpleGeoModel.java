package com.anar4732.croodaceous.rendering;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SimpleGeoModel<T extends IAnimatable> extends AnimatedGeoModel<T> {
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
}