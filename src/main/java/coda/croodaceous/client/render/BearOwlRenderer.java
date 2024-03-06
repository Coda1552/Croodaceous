package coda.croodaceous.client.render;

import coda.croodaceous.client.render.layer.BearowlEyesLayer;
import coda.croodaceous.client.model.BearowlModel;
import coda.croodaceous.common.entities.Bearowl;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BearOwlRenderer extends GeoEntityRenderer<Bearowl> {

	public BearOwlRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new BearowlModel());
		addRenderLayer(new BearowlEyesLayer(this));
	}

	@Override
	public RenderType getRenderType(Bearowl animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityCutout(texture);
	}
}