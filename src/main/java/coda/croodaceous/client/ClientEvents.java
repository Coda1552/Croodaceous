package coda.croodaceous.client;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.client.render.*;
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
    }
    
    @SubscribeEvent
    public static void onRegisterRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CEEntities.LIYOTE.get(), LiyoteRenderer::new);
        event.registerEntityRenderer(CEEntities.BEAROWL.get(), BearOwlRenderer::new);
        event.registerEntityRenderer(CEEntities.RAMU.get(), RamuRenderer::new);
        event.registerEntityRenderer(CEEntities.FANG_FLY.get(), FangFlyRenderer::new);
        event.registerEntityRenderer(CEEntities.JACKROBAT.get(), JackrobatRenderer::new);
        event.registerEntityRenderer(CEEntities.TRIP_GERBIL.get(), TripGerbilRenderer::new);
        event.registerEntityRenderer(CEEntities.BEAR_PEAR.get(), BearPearRenderer::new);
        event.registerEntityRenderer(CEEntities.TRIP_GERBIL.get(), TripGerbilRenderer::new);
        event.registerEntityRenderer(CEEntities.TURTLE_DOVE.get(), TurtleDoveRenderer::new);
    }
}
