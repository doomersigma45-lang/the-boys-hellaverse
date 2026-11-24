package com.hellaverse.registry;

import com.hellaverse.HellaverseMod;
import com.hellaverse.Ring;
import com.hellaverse.item.RingKeyItem;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, HellaverseMod.MODID);

    public static final Map<Ring, RegistryObject<Item>> SOIL_ITEMS = new EnumMap<>(Ring.class);
    public static final Map<Ring, RegistryObject<Item>> KEYS = new EnumMap<>(Ring.class);

    static {
        for (Ring ring : Ring.values()) {
            SOIL_ITEMS.put(ring, ITEMS.register(ring.soilName(),
                    () -> new BlockItem(ModBlocks.SOILS.get(ring).get(), new Item.Properties())));
            KEYS.put(ring, ITEMS.register(ring.keyName(),
                    () -> new RingKeyItem(ring, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC))));
        }
    }

    public static final RegistryObject<Item> BLACK_NOIR_SPAWN_EGG = ITEMS.register("black_noir_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.BLACK_NOIR, 0x0B0B0F, 0x8E0F1C, new Item.Properties()));

    public static final RegistryObject<Item> HOMELANDER_SPAWN_EGG = ITEMS.register("homelander_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.HOMELANDER, 0x1C3F94, 0xC8102E, new Item.Properties()));
    public static final RegistryObject<Item> KIMIKO_SPAWN_EGG = ITEMS.register("kimiko_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.KIMIKO, 0xE6A5C0, 0x6B1F3A, new Item.Properties()));

    private ModItems() {}
}
