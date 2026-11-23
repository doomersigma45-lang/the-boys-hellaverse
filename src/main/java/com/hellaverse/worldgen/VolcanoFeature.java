package com.hellaverse.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Vulcao em forma de cone com cratera de lava. O raio maximo (15) garante que a feature
 * nunca escreve alem dos chunks vizinhos (3x3) durante a geracao.
 */
public class VolcanoFeature extends Feature<NoneFeatureConfiguration> {
    private static final int CRATER_DEPTH = 3;

    public VolcanoFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // Nao constroi vulcoes dentro d'agua nem coladas na praia.
        if (origin.getY() < level.getSeaLevel() + 2) {
            return false;
        }

        int radius = 11 + random.nextInt(5);        // 11..15
        int height = 10 + random.nextInt(7);        // 10..16
        double craterRadius = 3.0 + random.nextInt(3); // 3..5
        int rimHeight = coneHeight(craterRadius, radius, height);
        int baseY = origin.getY();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > radius) {
                    continue;
                }
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int ground = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                if (ground < level.getSeaLevel()) {
                    continue; // coluna dentro d'agua
                }

                boolean crater = distance < craterRadius;
                int top;
                if (crater) {
                    top = baseY + rimHeight - CRATER_DEPTH;
                } else {
                    top = baseY + coneHeight(distance, radius, height) + random.nextInt(2);
                }

                // Corpo do vulcao: da superficie do terreno ate o topo calculado.
                int solidTop = crater ? top - 1 : top;
                for (int y = ground; y < solidTop; y++) {
                    level.setBlock(pos.set(x, y, z), slopeBlock(random), 2);
                }
                // Lava no fundo da cratera.
                if (crater && top - 1 >= ground) {
                    level.setBlock(pos.set(x, top - 1, z), Blocks.LAVA.defaultBlockState(), 2);
                }
                // Chamine: lava descendo abaixo do centro.
                if (distance < 1.5) {
                    for (int y = ground - 6; y < ground; y++) {
                        level.setBlock(pos.set(x, y, z), Blocks.LAVA.defaultBlockState(), 2);
                    }
                }
            }
        }
        return true;
    }

    private static int coneHeight(double distance, int radius, int height) {
        double t = 1.0 - distance / radius;
        return (int) Math.round(height * Math.pow(Math.max(t, 0.0), 1.4));
    }

    private static BlockState slopeBlock(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 10) {
            return Blocks.MAGMA_BLOCK.defaultBlockState();
        }
        if (roll < 55) {
            return Blocks.BLACKSTONE.defaultBlockState();
        }
        return Blocks.BASALT.defaultBlockState();
    }
}
