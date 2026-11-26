package com.hellaverse.entity.goal;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/** Teletransporta o mob para perto do alvo (quando longe, ou de vez em quando para surpreender). */
public class TeleportToTargetGoal extends Goal {
    private final Mob mob;
    private final int cooldown;
    private final double farDistance;
    private long nextAllowedTime;

    public TeleportToTargetGoal(Mob mob, int cooldownTicks, double farDistance) {
        this.mob = mob;
        this.cooldown = cooldownTicks;
        this.farDistance = farDistance;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive() || mob.level().getGameTime() < nextAllowedTime) {
            return false;
        }
        return mob.distanceTo(target) > farDistance || mob.getRandom().nextInt(12) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        nextAllowedTime = mob.level().getGameTime() + cooldown;
        LivingEntity target = mob.getTarget();
        if (target == null || !(mob.level() instanceof ServerLevel level)) {
            return;
        }
        double fromX = mob.getX();
        double fromY = mob.getY() + mob.getBbHeight() / 2.0D;
        double fromZ = mob.getZ();
        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = mob.getRandom().nextDouble() * Math.PI * 2.0D;
            double radius = 3.0D + mob.getRandom().nextDouble() * 2.5D;
            double x = target.getX() + Math.cos(angle) * radius;
            double z = target.getZ() + Math.sin(angle) * radius;
            if (mob.randomTeleport(x, target.getY(), z, true)) {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, fromX, fromY, fromZ, 30, 0.4D, 0.8D, 0.4D, 0.1D);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, mob.getX(), mob.getY() + 1.0D, mob.getZ(),
                        20, 0.5D, 1.0D, 0.5D, 0.2D);
                mob.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.5F, 1.2F);
                mob.getNavigation().stop();
                return;
            }
        }
    }
}
