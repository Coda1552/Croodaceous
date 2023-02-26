package coda.croodaceous.client.render;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.client.CEModelLayers;
import coda.croodaceous.client.model.LiyoteModel;
import coda.croodaceous.common.entities.Liyote;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class LiyoteRenderer extends MobRenderer<Liyote, LiyoteModel<Liyote>> {
    private static final ResourceLocation TEX = new ResourceLocation(CroodaceousMod.MOD_ID, "textures/entity/liyote.png");

    public LiyoteRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new LiyoteModel<>(pContext.bakeLayer(CEModelLayers.LIYOTE)), 0.6F);
    }

    @Override
    public ResourceLocation getTextureLocation(Liyote pEntity) {
        return TEX;
    }
}
