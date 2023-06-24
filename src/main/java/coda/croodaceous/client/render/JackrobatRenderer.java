package coda.croodaceous.client.render;

import coda.croodaceous.client.model.JackrobatModel;
import coda.croodaceous.common.entities.Jackrobat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import javax.annotation.Nullable;

public class JackrobatRenderer<T extends Jackrobat> extends GeoEntityRenderer<T> {
	private final ItemRenderer itemRenderer;

	private MultiBufferSource renderTypeBuffer;
	private T animatable;
	private ItemStack itemStack;

	public JackrobatRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new JackrobatModel<T>());
		this.itemRenderer = mgr.getItemRenderer();
	}

	@Override
	public void renderRecursively(GeoBone bone, PoseStack stack, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		if ("snout".equals(bone.name) && !itemStack.isEmpty()) {
			stack.pushPose();
			stack.mulPose(Vector3f.XP.rotation(0));
			stack.translate(0.0001F, 0.125F, -0.45F);
			itemRenderer.renderStatic(itemStack, ItemTransforms.TransformType.GROUND, packedLightIn, packedOverlayIn, stack, renderTypeBuffer, 0);
			RenderType type = getRenderType(animatable, 1F, stack, renderTypeBuffer, null, packedLightIn, getTextureLocation(animatable));
			bufferIn = renderTypeBuffer.getBuffer(type);
			stack.popPose();
		}
		super.renderRecursively(bone, stack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}

	@Override
	public void renderLate(T animatable, PoseStack stackIn, float ticks, MultiBufferSource renderTypeBuffer, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float partialTicks) {
		this.renderTypeBuffer = renderTypeBuffer;
		this.animatable = animatable;
		this.itemStack = animatable.getMainHandItem();
	}

	@Override
	public RenderType getRenderType(T animatable, float partialTicks, PoseStack stack, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
		return RenderType.entityCutout(textureLocation);
	}
}