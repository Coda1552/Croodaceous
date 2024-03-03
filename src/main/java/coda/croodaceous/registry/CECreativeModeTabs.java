package coda.croodaceous.registry;

import coda.croodaceous.CroodaceousMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = CroodaceousMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CECreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CroodaceousMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("croodaceous", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.croodaceous"))
            .icon(CEItems.RAMU_EGG.get()::getDefaultInstance)
            .displayItems((itemDisplayParameters, output) -> {
                CEItems.ITEMS.getEntries().forEach(itemRegistryObject -> output.accept(itemRegistryObject.get()));
            })
            .build()
    );

}
