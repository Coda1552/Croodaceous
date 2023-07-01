package coda.croodaceous.common.world.tree;

import coda.croodaceous.CroodaceousMod;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

public class DesertBaobabTreeGrower extends AbstractTreeGrower {

    private static final ResourceLocation FEATURE_ID = new ResourceLocation(CroodaceousMod.MOD_ID, "desert_baobab");

    @Nullable
    @Override
    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource pRandom, boolean pLargeHive) {
        ConfiguredFeature<?, ?> cf = ServerLifecycleHooks.getCurrentServer().registryAccess().registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY).get(FEATURE_ID);
        if (null == cf) {
            throw new IllegalArgumentException("Failed to create holder for unknown configured feature '" + FEATURE_ID + "'");
        }
        return Holder.direct(cf);
    }
}

