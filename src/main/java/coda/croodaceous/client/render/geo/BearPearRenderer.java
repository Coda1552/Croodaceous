package coda.croodaceous.client.render.geo;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.client.model.geo.BearPearModel;
import coda.croodaceous.client.model.geo.SimpleGeoModel;
import coda.croodaceous.common.entities.BearPear;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class BearPearRenderer extends GeoEntityRenderer<BearPear> {

	private static final ResourceLocation TEXTURE_IDLE = new ResourceLocation(CroodaceousMod.MOD_ID, "textures/entity/bear_pear/idle.png");
	private static final ResourceLocation TEXTURE_HOSTILE = new ResourceLocation(CroodaceousMod.MOD_ID, "textures/entity/bear_pear/hostile.png");

	public BearPearRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new BearPearModel());
	}

	@Override
	public ResourceLocation getTextureLocation(BearPear animatable) {
		if (animatable.isAggressive() || animatable.isDropping()) {
			return TEXTURE_HOSTILE;
		}
		return TEXTURE_IDLE;
	}
}