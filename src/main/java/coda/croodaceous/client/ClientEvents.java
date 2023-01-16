package coda.croodaceous.client;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.client.render.BearOwlRenderer;
import coda.croodaceous.client.render.LiyoteRenderer;
import coda.croodaceous.client.render.RamuRenderer;
import coda.croodaceous.registry.CEEntities;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
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
    }
}
