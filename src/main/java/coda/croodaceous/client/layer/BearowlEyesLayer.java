package coda.croodaceous.client.layer;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.entities.Bearowl;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class BearowlEyesLayer extends GeoLayerRenderer<Bearowl> {

    private static final ResourceLocation EYES = new ResourceLocation(CroodaceousMod.MOD_ID, "textures/entity/bearowl/eyes.png");
    private static final ResourceLocation EYES_SLEEPING = new ResourceLocation(CroodaceousMod.MOD_ID, "textures/entity/bearowl/eyes_sleeping.png");

    public BearowlEyesLayer(IGeoRenderer<Bearowl> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Bearowl entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        renderModel(getEntityModel(), getEntityTexture(entity), matrixStackIn, bufferIn, packedLightIn, entity, partialTicks, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected ResourceLocation getEntityTexture(Bearowl entity) {
        if(entity.sleeping) {
            return EYES_SLEEPING;
        }
        return EYES;
    }
}
