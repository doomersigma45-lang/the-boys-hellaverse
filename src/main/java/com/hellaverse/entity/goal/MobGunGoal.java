package com.hellaverse.entity.goal;

import com.hellaverse.entity.RocketEntity;
import com.hellaverse.item.GunItem;
import com.hellaverse.item.RpgItem;
import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Faz um mob atirar com as armas do mod. A arma e escolhida a cada ativacao por {@link WeaponChooser}
 * e colocada na mao principal; a cadencia e o dano vem dos stats da propria arma (mobs atiram na metade da cadencia).
 */
public class MobGunGoal extends Goal {
    @FunctionalInterface
    public interface WeaponChooser {
        Item choose(Mob shooter, LivingEntity target, double distance);
    }

    private static final float MOB_EXTRA_SPREAD = 1.5F;

    private final Mob mob;
    private final WeaponChooser chooser;
    private final double minRange;
    private final double maxRange;
    private Item weapon;
    private int cooldown;

    public MobGunGoal(Mob mob, double minRange, double maxRange, WeaponChooser chooser) {
        this.mob = mob;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.chooser = chooser;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        double distance = mob.distanceTo(target);
        return distance >= minRange && distance <= maxRange && mob.hasLineOfSight(target);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive() && mob.distanceTo(target) <= maxRange + 4.0D
                && mob.hasLineOfSight(target);
    }

    @Override
    public void start() {
        cooldown = 10;
        pickWeapon();
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void pickWeapon() {
        LivingEntity target = mob.getTarget();
        if (target == null) {
            return;
        }
        Item chosen = chooser.choose(mob, target, mob.distanceTo(target));
        if (chosen != weapon || !mob.getMainHandItem().is(chosen)) {
            weapon = chosen;
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(chosen));
            mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        }
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null || !(mob.level() instanceof ServerLevel level) || !(weapon instanceof GunItem gun)) {
            return;
        }
        mob.getLookControl().setLookAt(target, 60.0F, 60.0F);
        double distance = mob.distanceTo(target);
        if (distance < minRange + 2.0D) {
            mob.getNavigation().stop();
        } else if (distance > maxRange * 0.7D) {
            mob.getNavigation().moveTo(target, 1.0D);
        } else {
            mob.getNavigation().stop();
        }

        if (--cooldown > 0) {
            return;
        }
        pickWeapon();
        if (!(weapon instanceof GunItem current)) {
            return;
        }
        GunItem.Stats stats = current.stats();
        level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), stats.sound(), SoundSource.HOSTILE, 1.2F, stats.pitch());
        if (current instanceof RpgItem) {
            Vec3 dir = target.getEyePosition().subtract(mob.getEyePosition()).normalize();
            level.addFreshEntity(new RocketEntity(level, mob, dir));
            level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH,
                    SoundSource.HOSTILE, 1.0F, 0.6F);
            cooldown = 80;
        } else {
            // mira exata no alvo (mobs nao tem pitch controlado pelo olhar com a mesma precisao do jogador)
            Vec3 aim = target.getEyePosition().subtract(mob.getEyePosition());
            double horizontal = Math.sqrt(aim.x * aim.x + aim.z * aim.z);
            mob.setYRot((float) (Math.toDegrees(Math.atan2(aim.z, aim.x)) - 90.0D));
            mob.setXRot((float) -Math.toDegrees(Math.atan2(aim.y, horizontal)));
            GunItem.shootFrom(level, mob, stats, MOB_EXTRA_SPREAD);
            cooldown = Math.max(stats.interval() * 2, 8);
        }
    }
}
