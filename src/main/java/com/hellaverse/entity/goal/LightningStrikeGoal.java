package com.hellaverse.entity.goal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Chama raios sobre o alvo: marca o chao com faiscas por ~0,75 s e entao cai um raio (so visual, sem fogo nem
 * efeitos colaterais no mundo) que fere quem estiver no ponto. O primeiro raio cai no alvo, os demais em volta.
 */
public class LightningStrikeGoal extends Goal {
    private static final int WARN_TICKS = 15;
    private static final double BLAST_RADIUS = 2.6D;

    private final Mob mob;
    private final float damage;
    private final int bolts;
    private final int cooldown;
    private final double maxRange;
    private final List<Vec3> spots = new ArrayList<>();
    private long nextAllowedTime;
    private int ticks;

    public LightningStrikeGoal(Mob mob, float damage, int bolts, int cooldownTicks, double maxRange) {
        this.mob = mob;
        this.damage = damage;
        this.bolts = bolts;
        this.cooldown = cooldownTicks;
        this.maxRange = maxRange;
        setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive() && mob.level().getGameTime() >= nextAllowedTime
                && mob.distanceTo(target) <= maxRange;
    }

    @Override
    public boolean canContinueToUse() {
        return ticks <= WARN_TICKS && mob.getTarget() != null;
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
        for (int i = 1; i < bolts; i++) {
            double angle = mob.getRandom().nextDouble() * Math.PI * 2.0D;
            double distance = 2.0D + mob.getRandom().nextDouble() * 3.0D;
            spots.add(target.position().add(Math.cos(angle) * distance, 0.0D, Math.sin(angle) * distance));
        }
        mob.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0F, 1.6F);
    }

    @Override
    public void stop() {
        nextAllowedTime = mob.level().getGameTime() + cooldown;
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
            mob.getLookControl().setLookAt(target, 60.0F, 60.0F);
        }
        ticks++;
        if (ticks < WARN_TICKS) {
            for (Vec3 spot : spots) {
                for (int i = 0; i < 6; i++) {
                    double a = i * Math.PI / 3.0D + ticks * 0.4D;
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, spot.x + Math.cos(a) * BLAST_RADIUS * 0.8D,
                            spot.y + 0.1D, spot.z + Math.sin(a) * BLAST_RADIUS * 0.8D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, spot.x, spot.y + 1.0D + ticks * 0.1D, spot.z,
                        2, 0.2D, 0.6D, 0.2D, 0.0D);
            }
            return;
        }
        for (Vec3 spot : spots) {
            strike(level, spot);
        }
    }

    private void strike(ServerLevel level, Vec3 spot) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(spot);
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
        AABB box = new AABB(spot.x - BLAST_RADIUS, spot.y - 1.0D, spot.z - BLAST_RADIUS,
                spot.x + BLAST_RADIUS, spot.y + 4.0D, spot.z + BLAST_RADIUS);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != mob && e.isAlive())) {
            victim.hurt(mob.damageSources().lightningBolt(), damage);
            victim.setSecondsOnFire(2);
        }
    }
}
