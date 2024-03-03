package coda.croodaceous.client.render;

import coda.croodaceous.client.model.TurtleDoveModel;
import coda.croodaceous.common.entities.TurtleDove;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TurtleDoveRenderer extends GeoEntityRenderer<TurtleDove> {

	public TurtleDoveRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new TurtleDoveModel<>());
	}

	@Override
	public void render(TurtleDove entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		if (entity.isBaby()) {
			poseStack.scale(0.5F, 0.5F, 0.5F);
		}
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}
}