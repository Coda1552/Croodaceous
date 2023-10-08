package coda.croodaceous.client.render;

import coda.croodaceous.client.render.layer.BearowlEyesLayer;
import coda.croodaceous.client.model.BearowlModel;
import coda.croodaceous.common.entities.Bearowl;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BearOwlRenderer extends GeoEntityRenderer<Bearowl> {

	public BearOwlRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new BearowlModel());
		addRenderLayer(new BearowlEyesLayer(this));
	}
}