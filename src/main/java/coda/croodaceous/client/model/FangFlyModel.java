package coda.croodaceous.client.model;

import coda.croodaceous.common.entities.FangFly;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class FangFlyModel<T extends FangFly> extends HierarchicalModel<T> {
	private final ModelPart root;

	public FangFlyModel(ModelPart root) {
		super();
		this.root = root.getChild("root");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		// root
		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		// body
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F, CubeDeformation.NONE), PartPose.offset(0.0F, -7.0F, 0.0F));
		PartDefinition butt = body.addOrReplaceChild("butt", CubeListBuilder.create().texOffs(0, 9).addBox(-2.5F, -1.0F, 0.0F, 5.0F, 5.0F, 6.0F, CubeDeformation.NONE), PartPose.offsetAndRotation(0.0F, -1.0F, 1.5F, -0.2618F, 0.0F, 0.0F));
		butt.addOrReplaceChild("stinger", CubeListBuilder.create().texOffs(0, 3).addBox(0.0F, -0.5F, 0.0F, 0.0F, 1.0F, 2.0F, CubeDeformation.NONE), PartPose.offset(0.0F, 1.5F, 6.0F));
		butt.addOrReplaceChild("spikes", CubeListBuilder.create().texOffs(0, 1).addBox(0.0F, -1.0F, -3.0F, 0.0F, 1.0F, 6.0F, CubeDeformation.NONE), PartPose.offset(0.0F, -1.0F, 3.0F));

		// head
		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 21).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 3.0F, 2.0F, CubeDeformation.NONE), PartPose.offset(0.0F, 0.5F, -1.5F));
		head.addOrReplaceChild("leftantenna", CubeListBuilder.create().texOffs(7, 26).mirror().addBox(0.0F, -4.0F, -1.0F, 0.0F, 4.0F, 2.0F, CubeDeformation.NONE).mirror(false), PartPose.offsetAndRotation(-0.5F, -1.0F, -1.0F, 0.9599F, 0.2618F, -0.3491F));
		head.addOrReplaceChild("rightantenna", CubeListBuilder.create().texOffs(7, 26).addBox(0.0F, -4.0F, -1.0F, 0.0F, 4.0F, 2.0F, CubeDeformation.NONE), PartPose.offsetAndRotation(0.5F, -1.0F, -1.0F, 0.9599F, -0.2618F, 0.3491F));

		// fangs
		head.addOrReplaceChild("leftboi", CubeListBuilder.create().texOffs(1, 8).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, CubeDeformation.NONE), PartPose.offsetAndRotation(-0.4F, 1.5F, -1.5F, -1.1345F, 0.4363F, 0.0F));
		head.addOrReplaceChild("rightboi", CubeListBuilder.create().texOffs(1, 8).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, CubeDeformation.NONE), PartPose.offsetAndRotation(0.4F, 1.5F, -1.5F, -1.1345F, -0.4363F, 0.0F));

		// legs
		body.addOrReplaceChild("leftfrontleg", CubeListBuilder.create().texOffs(15, 0).mirror().addBox(-3.0F, -0.5F, 0.0F, 3.0F, 7.0F, 0.0F, CubeDeformation.NONE).mirror(false), PartPose.offsetAndRotation(-1.5F, 0.5F, -1.0F, 0.0F, -0.2618F, 0.0F));
		body.addOrReplaceChild("leftlastleg", CubeListBuilder.create().texOffs(15, 0).mirror().addBox(-3.0F, -0.5F, 0.0F, 3.0F, 7.0F, 0.0F, CubeDeformation.NONE).mirror(false), PartPose.offsetAndRotation(-1.5F, 0.5F, 0.0F, 0.0F, 0.2618F, 0.0F));
		body.addOrReplaceChild("leftmidleg", CubeListBuilder.create().texOffs(15, 0).mirror().addBox(-3.0F, -0.5F, 0.0F, 3.0F, 7.0F, 0.0F, CubeDeformation.NONE).mirror(false), PartPose.offset(-1.5F, 0.5F, -0.5F));
		body.addOrReplaceChild("rightmidleg", CubeListBuilder.create().texOffs(15, 0).addBox(0.0F, -0.5F, 0.0F, 3.0F, 7.0F, 0.0F, CubeDeformation.NONE), PartPose.offset(1.5F, 0.5F, -0.5F));
		body.addOrReplaceChild("rightlastleg", CubeListBuilder.create().texOffs(15, 0).addBox(0.0F, -0.5F, 0.0F, 3.0F, 7.0F, 0.0F, CubeDeformation.NONE), PartPose.offsetAndRotation(1.5F, 0.5F, 0.0F, 0.0F, -0.2618F, 0.0F));
		body.addOrReplaceChild("rightfrontleg", CubeListBuilder.create().texOffs(15, 0).addBox(0.0F, -0.5F, 0.0F, 3.0F, 7.0F, 0.0F, CubeDeformation.NONE), PartPose.offsetAndRotation(1.5F, 0.5F, -1.0F, 0.0F, 0.2618F, 0.0F));

		// wings
		body.addOrReplaceChild("leftwing", CubeListBuilder.create().texOffs(12, 21).mirror().addBox(0.0F, -7.0F, -0.5F, 0.0F, 7.0F, 3.0F, CubeDeformation.NONE).mirror(false), PartPose.offset(-0.5F, -1.0F, -1.0F));
		body.addOrReplaceChild("rightwing", CubeListBuilder.create().texOffs(12, 21).addBox(0.0F, -7.0F, -0.5F, 0.0F, 7.0F, 3.0F, CubeDeformation.NONE), PartPose.offset(0.5F, -1.0F, -1.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		animate(entity.flyIdleAnimationState, Animations.IDLE_FLY, ageInTicks);
	}

	private static final class Animations {
		public static final AnimationDefinition IDLE_FLY = AnimationDefinition.Builder.withLength(1f).looping()
				.addAnimation("body",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5f, KeyframeAnimations.posVec(0f, 4f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM))).build();
	}
}