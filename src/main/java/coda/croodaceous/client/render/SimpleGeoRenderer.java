package coda.croodaceous.client.render;

import coda.croodaceous.client.model.SimpleGeoModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import javax.annotation.Nullable;

public class SimpleGeoRenderer<T extends LivingEntity & IAnimatable> extends GeoEntityRenderer<T> {

	public SimpleGeoRenderer(EntityRendererProvider.Context mgr, String modId, String modelName) {
		this(mgr, new SimpleGeoModel<>(modId, modelName), 1.0F);
	}

	public SimpleGeoRenderer(EntityRendererProvider.Context mgr, String modId, String modelName, float scale) {
		this(mgr, new SimpleGeoModel<>(modId, modelName), scale);
	}

	public SimpleGeoRenderer(EntityRendererProvider.Context mgr, AnimatedGeoModel<T> modelProvider) {
		this(mgr, modelProvider, 1.0F);
	}

	public SimpleGeoRenderer(EntityRendererProvider.Context mgr, AnimatedGeoModel<T> modelProvider, float scale) {
		super(mgr, modelProvider);
		this.widthScale = scale;
		this.heightScale = scale;
	}

	@Override
	public float getWidthScale(T entity) {
		return super.getWidthScale(entity) * (entity.isBaby() ? 0.5F : 1.0F);
	}

	@Override
	public float getHeightScale(T entity) {
		return super.getHeightScale(entity) * (entity.isBaby() ? 0.5F : 1.0F);
	}

	@Override
	public RenderType getRenderType(T animatable, float partialTicks, PoseStack stack, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
		return RenderType.entityTranslucent(textureLocation);
	}
}