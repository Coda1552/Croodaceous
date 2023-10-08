package coda.croodaceous.client.model;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.entities.Bearowl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class BearowlModel extends GeoModel<Bearowl> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(CroodaceousMod.MOD_ID, "textures/entity/bearowl/bearowl.png");
	private static final ResourceLocation MODEL = new ResourceLocation(CroodaceousMod.MOD_ID, "geo/entity/bearowl.geo.json");
	private static final ResourceLocation ANIMATIONS = new ResourceLocation(CroodaceousMod.MOD_ID, "animations/entity/bearowl.animation.json");

	@Override
	public ResourceLocation getModelResource(Bearowl animatable) {
		return MODEL;
	}

	@Override
	public ResourceLocation getTextureResource(Bearowl entity) {
		return TEXTURE;
	}

	@Override
	public ResourceLocation getAnimationResource(Bearowl animatable) {
		return ANIMATIONS;
	}

	@Override
	public void setCustomAnimations(Bearowl animatable, long instanceId, AnimationState<Bearowl> animationState) {
		super.setCustomAnimations(animatable, instanceId, animationState);
		// calculate head rotation
		final float xRot = -Mth.lerp(animationState.getPartialTick(), animatable.xRotO, animatable.getXRot());
		final float yBodyRot = Mth.rotLerp(animationState.getPartialTick(), animatable.yBodyRotO, animatable.yBodyRot);
		final float yHeadRot = Mth.rotLerp(animationState.getPartialTick(), animatable.yHeadRotO, animatable.yHeadRot);
		final float yRot = -(yHeadRot - yBodyRot);

		// rotate neck bone
		float xRotRadians = (float) (Math.toRadians(xRot) * 0.5F);
		float yRotRadians = (float) (Math.toRadians(yRot) * 0.5F);
		final var neck = this.getBone("neck");

		if (neck.isPresent()) {
			neck.get().setRotX(xRotRadians);
			neck.get().setRotY(yRotRadians);

			// rotate head bone
			final var head = this.getBone("head");

			if (head.isPresent()) {
				head.get().setRotX(xRotRadians);
				head.get().setRotY(yRotRadians);
			}

		}
	}
}