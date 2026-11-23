package com.hellaverse.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Fazenda 9x9: cerca de madeira, canal de agua no meio e fileiras de plantacao.
 * Aplana o terreno na altura de origem para a fazenda ficar reta.
 */
public class FarmFeature extends Feature<NoneFeatureConfiguration> {
    private static final int HALF = 4;

    public FarmFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        if (origin.getY() < level.getSeaLevel() + 1) {
            return false;
        }
        // Nao constroi sobre agua.
        if (!level.getFluidState(origin.below()).isEmpty()) {
            return false;
        }

        Block[] crops = {Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES};
        Block rowCrop = crops[random.nextInt(crops.length)];
        BlockState ground = level.getBlockState(origin.below());
        if (ground.isAir()) {
            ground = Blocks.DIRT.defaultBlockState();
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = -HALF; dx <= HALF; dx++) {
            for (int dz = -HALF; dz <= HALF; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int y = origin.getY();

                // Base solida e espaco livre acima.
                for (int by = y - 3; by < y - 1; by++) {
                    level.setBlock(pos.set(x, by, z), ground, 2);
                }
                for (int ay = y; ay <= y + 3; ay++) {
                    level.setBlock(pos.set(x, ay, z), Blocks.AIR.defaultBlockState(), 2);
                }

                boolean border = Math.abs(dx) == HALF || Math.abs(dz) == HALF;
                if (border) {
                    level.setBlock(pos.set(x, y - 1, z), ground, 2);
                    boolean gate = dz == HALF && dx == 0;
                    if (!gate) {
                        level.setBlock(pos.set(x, y, z), Blocks.OAK_FENCE.defaultBlockState(), 2);
                    }
                } else if (dx == 0) {
                    // Canal de agua no meio (hidrata os dois lados).
                    level.setBlock(pos.set(x, y - 1, z), Blocks.WATER.defaultBlockState(), 2);
                } else {
                    BlockState farmland = Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7);
                    level.setBlock(pos.set(x, y - 1, z), farmland, 2);
                    BlockState crop = rowCrop.defaultBlockState().setValue(CropBlock.AGE, 3 + random.nextInt(5));
                    level.setBlock(pos.set(x, y, z), crop, 2);
                }
            }
        }

        // Espantalho: poste de cerca com abobora esculpida ao lado do portao.
        BlockPos post = origin.offset(-HALF - 1, 0, HALF);
        level.setBlock(post, Blocks.OAK_FENCE.defaultBlockState(), 2);
        level.setBlock(post.above(), Blocks.CARVED_PUMPKIN.defaultBlockState(), 2);
        return true;
    }
}
