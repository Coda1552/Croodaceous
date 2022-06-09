package com.anar4732.croodaceous.client.render;

import com.anar4732.croodaceous.CroodaceousMod;
import com.anar4732.croodaceous.client.model.SimpleGeoModel;
import com.anar4732.croodaceous.common.entities.LiyoteEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import javax.annotation.Nullable;

public class LiyoteRenderer extends GeoEntityRenderer<LiyoteEntity> {
	private MultiBufferSource renderTypeBuffer;
	private LiyoteEntity animatable;
	
	public LiyoteRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new SimpleGeoModel<>(CroodaceousMod.ID, "liyote"));
	}

	@Override
	public void renderRecursively(GeoBone bone, PoseStack stack, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		if (bone.name.equals("snout")) {
			if (!mainHand.isEmpty()) {
				stack.pushPose();
				stack.mulPose(Vector3f.XP.rotation(130));
				stack.translate(0, 0.5, 0.8);
				Minecraft.getInstance().getItemRenderer().renderStatic(mainHand, ItemTransforms.TransformType.GROUND, packedLightIn, packedOverlayIn, stack, renderTypeBuffer, 0);
				RenderType type = getRenderType(animatable, 1F, stack, renderTypeBuffer, null, packedLightIn, getTextureLocation(animatable));
				bufferIn = renderTypeBuffer.getBuffer(type);
				stack.popPose();
			}
		}
		super.renderRecursively(bone, stack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
	@Override
	public void renderLate(LiyoteEntity animatable, PoseStack stackIn, float ticks, MultiBufferSource renderTypeBuffer, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float partialTicks) {
		this.renderTypeBuffer = renderTypeBuffer;
		this.animatable = animatable;
	}
	
	@Override
	public RenderType getRenderType(LiyoteEntity animatable, float partialTicks, PoseStack stack, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
		return RenderType.entityTranslucent(textureLocation);
	}
	
}