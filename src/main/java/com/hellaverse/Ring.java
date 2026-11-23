package com.hellaverse;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.MapColor;

/**
 * Cada anel e uma dimensao. Para adicionar uma nova dimensao no futuro:
 * 1) adicione uma entrada aqui; 2) adicione-a em tools/generate_assets.py; 3) rode o script.
 */
public enum Ring {
    ORGULHO("orgulho", MapColor.COLOR_BLACK),
    IRA("ira", MapColor.COLOR_RED),
    GANANCIA("ganancia", MapColor.COLOR_BLACK),
    LUXURIA("luxuria", MapColor.COLOR_PURPLE),
    GULA("gula", MapColor.COLOR_YELLOW),
    INVEJA("inveja", MapColor.COLOR_PURPLE),
    PREGUICA("preguica", MapColor.COLOR_PINK),
    PARAISO("paraiso", MapColor.SNOW);

    public final String id;
    public final MapColor mapColor;

    Ring(String id, MapColor mapColor) {
        this.id = id;
        this.mapColor = mapColor;
    }

    public String soilName() {
        return id + "_soil";
    }

    public String keyName() {
        return id + "_key";
    }

    public ResourceKey<Level> dimension() {
        return ResourceKey.create(Registries.DIMENSION, new ResourceLocation(HellaverseMod.MODID, id));
    }
}
