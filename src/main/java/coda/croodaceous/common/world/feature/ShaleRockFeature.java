package coda.croodaceous.common.world.feature;

import coda.croodaceous.registry.CEBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public class ShaleRockFeature extends Feature<BlockStateConfiguration> {
    public ShaleRockFeature(Codec<BlockStateConfiguration> p_65248_) {
        super(p_65248_);
    }

    public boolean place(FeaturePlaceContext<BlockStateConfiguration> p_159471_) {
        BlockPos blockpos = p_159471_.origin();
        WorldGenLevel worldgenlevel = p_159471_.level();
        RandomSource randomsource = p_159471_.random();

        int width = 2;
        int height = randomsource.nextInt(10) + 4;

        double remove = 3 + Math.cos(height * -(Math.PI / height));
        for (BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.offset(-width, -height, -width), blockpos.offset(width, height, width))) {
            for (int k = -width; k <= width; k++) {
                for (int m = -width; m <= width; m++) {
                    for (int l = -height; l < height; l++) {
                        if (k * k + m * m <= remove * remove) {
                            worldgenlevel.setBlock(blockpos1.offset(k, l, m), CEBlocks.HOODOO_SHALE.get().defaultBlockState(), 4);
                        }
                    }
                }
            }

            blockpos = blockpos.offset(-1 + randomsource.nextInt(2), -randomsource.nextInt(2), -1 + randomsource.nextInt(2));
        }
        return true;
    }
}