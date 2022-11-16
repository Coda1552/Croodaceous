package com.anar4732.croodaceous.client.render;

import com.anar4732.croodaceous.client.model.BearOwlModel;
import com.anar4732.croodaceous.common.entities.BearowlEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import javax.annotation.Nullable;

public class BearOwlRenderer extends GeoEntityRenderer<BearowlEntity> {

	public BearOwlRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new BearOwlModel());
	}

	@Override
	public RenderType getRenderType(BearowlEntity animatable, float partialTicks, PoseStack stack, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
		return RenderType.entityTranslucent(textureLocation);
	}
	
}