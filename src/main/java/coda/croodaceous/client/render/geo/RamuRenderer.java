package coda.croodaceous.client.render.geo;

import coda.croodaceous.common.entities.Ramu;
import coda.croodaceous.registry.CEItems;
import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.client.model.geo.SimpleGeoModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.util.RenderUtils;

import javax.annotation.Nullable;

public class RamuRenderer extends GeoEntityRenderer<Ramu> {
	private MultiBufferSource renderTypeBuffer;
	private Ramu animatable;
	
	private static final ItemStack egg = new ItemStack(CEItems.RAMU_EGG.get());
	
	public RamuRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new SimpleGeoModel<>(CroodaceousMod.MOD_ID, "ramu"));
	}

	@Override
	public void renderRecursively(GeoBone bone, PoseStack stack, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		if (bone.name.equals("head")) {
			if (animatable.carriesEgg()) {
				stack.pushPose();
				RenderUtils.translateMatrixToBone(stack, bone);
				RenderUtils.translateToPivotPoint(stack, bone);
				RenderUtils.rotateMatrixAroundBone(stack, bone);
				RenderUtils.scaleMatrixForBone(stack, bone);
				RenderUtils.translateAwayFromPivotPoint(stack, bone);
				stack.mulPose(Vector3f.XP.rotation(130));
				stack.translate(0, 0.1, 2.3);
				Minecraft.getInstance().getItemRenderer().renderStatic(egg, ItemTransforms.TransformType.GROUND, packedLightIn, packedOverlayIn, stack, renderTypeBuffer, 0);
				RenderType type = getRenderType(animatable, 1F, stack, renderTypeBuffer, null, packedLightIn, getTextureLocation(animatable));
				bufferIn = renderTypeBuffer.getBuffer(type);
				stack.popPose();
			}
		}
		super.renderRecursively(bone, stack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
	@Override
	public void renderLate(Ramu animatable, PoseStack stackIn, float ticks, MultiBufferSource renderTypeBuffer, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float partialTicks) {
		this.renderTypeBuffer = renderTypeBuffer;
		this.animatable = animatable;
	}
	
	@Override
	public RenderType getRenderType(Ramu animatable, float partialTicks, PoseStack stack, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
		return RenderType.entityTranslucent(textureLocation);
	}
	
}