package coda.croodaceous.client.render;

import coda.croodaceous.client.render.layer.BearowlEyesLayer;
import coda.croodaceous.client.model.BearowlModel;
import coda.croodaceous.common.entities.Bearowl;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BearOwlRenderer extends GeoEntityRenderer<Bearowl> {

	public BearOwlRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new BearowlModel());
		addRenderLayer(new BearowlEyesLayer(this));
	}

	@Override
	public void render(Bearowl entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		if (entity.isBaby()) {
			poseStack.scale(0.5F, 0.5F, 0.5F);
		}
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}
}