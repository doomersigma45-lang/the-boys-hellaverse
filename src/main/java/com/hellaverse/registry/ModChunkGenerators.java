package com.hellaverse.registry;

import com.hellaverse.HellaverseMod;
import com.hellaverse.worldgen.RingChunkGenerator;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.registries.DeferredRegister;

public final class ModChunkGenerators {
    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, HellaverseMod.MODID);

    static {
        CHUNK_GENERATORS.register("ring", () -> RingChunkGenerator.CODEC);
    }

    private ModChunkGenerators() {}
}
