package coda.croodaceous.registry;

import coda.croodaceous.CroodaceousMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CESounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, CroodaceousMod.MOD_ID);

    public static final RegistryObject<SoundEvent> BEAR_PEAR_AMBIENT = create("bear_pear.ambient");
    public static final RegistryObject<SoundEvent> BEAR_PEAR_HURT = create("bear_pear.hurt");
    public static final RegistryObject<SoundEvent> BEAR_PEAR_DEATH = create("bear_pear.death");
    public static final RegistryObject<SoundEvent> BEAR_PEAR_DETACH = create("bear_pear.detach");
    public static final RegistryObject<SoundEvent> BEAR_PEAR_ATTACH = create("bear_pear.attach");
    public static final RegistryObject<SoundEvent> BEAR_PEAR_BITE = create("bear_pear.bite"); // todo - make the bite sound play when it does the drop attack
    public static final RegistryObject<SoundEvent> BEAR_PEAR_BUMP = create("bear_pear.bump"); // todo

    private static RegistryObject<SoundEvent> create(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(CroodaceousMod.MOD_ID, name)));
    }
}