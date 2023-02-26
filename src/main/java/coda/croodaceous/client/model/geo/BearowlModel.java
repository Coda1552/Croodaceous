package coda.croodaceous.client.model.geo;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.entities.Bearowl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;

public class BearowlModel extends SimpleGeoModel<Bearowl> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(CroodaceousMod.MOD_ID, "textures/entity/bearowl/bearowl.png");

	public BearowlModel() {
		super(CroodaceousMod.MOD_ID, "bearowl");
	}
	
	@Override
	public ResourceLocation getTextureResource(Bearowl entity) {
		return TEXTURE;
	}

	@Override
	public void setCustomAnimations(Bearowl animatable, int instanceId, AnimationEvent animationEvent) {
		super.setCustomAnimations(animatable, instanceId, animationEvent);
		// calculate head rotation
		final float xRot = -Mth.lerp(animationEvent.getPartialTick(), animatable.xRotO, animatable.getXRot());
		final float yBodyRot = Mth.rotLerp(animationEvent.getPartialTick(), animatable.yBodyRotO, animatable.yBodyRot);
		final float yHeadRot = Mth.rotLerp(animationEvent.getPartialTick(), animatable.yHeadRotO, animatable.yHeadRot);
		final float yRot = -(yHeadRot - yBodyRot);
		// rotate neck bone
		float xRotRadians = (float) (Math.toRadians(xRot) * 0.5F);
		float yRotRadians = (float) (Math.toRadians(yRot) * 0.5F);
		final IBone neck = this.getBone("neck");
		neck.setRotationX(xRotRadians);
		neck.setRotationY(yRotRadians);
		// rotate head bone
		final IBone head = this.getBone("head");
		head.setRotationX(xRotRadians);
		head.setRotationY(yRotRadians);
	}
}