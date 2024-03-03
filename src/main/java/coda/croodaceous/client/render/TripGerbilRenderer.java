package coda.croodaceous.client.render;

import coda.croodaceous.client.model.SimpleGeoModel;
import coda.croodaceous.client.render.layer.TripGerbilTailLayer;
import coda.croodaceous.common.entities.TripGerbil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TripGerbilRenderer extends GeoEntityRenderer<TripGerbil> {

	public TripGerbilRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new SimpleGeoModel<>("trip_gerbil"));
		this.addRenderLayer(new TripGerbilTailLayer(this));
	}

	@Override
	public RenderType getRenderType(TripGerbil animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityCutout(texture);
	}
}