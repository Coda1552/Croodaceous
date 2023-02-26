package coda.croodaceous.client.model;

import coda.croodaceous.common.entities.Liyote;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class LiyoteModel<T extends Liyote> extends EntityModel<T> {
	private final ModelPart base;
	private final ModelPart head;
	private final ModelPart chest;
	private final ModelPart snout;
	private final ModelPart tongue;
	private final ModelPart body;
	private final ModelPart tail;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;
	private final ModelPart leftArm;
	private final ModelPart rightArm;
	private final ModelPart earLeft;
	private final ModelPart earRight;

	public LiyoteModel(ModelPart root) {
		this.base = root.getChild("root");
		this.head = base.getChild("head");
		this.earLeft = head.getChild("earLeft");
		this.earRight = head.getChild("earRight");
		this.snout = head.getChild("snout");
		this.tongue = snout.getChild("tongue");
		this.chest = base.getChild("chest");
		this.body = chest.getChild("body");
		this.leftLeg = body.getChild("leftLeg");
		this.rightLeg = body.getChild("rightLeg");
		this.tail = body.getChild("tail");
		this.leftArm = chest.getChild("leftArm");
		this.rightArm = chest.getChild("rightArm");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition chest = root.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(0, 21).addBox(-3.0F, -2.5F, -2.5F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, -4.0F));

		PartDefinition body = chest.addOrReplaceChild("body", CubeListBuilder.create().texOffs(37, 2).addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, -0.2618F, 0.0F, 0.0F));

		PartDefinition leftLeg = body.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(23, 22).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.0F, 0.0F, 6.0F, 0.3491F, 0.0F, 0.3491F));

		PartDefinition rightLeg = body.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(23, 22).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.0F, 6.0F, 0.3491F, 0.0F, -0.3491F));

		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(33, 16).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 7.5F, -0.1745F, 0.0F, 0.0F));

		PartDefinition leftArm = chest.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(32, 18).mirror().addBox(-1.0F, -1.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, 1.5F, 0.0F, -0.1745F, 0.0F, 0.2618F));

		PartDefinition rightArm = chest.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(32, 18).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 1.5F, 0.0F, -0.1745F, 0.0F, -0.2618F));

		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 3).addBox(-2.5F, -2.5F, -5.0F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -9.0F, -5.5F));

		PartDefinition snout = head.addOrReplaceChild("snout", CubeListBuilder.create().texOffs(0, 14).addBox(-1.5F, -1.5F, -3.0F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, -5.0F));

		PartDefinition tongue = snout.addOrReplaceChild("tongue", CubeListBuilder.create().texOffs(32, 11).addBox(0.0F, 0.0F, -1.75F, 0.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -0.5F, -1.0F, 0.0F, 0.0F, 0.0349F));

		PartDefinition earLeft = head.addOrReplaceChild("earLeft", CubeListBuilder.create().texOffs(33, 3).mirror().addBox(-5.5F, -3.0F, 0.0F, 6.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.0F, -2.5F, -2.5F));

		PartDefinition earRight = head.addOrReplaceChild("earRight", CubeListBuilder.create().texOffs(33, 3).addBox(-0.5F, -3.0F, 0.0F, 6.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -2.5F, -2.5F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		base.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}