package coda.croodaceous.client.render;

import coda.croodaceous.client.model.SimpleGeoModel;
import coda.croodaceous.common.entities.FangFly;
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
}