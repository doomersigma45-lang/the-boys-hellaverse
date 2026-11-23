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
                    })
                    .build());

    private ModCreativeTabs() {}
}
