package coda.croodaceous.client;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.client.model.FangFlyModel;
import coda.croodaceous.client.model.LiyoteModel;
import coda.croodaceous.client.render.geo.BearOwlRenderer;
import coda.croodaceous.client.render.geo.BearPearRenderer;
import coda.croodaceous.client.render.geo.FangFlyRenderer;
import coda.croodaceous.client.render.geo.JackrobatRenderer;
import coda.croodaceous.client.render.LiyoteRenderer;
import coda.croodaceous.client.render.geo.RamuRenderer;
import coda.croodaceous.client.render.geo.TripGerbilRenderer;
import coda.croodaceous.registry.CEEntities;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CroodaceousMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent e) {
        EntityRenderers.register(CEEntities.LIYOTE.get(), LiyoteRenderer::new);
        EntityRenderers.register(CEEntities.BEAROWL.get(), BearOwlRenderer::new);
        EntityRenderers.register(CEEntities.RAMU.get(), RamuRenderer::new);
        EntityRenderers.register(CEEntities.FANG_FLY.get(), FangFlyRenderer::new);
        EntityRenderers.register(CEEntities.JACKROBAT.get(), JackrobatRenderer::new);
        EntityRenderers.register(CEEntities.TRIP_GERBIL.get(), TripGerbilRenderer::new);
        EntityRenderers.register(CEEntities.BEAR_PEAR.get(), BearPearRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(CEModelLayers.LIYOTE, LiyoteModel::createBodyLayer);
        e.registerLayerDefinition(CEModelLayers.FANG_FLY, FangFlyModel::createBodyLayer);
    }
}
