package com.hellaverse.client;

import com.hellaverse.HellaverseMod;
import com.hellaverse.item.GunItem;
import net.minecraft.client.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HellaverseMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private ClientForgeEvents() {}

    /** Segurar o gatilho de uma arma automatica nao deve deixar o jogador lento (o vanilla corta para 20%). */
    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        Player player = event.getEntity();
        if (player.isUsingItem() && player.getUseItem().getItem() instanceof GunItem) {
            Input input = event.getInput();
            input.leftImpulse *= 5.0F;
            input.forwardImpulse *= 5.0F;
        }
    }
}
