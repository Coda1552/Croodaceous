package coda.croodaceous.client;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.client.render.*;
import coda.croodaceous.registry.CEBlocks;
import coda.croodaceous.registry.CEEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CroodaceousMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent e) {

    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block e) {
        e.register((p_92621_, level, pos, p_92624_) -> level != null && pos != null ? BiomeColors.getAverageGrassColor(level, pos) : GrassColor.get(0.9D, 0.1D), CEBlocks.DESERT_BAOBAB_LEAVES.get());
    }


    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item e) {
        e.register((p1, p2) -> GrassColor.get(0.9D, 0.1D), CEBlocks.DESERT_BAOBAB_LEAVES.get().asItem());
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CEEntities.LIYOTE.get(), LiyoteRenderer::new);
        event.registerEntityRenderer(CEEntities.BEAROWL.get(), BearOwlRenderer::new);
        event.registerEntityRenderer(CEEntities.RAMU.get(), RamuRenderer::new);
        event.registerEntityRenderer(CEEntities.FANG_FLY.get(), FangFlyRenderer::new);
        event.registerEntityRenderer(CEEntities.JACKROBAT.get(), JackrobatRenderer::new);
        event.registerEntityRenderer(CEEntities.TRIP_GERBIL.get(), TripGerbilRenderer::new);
        event.registerEntityRenderer(CEEntities.BEAR_PEAR.get(), BearPearRenderer::new);
        event.registerEntityRenderer(CEEntities.TURTLE_DOVE.get(), TurtleDoveRenderer::new);
    }
}
