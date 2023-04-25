package coda.croodaceous.client.render;

import coda.croodaceous.client.model.JackrobatModel;
import coda.croodaceous.common.entities.Jackrobat;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class JackrobatRenderer extends GeoEntityRenderer<Jackrobat> {

	public JackrobatRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new JackrobatModel());
	}


	
}