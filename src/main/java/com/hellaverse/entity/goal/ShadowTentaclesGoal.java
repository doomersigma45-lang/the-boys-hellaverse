package com.hellaverse.entity.goal;

import com.hellaverse.entity.ShadowDemonEntity;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Tentaculos de sombra: marcam o chao em volta do alvo (aviso verde), brotam pretos com brilho verde,
 * ferem e prendem (lentidao forte) quem estiver no ponto. Desenhados com particulas.
 */
public class ShadowTentaclesGoal extends Goal {
    private static final DustParticleOptions BLACK = new DustParticleOptions(new Vector3f(0.04F, 0.04F, 0.06F), 1.8F);
    private static final DustParticleOptions GREEN = new DustParticleOptions(new Vector3f(0.15F, 1.0F, 0.4F), 1.0F);

    private static final double MAX_RANGE = 26.0D;
    private static final int WARN_TICKS = 20;
    private static final int STRIKE_TICKS = 16;
    private static final int COOLDOWN_TICKS = 140;
    private static final int TENTACLES = 6;
    private static final double RADIUS = 1.4D;
    private static final double HEIGHT = 3.2D;

    private final Mob mob;
    private final float damage;
    private final List<Vec3> spots = new ArrayList<>();
    private long nextAllowedTime;
    private int ticks;

    public ShadowTentaclesGoal(Mob mob, float damage) {
        this.mob = mob;
        this.damage = damage;
        setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive() && mob.level().getGameTime() >= nextAllowedTime
                && mob.distanceTo(target) <= MAX_RANGE;
    }

    @Override
    public boolean canContinueToUse() {
        return ticks < WARN_TICKS + STRIKE_TICKS && mob.getTarget() != null;
    }

    @Override
    public void start() {
        ticks = 0;
        spots.clear();
        LivingEntity target = mob.getTarget();
        if (target == null) {
            return;
        }
        spots.add(target.position());
        for (int i = 1; i < TENTACLES; i++) {
            double angle = mob.getRandom().nextDouble() * Math.PI * 2.0D;
            double distance = 1.5D + mob.getRandom().nextDouble() * 2.5D;
            spots.add(target.position().add(Math.cos(angle) * distance, 0.0D, Math.sin(angle) * distance));
        }
        mob.playSound(SoundEvents.WARDEN_TENDRIL_CLICKS, 2.0F, 0.6F);
    }

    @Override
    public void stop() {
        nextAllowedTime = mob.level().getGameTime() + COOLDOWN_TICKS;
        spots.clear();
        ticks = 0;
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
            mob.getLookControl().setLookAt(target, 40.0F, 40.0F);
        }
        ticks++;
        if (ticks <= WARN_TICKS) {
            for (Vec3 spot : spots) {
                for (int i = 0; i < 6; i++) {
                    double a = i * Math.PI / 3.0D + ticks * 0.3D;
                    level.sendParticles(GREEN, spot.x + Math.cos(a) * RADIUS, spot.y + 0.1D,
                            spot.z + Math.sin(a) * RADIUS, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
            return;
        }
        int strike = ticks - WARN_TICKS;
        if (strike == 1) {
            mob.playSound(SoundEvents.WARDEN_TENDRIL_CLICKS, 2.0F, 0.4F);
            for (Vec3 spot : spots) {
                hitAround(level, spot);
            }
        }
        double rise = Math.min(1.0D, strike / 5.0D) * HEIGHT;
        for (Vec3 spot : spots) {
            for (double y = 0.0D; y < rise; y += 0.35D) {
                double sway = Math.sin(y * 2.2D + ticks * 0.5D) * 0.35D;
                double cx = spot.x + sway;
                double cz = spot.z + Math.cos(y * 2.2D + ticks * 0.5D) * 0.35D;
                level.sendParticles(BLACK, cx, spot.y + y, cz, 2, 0.08D, 0.05D, 0.08D, 0.0D);
                if (((int) (y / 0.35D) + ticks) % 3 == 0) {
                    level.sendParticles(GREEN, cx, spot.y + y, cz, 1, 0.1D, 0.05D, 0.1D, 0.0D);
                    level.sendParticles(ParticleTypes.SCULK_SOUL, cx, spot.y + y, cz, 1, 0.05D, 0.05D, 0.05D, 0.0D);
                }
            }
        }
    }

    private void hitAround(ServerLevel level, Vec3 spot) {
        AABB box = new AABB(spot.x - RADIUS, spot.y - 0.5D, spot.z - RADIUS,
                spot.x + RADIUS, spot.y + HEIGHT, spot.z + RADIUS);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != mob && e.isAlive() && !(e instanceof ShadowDemonEntity))) {
            victim.hurt(mob.damageSources().mobAttack(mob), damage);
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
        }
    }
}
