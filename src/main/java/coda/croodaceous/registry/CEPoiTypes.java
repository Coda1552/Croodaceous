package coda.croodaceous.registry;

import coda.croodaceous.CroodaceousMod;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class CEPoiTypes {
	public static final DeferredRegister<PoiType> POIS = DeferredRegister.create(ForgeRegistries.POI_TYPES, CroodaceousMod.MOD_ID);
	
	 public static final RegistryObject<PoiType> RAMU_NEST = POIS.register("poi_ramu_nest", () ->
			 new PoiType(PoiTypes.getBlockStates(CEBlocks.RAMU_NEST.get()), 1, 1));

}
