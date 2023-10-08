package coda.croodaceous.client.render;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.client.model.SimpleGeoModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import javax.annotation.Nullable;

// todo - remove this and have an individual render for each entity?
public class SimpleGeoRenderer<T extends LivingEntity & GeoEntity> extends GeoEntityRenderer<T> {

	public SimpleGeoRenderer(EntityRendererProvider.Context mgr, String modelName) {
		this(mgr, modelName, 1.0F);
	}

	public SimpleGeoRenderer(EntityRendererProvider.Context mgr, String modelName, float scale) {
		this(mgr, modelName, scale);
	}

	public SimpleGeoRenderer(EntityRendererProvider.Context mgr, String assetSubpath) {
		this(mgr, assetSubpath, 1.0F);
	}

	public SimpleGeoRenderer(EntityRendererProvider.Context mgr, String assetSubpath, float scale) {
		super(mgr, new DefaultedGeoModel<>(new ResourceLocation(CroodaceousMod.MOD_ID, assetSubpath)));
		this.scaleWidth = scale;
		this.scaleHeight = scale;
	}

	@Override
	public void preRender(PoseStack poseStack, T animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

		if (animatable.isBaby()) {
			poseStack.scale(0.5F, 0.5F, 0.5F);
		}
	}
}