package coda.croodaceous.client.render.layer;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.entities.Bearowl;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class BearowlEyesLayer extends GeoRenderLayer<Bearowl> {
    private static final ResourceLocation EYES = new ResourceLocation(CroodaceousMod.MOD_ID, "textures/entity/bearowl/eyes.png");
    private static final ResourceLocation EYES_SLEEPING = new ResourceLocation(CroodaceousMod.MOD_ID, "textures/entity/bearowl/eyes_sleeping.png");

    public BearowlEyesLayer(GeoRenderer<Bearowl> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, Bearowl animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
       VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(getTextureResource(animatable)));

       this.getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, renderType, consumer, partialTick, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected ResourceLocation getTextureResource(Bearowl animatable) {
        if (animatable.isSleepingState()) {
            return EYES_SLEEPING;
        }
        return EYES;
    }
}
