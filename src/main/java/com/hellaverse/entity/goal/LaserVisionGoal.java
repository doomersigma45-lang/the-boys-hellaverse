package com.hellaverse.entity.goal;

import java.util.EnumSet;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Visao de laser: dispara um feixe vermelho dos olhos ate o alvo quando ha linha de visao.
 * O feixe e implementado aqui (particulas + dano), pois o laser do Compound V so funciona para jogadores.
 */
public class LaserVisionGoal extends Goal {
    private static final DustParticleOptions CORE = new DustParticleOptions(new Vector3f(1.0F, 0.1F, 0.1F), 1.4F);
    private static final DustParticleOptions GLOW = new DustParticleOptions(new Vector3f(1.0F, 0.55F, 0.2F), 0.9F);

    private static final double MIN_RANGE = 4.0D;
    private static final double MAX_RANGE = 48.0D;
    private static final int FIRE_TICKS = 30;
    private static final int COOLDOWN_TICKS = 60;
    private static final int HIT_INTERVAL = 5;

    private final Mob mob;
    private final float damage;
    private long nextAllowedTime;
    private int firing;

    public LaserVisionGoal(Mob mob, float damagePerHit) {
        this.mob = mob;
        this.damage = damagePerHit;
        setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive()
                && mob.level().getGameTime() >= nextAllowedTime
                && inRange(target) && mob.hasLineOfSight(target);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        return firing < FIRE_TICKS && target != null && target.isAlive()
                && inRange(target) && mob.hasLineOfSight(target);
    }

    @Override
    public void start() {
        firing = 0;
        mob.playSound(SoundEvents.BEACON_ACTIVATE, 1.5F, 1.8F);
    }

    @Override
    public void stop() {
        nextAllowedTime = mob.level().getGameTime() + COOLDOWN_TICKS;
        firing = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) {
            return;
        }
        mob.getLookControl().setLookAt(target, 60.0F, 60.0F);
        firing++;

        Vec3 from = mob.getEyePosition();
        Vec3 to = target.getEyePosition();
        drawBeam(from, to);

        if (firing % HIT_INTERVAL == 0) {
            target.hurt(mob.damageSources().mobAttack(mob), damage);
            target.setSecondsOnFire(3);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private boolean inRange(LivingEntity target) {
        double distance = mob.distanceTo(target);
        return distance >= MIN_RANGE && distance <= MAX_RANGE;
    }

    private void drawBeam(Vec3 from, Vec3 to) {
        if (!(mob.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 delta = to.subtract(from);
        double length = delta.length();
        Vec3 step = delta.normalize().scale(0.75D);
        Vec3 point = from.add(step); // comeca um pouco a frente dos olhos
        for (double travelled = 0.75D; travelled < length; travelled += 0.75D) {
            level.sendParticles(CORE, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            level.sendParticles(GLOW, point.x, point.y, point.z, 1, 0.08D, 0.08D, 0.08D, 0.0D);
            point = point.add(step);
        }
    }
}
