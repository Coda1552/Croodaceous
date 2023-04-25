package coda.croodaceous.client.render;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.client.model.SimpleGeoModel;
import coda.croodaceous.common.entities.FangFly;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class FangFlyRenderer extends GeoEntityRenderer<FangFly> {

	public FangFlyRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new SimpleGeoModel<>(CroodaceousMod.MOD_ID, "fang_fly"));
	}
}