package coda.croodaceous.client.render;

import coda.croodaceous.client.model.SimpleGeoModel;
import coda.croodaceous.common.entities.FangFly;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FangFlyRenderer extends GeoEntityRenderer<FangFly> {

	public FangFlyRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new SimpleGeoModel<>("fang_fly"));
	}

	@Override
	public RenderType getRenderType(FangFly animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityCutoutNoCull(texture);
	}

	@Override
	public void render(FangFly entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		if (entity.isBaby()) {
			poseStack.scale(0.5F, 0.5F, 0.5F);
		}
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}
}