package com.hellaverse.registry;

import com.hellaverse.HellaverseMod;
import com.hellaverse.Ring;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, HellaverseMod.MODID);

    /** Um bloco de "terra" por anel. E o bloco-base do terreno daquela dimensao. */
    public static final Map<Ring, RegistryObject<Block>> SOILS = new EnumMap<>(Ring.class);

    static {
        for (Ring ring : Ring.values()) {
            SOILS.put(ring, BLOCKS.register(ring.soilName(), () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(ring.mapColor)
                            .strength(0.6F)
                            .sound(SoundType.GRAVEL))));
        }
    }

    private ModBlocks() {}
}
