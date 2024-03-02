package coda.croodaceous.client.render;

import coda.croodaceous.client.model.SimpleGeoModel;
import coda.croodaceous.common.entities.Liyote;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LiyoteRenderer extends GeoEntityRenderer<Liyote> {
	private final ItemRenderer itemRenderer;
	
	public LiyoteRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new SimpleGeoModel<>("liyote"));
		this.itemRenderer = mgr.getItemRenderer();
	}

	@Override
	public void renderRecursively(PoseStack poseStack, Liyote animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		if ("snout".equals(bone.getName())) {
			renderItem(bone, animatable.getEatingItem(), poseStack, bufferSource, packedLight);
		}
		super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public RenderType getRenderType(Liyote animatable, ResourceLocation texture, @org.jetbrains.annotations.Nullable MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityTranslucent(texture);
	}

	private void renderItem(final GeoBone bone, final ItemStack itemStack, PoseStack stack, MultiBufferSource renderTypeBuffer, int packedLightIn) {
		if (itemStack.isEmpty()) {
			return;
		}
		stack.pushPose();
		stack.mulPose(Axis.XP.rotation(130));
		stack.translate(0, 0.5, 0.8);
		itemRenderer.renderStatic(itemStack, ItemDisplayContext.GROUND, packedLightIn, OverlayTexture.NO_OVERLAY, stack, renderTypeBuffer, Minecraft.getInstance().level, 0);
		stack.popPose();
	}

}