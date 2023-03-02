package coda.croodaceous.client.render;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.client.CEModelLayers;
import coda.croodaceous.client.model.FangFlyModel;
import coda.croodaceous.common.entities.FangFly;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FangFlyRenderer extends MobRenderer<FangFly, FangFlyModel<FangFly>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CroodaceousMod.MOD_ID, "textures/entity/fang_fly.png");

    public FangFlyRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new FangFlyModel<>(pContext.bakeLayer(CEModelLayers.FANG_FLY)), 0.25F);
    }

    @Override
    public ResourceLocation getTextureLocation(FangFly pEntity) {
        return TEXTURE;
    }
}
