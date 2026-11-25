package com.hellaverse.item;

import com.hellaverse.entity.RocketEntity;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

/** Lanca-foguetes: dispara um foguete explosivo (1 foguete por tiro, um tiro por clique). */
public class RpgItem extends GunItem {
    public RpgItem(Properties properties, Supplier<? extends Item> ammo, Stats stats) {
        super(properties, ammo, stats);
    }

    @Override
    protected void shoot(ServerLevel level, Player player) {
        Vec3 dir = player.getLookAngle();
        RocketEntity rocket = new RocketEntity(level, player, dir);
        level.addFreshEntity(rocket);
    }
}
