package coda.croodaceous.client.render;

import coda.croodaceous.client.model.TurtleDoveModel;
import coda.croodaceous.common.entities.TurtleDove;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TurtleDoveRenderer extends GeoEntityRenderer<TurtleDove> {

	public TurtleDoveRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new TurtleDoveModel<>());
	}
}