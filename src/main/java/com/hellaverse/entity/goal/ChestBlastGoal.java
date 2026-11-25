package com.hellaverse.entity.goal;

import java.util.EnumSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

/**
 * Explosao de energia do peito (poder do Soldier Boy): para, carrega por 2 s e detona.
 * Implementada aqui porque o efeito chest_blast do Compound V so funciona em jogadores.
 */
public class ChestBlastGoal extends Goal {
    private static final double TRIGGER_RANGE = 9.0D;
    private static final int CHARGE_TICKS = 40;
    private static final int COOLDOWN_TICKS = 300;
    private static final float POWER = 5.0F;

    private final Mob mob;
    private long nextAllowedTime;
    private int charge;

    public ChestBlastGoal(Mob mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive() && mob.level().getGameTime() >= nextAllowedTime
                && mob.distanceTo(target) <= TRIGGER_RANGE && mob.hasLineOfSight(target);
    }

    @Override
    public boolean canContinueToUse() {
        return charge < CHARGE_TICKS && mob.getTarget() != null && mob.getTarget().isAlive();
    }

    @Override
    public void start() {
        charge = 0;
        mob.getNavigation().stop();
        mob.playSound(SoundEvents.BEACON_POWER_SELECT, 2.0F, 0.6F);
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
        if (!(mob.level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity target = mob.getTarget();
        if (target != null) {
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
        charge++;
        double x = mob.getX();
        double y = mob.getY() + mob.getBbHeight() * 0.7D;
        double z = mob.getZ();
        level.sendParticles(ParticleTypes.END_ROD, x, y, z, 4 + charge / 4,
                0.6D, 0.4D, 0.6D, 0.02D + charge * 0.002D);
        if (charge % 10 == 0) {
            mob.playSound(SoundEvents.BEACON_AMBIENT, 1.5F, 0.6F + charge * 0.02F);
        }
        if (charge >= CHARGE_TICKS) {
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            level.explode(mob, x, y, z, POWER, Level.ExplosionInteraction.MOB);
        }
    }
}
