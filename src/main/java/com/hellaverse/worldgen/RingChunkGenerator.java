package com.hellaverse.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * Gerador de terreno igual ao do Overworld, mas que troca a pedra/terra/grama/areia
 * pelo bloco de "terra" do anel (preto, vermelho, roxo, amarelo, rosa, branco...).
 * A agua ganha a cor pelo bioma (water_color), entao nao precisa de fluido novo.
 */
public class RingChunkGenerator extends NoiseBasedChunkGenerator {
    public static final Codec<RingChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(RingChunkGenerator::generatorSettings),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("soil").forGetter(g -> g.soil)
    ).apply(instance, instance.stable(RingChunkGenerator::new)));

    /** Blocos naturais do Overworld que viram "terra do anel". */
    private static final Set<Block> REPLACED = Set.of(
            Blocks.STONE, Blocks.DEEPSLATE, Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.COARSE_DIRT,
            Blocks.PODZOL, Blocks.MYCELIUM, Blocks.ROOTED_DIRT, Blocks.MUD, Blocks.SAND, Blocks.RED_SAND,
            Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.GRAVEL, Blocks.CLAY, Blocks.TUFF,
            Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.TERRACOTTA, Blocks.PACKED_ICE,
            Blocks.SNOW_BLOCK, Blocks.ICE);

    private final Block soil;

    public RingChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings, Block soil) {
        super(biomeSource, settings);
        this.soil = soil;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager,
                             RandomState randomState, ChunkAccess chunk) {
        super.buildSurface(region, structureManager, randomState, chunk);

        var soilState = soil.defaultBlockState();
        for (int index = 0; index < chunk.getSectionsCount(); index++) {
            LevelChunkSection section = chunk.getSection(index);
            if (section.hasOnlyAir()) {
                continue;
            }
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        if (REPLACED.contains(section.getBlockState(x, y, z).getBlock())) {
                            section.setBlockState(x, y, z, soilState, false);
                        }
                    }
                }
            }
        }
    }
}
