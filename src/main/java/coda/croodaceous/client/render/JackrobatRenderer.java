package coda.croodaceous.client.render;

import coda.croodaceous.client.model.JackrobatModel;
import coda.croodaceous.common.entities.Jackrobat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class JackrobatRenderer<T extends Jackrobat> extends GeoEntityRenderer<T> {
	private final ItemRenderer itemRenderer;

	public JackrobatRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new JackrobatModel<>());
		this.itemRenderer = mgr.getItemRenderer();
	}

	@Override
	public void renderRecursively(PoseStack stack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		ItemStack itemStack = animatable.getMainHandItem();
		if ("snout".equals(bone.getName()) && !itemStack.isEmpty()) {
			stack.pushPose();
			stack.mulPose(Axis.XP.rotation(0));
			stack.translate(0.0001F, 0.125F, -0.45F);
			itemRenderer.renderStatic(itemStack, ItemDisplayContext.GROUND, packedLightIn, packedOverlayIn, stack, bufferSource, Minecraft.getInstance().level, 0);
			RenderType type = getRenderType(animatable, getTextureLocation(animatable), bufferSource,1F);
			buffer = bufferSource.getBuffer(type);
			stack.popPose();
		}
		super.renderRecursively(stack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}

	@Override
	public RenderType getRenderType(T animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityCutout(texture);
	}

	@Override
	public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		if (entity.isBaby()) {
			poseStack.scale(0.5F, 0.5F, 0.5F);
		}
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}
}