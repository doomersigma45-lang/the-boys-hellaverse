package com.hellaverse.client;

import com.hellaverse.HellaverseMod;
import com.hellaverse.registry.ModEntities;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HellaverseMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private static final boolean ADAM_SLIM = true;
    private static final boolean SOLDIER_BOY_SLIM = false;

    private ClientModEvents() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BLACK_NOIR.get(), BlackNoirRenderer::new);
        event.registerEntityRenderer(ModEntities.HOMELANDER.get(),
                context -> new SimpleHumanoidRenderer<>(context, "homelander"));
        event.registerEntityRenderer(ModEntities.KIMIKO.get(),
                context -> new SimpleHumanoidRenderer<>(context, "kimiko"));
        event.registerEntityRenderer(ModEntities.ADAM.get(),
                context -> new SimpleHumanoidRenderer<>(context, "adam", ADAM_SLIM));
        event.registerEntityRenderer(ModEntities.SOLDIER_BOY.get(),
                context -> new SimpleHumanoidRenderer<>(context, "soldier_boy", SOLDIER_BOY_SLIM));
        event.registerEntityRenderer(ModEntities.LAMPLIGHTER.get(),
                context -> new SimpleHumanoidRenderer<>(context, "lamplighter", false));
        event.registerEntityRenderer(ModEntities.CHERRI_BOMB.get(),
                context -> new SimpleHumanoidRenderer<>(context, "cherri_bomb", true));
        event.registerEntityRenderer(ModEntities.ALASTOR.get(),
                context -> new SimpleHumanoidRenderer<>(context, "alastor", true));
        event.registerEntityRenderer(ModEntities.SHADOW_DEMON.get(),
                context -> new SimpleHumanoidRenderer<>(context, "shadow_demon").withScale(0.65F));
        event.registerEntityRenderer(ModEntities.FRENCHIE.get(),
                context -> new SimpleHumanoidRenderer<>(context, "frenchie"));
        event.registerEntityRenderer(ModEntities.MOXXIE.get(),
                context -> new SimpleHumanoidRenderer<>(context, "moxxie").withScale(0.78F));
        event.registerEntityRenderer(ModEntities.ASMODEUS.get(), AsmodeusRenderer::new);
        event.registerEntityRenderer(ModEntities.BLUE_FIREBALL.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.VOX.get(),
                context -> new SimpleHumanoidRenderer<>(context, "vox", true));
        event.registerEntityRenderer(ModEntities.STORMFRONT.get(),
                context -> new SimpleHumanoidRenderer<>(context, "stormfront"));
        event.registerEntityRenderer(ModEntities.BEELZEBUB.get(),
                context -> new SimpleHumanoidRenderer<>(context, "beelzebub"));
        event.registerEntityRenderer(ModEntities.BILLY_BUTCHER.get(),
                context -> new SimpleHumanoidRenderer<>(context, "billy_butcher"));
        event.registerEntityRenderer(ModEntities.BLITZO.get(),
                context -> new SimpleHumanoidRenderer<>(context, "blitzo").withScale(0.86F));
        event.registerEntityRenderer(ModEntities.SATAN.get(),
                context -> new SimpleHumanoidRenderer<>(context, "satan").withScale(5.0F));
        event.registerEntityRenderer(ModEntities.BOMBSIGHT.get(),
                context -> new SimpleHumanoidRenderer<>(context, "bombsight"));
        event.registerEntityRenderer(ModEntities.ROCKET.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.WEB_WEAVER.get(),
                context -> new SimpleHumanoidRenderer<>(context, "web_weaver"));
    }

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(PrideSkyEffects.ID, new PrideSkyEffects());
    }
}
