package com.hellaverse.entity.goal;

import java.util.EnumSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.phys.Vec3;

/** Solta bolas de fogo (como o Ghast) contra o alvo, com carga curta antes de cada disparo. */
public class FireballGoal extends Goal {
    private static final double MIN_RANGE = 4.0D;
    private static final double MAX_RANGE = 28.0D;
    private static final int CHARGE_TICKS = 15;
    private static final int COOLDOWN_TICKS = 60;

    private final Mob mob;
    private final int explosionPower;
    private long nextAllowedTime;
    private int charge;

    public FireballGoal(Mob mob, int explosionPower) {
        this.mob = mob;
        this.explosionPower = explosionPower;
        setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive() && mob.level().getGameTime() >= nextAllowedTime
                && inRange(target) && mob.hasLineOfSight(target);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        return charge < CHARGE_TICKS && target != null && target.isAlive() && mob.hasLineOfSight(target);
    }

    @Override
    public void start() {
        charge = 0;
        mob.playSound(SoundEvents.BLAZE_AMBIENT, 1.5F, 0.8F);
    }

    @Override
    public void stop() {
        nextAllowedTime = mob.level().getGameTime() + COOLDOWN_TICKS;
        charge = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) {
            return;
        }
        mob.getLookControl().setLookAt(target, 60.0F, 60.0F);
        charge++;
        if (charge >= CHARGE_TICKS && !mob.level().isClientSide) {
            Vec3 look = mob.getViewVector(1.0F);
            double dx = target.getX() - (mob.getX() + look.x * 1.2D);
            double dy = target.getY(0.5D) - mob.getY(0.6D);
            double dz = target.getZ() - (mob.getZ() + look.z * 1.2D);
            LargeFireball fireball = new LargeFireball(mob.level(), mob, dx, dy, dz, explosionPower);
            fireball.setPos(mob.getX() + look.x * 1.2D, mob.getY(0.6D), mob.getZ() + look.z * 1.2D);
            mob.level().addFreshEntity(fireball);
            mob.playSound(SoundEvents.GHAST_SHOOT, 1.5F, 1.0F);
        }
    }

    private boolean inRange(LivingEntity target) {
        double distance = mob.distanceTo(target);
        return distance >= MIN_RANGE && distance <= MAX_RANGE;
    }
}
