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

    public static final RegistryObject<SoundEvent> BEAROWL_AMBIENT = create("bearowl.ambient");
    public static final RegistryObject<SoundEvent> BEAROWL_HURT = create("bearowl.hurt");
    public static final RegistryObject<SoundEvent> BEAROWL_DEATH = create("bearowl.death");
    public static final RegistryObject<SoundEvent> BEAROWL_ROAR = create("bearowl.roar");

    public static final RegistryObject<SoundEvent> LIYOTE_AMBIENT = create("liyote.ambient");
    public static final RegistryObject<SoundEvent> LIYOTE_HURT = create("liyote.hurt");
    public static final RegistryObject<SoundEvent> LIYOTE_DEATH = create("liyote.death");

    public static final RegistryObject<SoundEvent> RAMU_AMBIENT = create("ramu.ambient");
    public static final RegistryObject<SoundEvent> RAMU_HURT = create("ramu.hurt");
    public static final RegistryObject<SoundEvent> RAMU_DEATH = create("ramu.death");

    public static final RegistryObject<SoundEvent> TRIP_GERBIL_AMBIENT = create("trip_gerbil.ambient");
    public static final RegistryObject<SoundEvent> TRIP_GERBIL_HURT = create("trip_gerbil.hurt");
    public static final RegistryObject<SoundEvent> TRIP_GERBIL_DEATH = create("trip_gerbil.death");

    public static final RegistryObject<SoundEvent> TURTLE_DOVE_AMBIENT = create("turtle_dove.ambient");
    public static final RegistryObject<SoundEvent> TURTLE_DOVE_HURT = create("turtle_dove.hurt");
    public static final RegistryObject<SoundEvent> TURTLE_DOVE_DEATH = create("turtle_dove.death");

    private static RegistryObject<SoundEvent> create(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(CroodaceousMod.MOD_ID, name)));
    }
}