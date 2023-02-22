package coda.croodaceous.client.render;

import coda.croodaceous.client.layer.BearowlEyesLayer;
import coda.croodaceous.client.model.BearowlModel;
import coda.croodaceous.common.entities.Bearowl;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import javax.annotation.Nullable;

public class BearOwlRenderer extends GeoEntityRenderer<Bearowl> {

	public BearOwlRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new BearowlModel());
		addLayer(new BearowlEyesLayer(this));
	}

	@Override
	public RenderType getRenderType(Bearowl animatable, float partialTicks, PoseStack stack, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
		return RenderType.entityTranslucent(textureLocation);
	}
	
}