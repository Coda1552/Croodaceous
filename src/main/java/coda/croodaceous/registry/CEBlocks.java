package coda.croodaceous.registry;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.blocks.BaobabSaplingBlock;
import coda.croodaceous.common.blocks.BranchesBlock;
import coda.croodaceous.common.blocks.BranchesWallBlock;
import coda.croodaceous.common.blocks.CESandBlock;
import coda.croodaceous.common.blocks.DryBushBlock;
import coda.croodaceous.common.blocks.RamuNestBlock;
import coda.croodaceous.common.world.tree.DesertBaobabTreeGrower;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class CEBlocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CroodaceousMod.MOD_ID);

    // Other
	public static final RegistryObject<Block> RAMU_NEST = register("ramu_nest", RamuNestBlock::new);
	public static final RegistryObject<Block> DRY_BUSH = register("dry_bush", () -> new DryBushBlock(BlockBehaviour.Properties.of().instabreak().noCollission().sound(SoundType.GRASS)));

    // Desert Baobab
    public static final RegistryObject<Block> DESERT_BAOBAB_SAPLING = register("desert_baobab_sapling", () -> new BaobabSaplingBlock(new DesertBaobabTreeGrower(), BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).strength(0).noCollission().sound(SoundType.GRASS)));
    public static final RegistryObject<Block> STRIPPED_DESERT_BAOBAB_LOG = register("stripped_desert_baobab_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).strength(2.0f).sound(SoundType.WOOD)));
    public static final RegistryObject<Block> STRIPPED_DESERT_BAOBAB_WOOD = register("stripped_desert_baobab_wood", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).strength(2.0f).sound(SoundType.WOOD)));
    public static final RegistryObject<Block> DESERT_BAOBAB_LOG = registerRotatedPillar("desert_baobab_log", CEBlocks.STRIPPED_DESERT_BAOBAB_LOG, BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).strength(2.0f).sound(SoundType.WOOD));
    public static final RegistryObject<Block> DESERT_BAOBAB_WOOD = registerRotatedPillar("desert_baobab_wood", CEBlocks.STRIPPED_DESERT_BAOBAB_WOOD, BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).strength(2.0f).sound(SoundType.WOOD));
    public static final RegistryObject<Block> DESERT_BAOBAB_PLANKS = register("desert_baobab_planks", () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)));
    public static final RegistryObject<Block> DESERT_BAOBAB_STAIRS = register("desert_baobab_stairs", () -> new StairBlock(() -> DESERT_BAOBAB_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.OAK_STAIRS)));
    public static final RegistryObject<Block> DESERT_BAOBAB_FENCE = register("desert_baobab_fence", () -> new FenceBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE)));
    public static final RegistryObject<Block> DESERT_BAOBAB_SLAB = register("desert_baobab_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SLAB)));
    public static final RegistryObject<Block> DESERT_BAOBAB_PRESSURE_PLATE = register("desert_baobab_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.OAK_PRESSURE_PLATE), CEBlockSetTypes.DESERT_BAOBAB));
    public static final RegistryObject<Block> DESERT_BAOBAB_BUTTON = register("desert_baobab_button", () -> new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.OAK_BUTTON), CEBlockSetTypes.DESERT_BAOBAB, 30, true));
    public static final RegistryObject<Block> DESERT_BAOBAB_TRAPDOOR = register("desert_baobab_trapdoor", () -> new TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_TRAPDOOR), CEBlockSetTypes.DESERT_BAOBAB));
    public static final RegistryObject<Block> DESERT_BAOBAB_DOOR = register("desert_baobab_door", () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_DOOR), CEBlockSetTypes.DESERT_BAOBAB));
    public static final RegistryObject<Block> DESERT_BAOBAB_FENCE_GATE = register("desert_baobab_fence_gate", () -> new FenceGateBlock(BlockBehaviour.Properties.copy(Blocks.OAK_DOOR), CEWoodTypes.DESERT_BAOBAB));
    public static final RegistryObject<Block> DESERT_BAOBAB_BRANCHES = BLOCKS.register("desert_baobab_branches", () -> new BranchesBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).sound(SoundType.GRASS).noCollission().instabreak()));
    public static final RegistryObject<Block> DESERT_BAOBAB_WALL_BRANCHES = BLOCKS.register("desert_baobab_wall_branches", () -> new BranchesWallBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).sound(SoundType.GRASS).noCollission().instabreak()));
    public static final RegistryObject<Block> DESERT_BAOBAB_LEAVES = register("desert_baobab_leaves", () -> new LeavesBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).sound(SoundType.GRASS).strength(0.2F).randomTicks().noOcclusion()));

    // Croodaceous Sand
    public static final RegistryObject<Block> DESOLATE_SAND = register("desolate_sand", () -> new CESandBlock(0xe7ba8a, BlockBehaviour.Properties.of().strength(0.5f).sound(SoundType.SAND)));
    public static final RegistryObject<Block> DESOLATE_SANDSTONE = register("desolate_sandstone", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(1.2f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> CHISELED_DESOLATE_SANDSTONE = register("chiseled_desolate_sandstone", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(1.2f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> CUT_DESOLATE_SANDSTONE = register("cut_desolate_sandstone", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(1.2f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> SMOOTH_DESOLATE_SANDSTONE = register("smooth_desolate_sandstone", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(1.2f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> DESOLATE_SANDSTONE_STAIRS = register("desolate_sandstone_stairs", () -> new StairBlock(() -> DESOLATE_SANDSTONE.get().defaultBlockState(), BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(1.2f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> DESOLATE_SANDSTONE_SLAB = register("desolate_sandstone_slab", () -> new SlabBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(1.2f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> CUT_DESOLATE_SANDSTONE_SLAB = register("cut_desolate_sandstone_slab", () -> new SlabBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(1.2f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> SMOOTH_DESOLATE_SANDSTONE_SLAB = register("smooth_desolate_sandstone_slab", () -> new SlabBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(1.2f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> SMOOTH_DESOLATE_SANDSTONE_STAIRS = register("smooth_desolate_sandstone_stairs", () -> new StairBlock(() -> SMOOTH_DESOLATE_SANDSTONE.get().defaultBlockState(), BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(1.2f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> DESOLATE_SANDSTONE_WALL = register("desolate_sandstone_wall", () -> new WallBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(1.2f).requiresCorrectToolForDrops()));

    // Hoodoo Shale
    public static final RegistryObject<Block> HOODOO_SHALE = register("hoodoo_shale", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(1.5f, 6.0f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> POLISHED_HOODOO_SHALE = register("polished_hoodoo_shale", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(1.5f, 6.0f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> HOODOO_SHALE_TLES = register("hoodoo_shale_tiles", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(1.5f, 6.0f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> CHISELED_HOODOO_SHALE = register("chiseled_hoodoo_shale", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(1.5f, 6.0f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> HOODOO_SHALE_STAIRS = register("hoodoo_shale_stairs", () -> new StairBlock(() -> HOODOO_SHALE.get().defaultBlockState(), BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(1.5f, 6.0f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> POLISHED_HOODOO_SHALE_STAIRS = register("polished_hoodoo_shale_stairs", () -> new StairBlock(() -> POLISHED_HOODOO_SHALE.get().defaultBlockState(), BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(1.5f, 6.0f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> HOODOO_SHALE_SLAB = register("hoodoo_shale_slab", () -> new SlabBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(1.5f, 6.0f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> POLISHED_HOODOO_SHALE_SLAB = register("polished_hoodoo_shale_slab", () -> new SlabBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(1.5f, 6.0f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> HOODOO_SHALE_WALL = register("hoodoo_shale_wall", () -> new WallBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(1.5f, 6.0f).requiresCorrectToolForDrops()));

    private static RegistryObject<Block> registerRotatedPillar(String name, Supplier<Block> stripped, BlockBehaviour.Properties properties) {
        return register(name, () -> new RotatedPillarBlock(properties) {
            @Override
            public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
                if (toolAction == ToolActions.AXE_STRIP) {
                    return stripped.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
                }
                return super.getToolModifiedState(state, context, toolAction, simulate);
            }
        });
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block) {
		return register(name, block, new Item.Properties());
	}

	private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block, Item.Properties itemProperties) {
		return register(name, block, BlockItem::new, itemProperties);
	}

	private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block, BiFunction<Block, Item.Properties, BlockItem> item, Item.Properties itemProperties) {
		final RegistryObject<T> registryObject = BLOCKS.register(name, block);
		if (itemProperties != null)
			CEItems.ITEMS.register(name, () -> item == null ? new BlockItem(registryObject.get(), itemProperties) : item.apply(registryObject.get(), itemProperties));
		return registryObject;
	}
}