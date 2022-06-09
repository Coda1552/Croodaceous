package com.anar4732.croodaceous.registry;

import com.anar4732.croodaceous.CroodaceousMod;
import com.anar4732.croodaceous.common.blocks.RamuNestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CEBlocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CroodaceousMod.ID);
	
	public static final RegistryObject<Block> RAMU_NEST = BLOCKS.register("ramu_nest", RamuNestBlock::new);
	
}