package coda.croodaceous.client.render;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.client.layer.TripGerbilTailLayer;
import coda.croodaceous.client.model.SimpleGeoModel;
import coda.croodaceous.common.entities.TripGerbil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;

public class TripGerbilRenderer extends SimpleGeoRenderer<TripGerbil> {

	private final GeoLayerRenderer<TripGerbil> tailLayer;

	public TripGerbilRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new SimpleGeoModel<>(CroodaceousMod.MOD_ID, "trip_gerbil"));
		this.tailLayer = new TripGerbilTailLayer(this);
	}

	@Override
	public void render(TripGerbil animatable, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		super.render(animatable, entityYaw, partialTick, poseStack, bufferSource, packedLight);
		// manually render the tail layer in order to use a fresh pose stack with world coordinates instead of relative ones
		tailLayer.render(poseStack, bufferSource, packedLight, animatable, 0, 0, partialTick, animatable.tickCount + partialTick, 0, 0);
	}
}