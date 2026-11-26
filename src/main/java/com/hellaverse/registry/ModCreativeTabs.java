package com.hellaverse.registry;

import com.hellaverse.HellaverseMod;
import com.hellaverse.Ring;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HellaverseMod.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + HellaverseMod.MODID))
                    .icon(() -> new ItemStack(ModItems.KEYS.get(Ring.ORGULHO).get()))
                    .displayItems((params, output) -> {
                        for (Ring ring : Ring.values()) {
                            output.accept(ModItems.KEYS.get(ring).get());
                        }
                        for (Ring ring : Ring.values()) {
                            output.accept(ModItems.SOIL_ITEMS.get(ring).get());
                        }
                        output.accept(ModItems.BLACK_NOIR_SPAWN_EGG.get());
                        output.accept(ModItems.HOMELANDER_SPAWN_EGG.get());
                        output.accept(ModItems.KIMIKO_SPAWN_EGG.get());
                        output.accept(ModItems.WEB_WEAVER_SPAWN_EGG.get());
                        output.accept(ModItems.ADAM_SPAWN_EGG.get());
                        output.accept(ModItems.SOLDIER_BOY_SPAWN_EGG.get());
                        output.accept(ModItems.SOLDIER_BOY_SHIELD.get());
                        output.accept(ModItems.LAMPLIGHTER_SPAWN_EGG.get());
                        output.accept(ModItems.CHERRI_BOMB_SPAWN_EGG.get());
                        output.accept(ModItems.LIGHTER.get());
                        output.accept(ModItems.FRENCHIE_SPAWN_EGG.get());
                        output.accept(ModItems.MOXXIE_SPAWN_EGG.get());
                        output.accept(ModItems.ASMODEUS_SPAWN_EGG.get());
                        output.accept(ModItems.VOX_SPAWN_EGG.get());
                        output.accept(ModItems.STORMFRONT_SPAWN_EGG.get());
                        output.accept(ModItems.BEELZEBUB_SPAWN_EGG.get());
                        output.accept(ModItems.BILLY_BUTCHER_SPAWN_EGG.get());
                        output.accept(ModItems.BLITZO_SPAWN_EGG.get());
                        output.accept(ModItems.CROWBAR.get());
                        output.accept(ModItems.ALASTOR_SPAWN_EGG.get());
                        output.accept(ModItems.ALASTOR_STAFF.get());
                        for (var weapon : new net.minecraft.world.item.Item[] {
                                ModItems.REVOLVER.get(), ModItems.UZI.get(), ModItems.AK47.get(),
                                ModItems.SNIPER.get(), ModItems.RPG.get(),
                                ModItems.PISTOL_AMMO.get(), ModItems.SMG_AMMO.get(), ModItems.RIFLE_AMMO.get(),
                                ModItems.SNIPER_AMMO.get(), ModItems.ROCKET.get()}) {
                            output.accept(weapon);
                        }
                    })
                    .build());

    private ModCreativeTabs() {}
}
