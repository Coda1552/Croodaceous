package coda.croodaceous.client.render.geo;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.client.layer.TripGerbilTailLayer;
import coda.croodaceous.client.model.geo.SimpleGeoModel;
import coda.croodaceous.common.entities.TripGerbil;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class TripGerbilRenderer extends GeoEntityRenderer<TripGerbil> {

	public TripGerbilRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new SimpleGeoModel<>(CroodaceousMod.MOD_ID, "trip_gerbil"));
		this.addLayer(new TripGerbilTailLayer(this, new Vector3f(1.0F, 1.0F, 1.0F)));
	}


	
}