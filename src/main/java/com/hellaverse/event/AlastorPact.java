package com.hellaverse.event;

import net.minecraft.world.entity.player.Player;

/** Estado do acordo com o Alastor, guardado nos dados persistentes do jogador (sobrevive a morte e relogin). */
public final class AlastorPact {
    private static final String KEY = "hellaverse_alastor_pact";

    private AlastorPact() {}

    public static boolean isBound(Player player) {
        return player.getPersistentData().getBoolean(KEY);
    }

    public static void bind(Player player) {
        player.getPersistentData().putBoolean(KEY, true);
    }

    public static void release(Player player) {
        player.getPersistentData().remove(KEY);
    }
}
