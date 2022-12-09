package coda.croodaceous.registry;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.blocks.RamuNestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CEBlocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CroodaceousMod.MOD_ID);
	
	public static final RegistryObject<Block> RAMU_NEST = BLOCKS.register("ramu_nest", RamuNestBlock::new);
	
}