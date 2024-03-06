package coda.croodaceous.client.render;

import coda.croodaceous.client.model.SimpleGeoModel;
import coda.croodaceous.common.entities.Ramu;
import coda.croodaceous.registry.CEItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.RenderUtils;

public class RamuRenderer extends GeoEntityRenderer<Ramu> {

	private static final ItemStack egg = new ItemStack(CEItems.RAMU_EGG.get());
	
	public RamuRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new SimpleGeoModel<>("ramu"));
	}

	@Override
	public void renderRecursively(PoseStack poseStack, Ramu animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		if (bone.getName().equals("head")) {
			if (animatable.carriesEgg()) {
				poseStack.pushPose();
				RenderUtils.translateMatrixToBone(poseStack, bone);
				RenderUtils.translateToPivotPoint(poseStack, bone);
				RenderUtils.rotateMatrixAroundBone(poseStack, bone);
				RenderUtils.scaleMatrixForBone(poseStack, bone);
				RenderUtils.translateAwayFromPivotPoint(poseStack, bone);
				poseStack.mulPose(Axis.XP.rotation(130));
				poseStack.translate(0, 0.1, 2.3);
				Minecraft.getInstance().getItemRenderer().renderStatic(egg, ItemDisplayContext.GROUND, packedLight, packedOverlay, poseStack, bufferSource, Minecraft.getInstance().level, 0);
				RenderType type = getRenderType(animatable, getTextureLocation(animatable), bufferSource, partialTick);
				buffer = bufferSource.getBuffer(type);
				poseStack.popPose();
			}
		}
		super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public void render(Ramu entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		if (entity.isBaby()) {
			poseStack.scale(0.5F, 0.5F, 0.5F);
		}
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}
}