package com.hellaverse.client;

import com.hellaverse.HellaverseMod;
import com.hellaverse.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HellaverseMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BLACK_NOIR.get(), BlackNoirRenderer::new);
        event.registerEntityRenderer(ModEntities.HOMELANDER.get(),
                context -> new SimpleHumanoidRenderer<>(context, "homelander"));
        event.registerEntityRenderer(ModEntities.KIMIKO.get(),
                context -> new SimpleHumanoidRenderer<>(context, "kimiko"));
        event.registerEntityRenderer(ModEntities.WEB_WEAVER.get(),
                context -> new SimpleHumanoidRenderer<>(context, "web_weaver"));
    }

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(PrideSkyEffects.ID, new PrideSkyEffects());
    }
}
