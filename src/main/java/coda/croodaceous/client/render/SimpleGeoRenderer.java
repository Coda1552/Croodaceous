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
	private float scale = 1F;
	
	public SimpleGeoRenderer(EntityRendererProvider.Context mgr, String modId, String modelName) {
		super(mgr, new SimpleGeoModel<>(modId, modelName));
	}
	
	public SimpleGeoRenderer(EntityRendererProvider.Context mgr, String modId, String modelName, float scale) {
		this(mgr, modId, modelName);
		this.scale = scale;
	}
	
	public SimpleGeoRenderer(EntityRendererProvider.Context mgr, AnimatedGeoModel<T> modelProvider) {
		super(mgr, modelProvider);
	}

	public SimpleGeoRenderer(EntityRendererProvider.Context mgr, AnimatedGeoModel<T> modelProvider, float scale) {
		this(mgr, modelProvider);
		this.scale = scale;
	}
	
	@Override
	public void render(T entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn) {
		if (scale != 1F) {
			stack.scale(scale, scale, scale);
		}
		super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
	}

	@Override
	public RenderType getRenderType(T animatable, float partialTicks, PoseStack stack, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
		return RenderType.entityTranslucent(textureLocation);
	}
}