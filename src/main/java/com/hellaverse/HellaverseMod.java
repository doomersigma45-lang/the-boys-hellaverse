package com.hellaverse;

import com.hellaverse.registry.ModBlocks;
import com.hellaverse.registry.ModChunkGenerators;
import com.hellaverse.registry.ModCreativeTabs;
import com.hellaverse.registry.ModFeatures;
import com.hellaverse.registry.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HellaverseMod.MODID)
public class HellaverseMod {
    public static final String MODID = "hellaverse";

    public HellaverseMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModFeatures.FEATURES.register(bus);
        ModChunkGenerators.CHUNK_GENERATORS.register(bus);
        ModCreativeTabs.TABS.register(bus);
    }
}
