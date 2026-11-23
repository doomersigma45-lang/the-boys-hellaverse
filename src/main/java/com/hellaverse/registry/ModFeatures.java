package com.hellaverse.registry;

import com.hellaverse.HellaverseMod;
import com.hellaverse.worldgen.FarmFeature;
import com.hellaverse.worldgen.VolcanoFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, HellaverseMod.MODID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> VOLCANO =
            FEATURES.register("volcano", () -> new VolcanoFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> FARM =
            FEATURES.register("farm", () -> new FarmFeature(NoneFeatureConfiguration.CODEC));

    private ModFeatures() {}
}
