package com.hellaverse.registry;

import com.hellaverse.HellaverseMod;
import com.hellaverse.Ring;
import com.hellaverse.item.GunItem;
import com.hellaverse.item.RingKeyItem;
import com.hellaverse.item.RpgItem;
import com.hellaverse.item.SoldierBoyShieldItem;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.sounds.SoundEvents;
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

    public static final RegistryObject<Item> WEB_WEAVER_SPAWN_EGG = ITEMS.register("web_weaver_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.WEB_WEAVER, 0x3A3A44, 0xE8E8E8, new Item.Properties()));

    public static final RegistryObject<Item> ADAM_SPAWN_EGG = ITEMS.register("adam_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ADAM, 0x1F4D2E, 0xC9A227, new Item.Properties()));
    public static final RegistryObject<Item> SOLDIER_BOY_SPAWN_EGG = ITEMS.register("soldier_boy_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.SOLDIER_BOY, 0xE8E8F0, 0xD4A93C, new Item.Properties()));

    /** Escudo do Soldier Boy: durabilidade acima da netherite, 30 de dano. */
    public static final RegistryObject<Item> SOLDIER_BOY_SHIELD = ITEMS.register("soldier_boy_shield",
            () -> new SoldierBoyShieldItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    // ----------------------------------------------------------------- municao
    public static final RegistryObject<Item> PISTOL_AMMO = ITEMS.register("pistol_ammo", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SMG_AMMO = ITEMS.register("smg_ammo", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RIFLE_AMMO = ITEMS.register("rifle_ammo", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SNIPER_AMMO = ITEMS.register("sniper_ammo", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ROCKET = ITEMS.register("rpg_rocket", () -> new Item(new Item.Properties()));

    // ----------------------------------------------------------------- armas
    // Stats(dano, intervalo em ticks, dispersao em graus, alcance, automatica, som, tom)
    public static final RegistryObject<Item> REVOLVER = ITEMS.register("revolver", () -> new GunItem(
            new Item.Properties().stacksTo(1), PISTOL_AMMO,
            new GunItem.Stats(12.0F, 14, 0.4F, 64.0D, false, SoundEvents.FIREWORK_ROCKET_BLAST, 0.7F)));
    public static final RegistryObject<Item> UZI = ITEMS.register("uzi", () -> new GunItem(
            new Item.Properties().stacksTo(1), SMG_AMMO,
            new GunItem.Stats(3.5F, 2, 3.0F, 40.0D, true, SoundEvents.FIREWORK_ROCKET_BLAST, 1.7F)));
    public static final RegistryObject<Item> AK47 = ITEMS.register("ak47", () -> new GunItem(
            new Item.Properties().stacksTo(1), RIFLE_AMMO,
            new GunItem.Stats(6.0F, 3, 1.5F, 64.0D, true, SoundEvents.FIREWORK_ROCKET_BLAST, 1.1F)));
    public static final RegistryObject<Item> SNIPER = ITEMS.register("sniper", () -> new GunItem(
            new Item.Properties().stacksTo(1), SNIPER_AMMO,
            new GunItem.Stats(40.0F, 40, 0.0F, 160.0D, false, SoundEvents.GENERIC_EXPLODE, 2.0F)));
    public static final RegistryObject<Item> RPG = ITEMS.register("rpg", () -> new RpgItem(
            new Item.Properties().stacksTo(1), ROCKET,
            new GunItem.Stats(0.0F, 60, 0.0F, 0.0D, false, SoundEvents.FIREWORK_ROCKET_LAUNCH, 0.6F)));

    private ModItems() {}
}
