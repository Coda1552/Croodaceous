package com.anar4732.croodaceous.client.model;

import com.anar4732.croodaceous.CroodaceousMod;
import com.anar4732.croodaceous.common.entities.BearowlEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class BearOwlModel extends AnimatedGeoModel<BearowlEntity> {

    @Override
    public ResourceLocation getModelLocation(BearowlEntity object) {
        return new ResourceLocation(CroodaceousMod.MOD_ID, "geo/bearowl.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(BearowlEntity object) {
        return object.sleeping ? new ResourceLocation(CroodaceousMod.MOD_ID, "textures/entity/bearowl/sleeping.png") : new ResourceLocation(CroodaceousMod.MOD_ID, "textures/entity/bearowl/bearowl.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(BearowlEntity animatable) {
        return new ResourceLocation(CroodaceousMod.MOD_ID, "animations/bearowl.animation.json");
    }

    @Override
    public void setLivingAnimations(BearowlEntity entity, Integer uniqueID, @Nullable AnimationEvent customPredicate) {
        super.setLivingAnimations(entity, uniqueID, customPredicate);

        IBone root = getAnimationProcessor().getBone("root");

        if (entity.isBaby()) {
            root.setScaleX(0.5F);
            root.setScaleY(0.5F);
            root.setScaleZ(0.5F);
        }
    }
}