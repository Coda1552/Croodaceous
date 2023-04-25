package coda.croodaceous.client.render;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.client.model.BearPearModel;
import coda.croodaceous.common.entities.BearPear;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
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

	@Override
	public RenderType getRenderType(BearPear animatable, float partialTick, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, ResourceLocation texture) {
		return RenderType.entityCutoutNoCull(texture);
	}
}