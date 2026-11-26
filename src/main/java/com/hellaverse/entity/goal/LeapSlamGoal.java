package com.hellaverse.entity.goal;

import java.util.EnumSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Salta em arco ate o alvo; ao pousar, pode explodir (respeita mobGriefing). */
public class LeapSlamGoal extends Goal {
    private static final double MIN_RANGE = 5.0D;
    private static final double MAX_RANGE = 30.0D;
    private static final int COOLDOWN_TICKS = 70;
    private static final int MAX_AIR_TICKS = 90;

    private final Mob mob;
    private final float explosionPower;
    private final float explosionChance;
    private long nextAllowedTime;
    private int airTicks;

    public LeapSlamGoal(Mob mob, float explosionPower, float explosionChance) {
        this.mob = mob;
        this.explosionPower = explosionPower;
        this.explosionChance = explosionChance;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive() || !mob.onGround()
                || mob.level().getGameTime() < nextAllowedTime) {
            return false;
        }
        double distance = mob.distanceTo(target);
        return distance >= MIN_RANGE && distance <= MAX_RANGE;
    }

    @Override
    public boolean canContinueToUse() {
        return airTicks < MAX_AIR_TICKS && !(airTicks > 5 && mob.onGround());
    }

    @Override
    public void start() {
        airTicks = 0;
        LivingEntity target = mob.getTarget();
        mob.getNavigation().stop();
        if (target == null) {
            return;
        }
        Vec3 delta = target.position().subtract(mob.position());
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        double speed = Math.min(1.6D, horizontal / 18.0D + 0.2D);
        Vec3 flat = horizontal < 0.01D ? Vec3.ZERO : new Vec3(delta.x / horizontal, 0.0D, delta.z / horizontal);
        double vertical = 0.95D + Math.max(0.0D, Math.min(0.4D, delta.y * 0.05D));
        mob.setDeltaMovement(flat.x * speed, vertical, flat.z * speed);
        mob.setOnGround(false);
        mob.hasImpulse = true;
        mob.playSound(SoundEvents.RAVAGER_ROAR, 1.5F, 1.0F);
    }

    @Override
    public void stop() {
        nextAllowedTime = mob.level().getGameTime() + COOLDOWN_TICKS;
        if (airTicks > 5 && mob.onGround() && mob.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, mob.getX(), mob.getY() + 0.2D, mob.getZ(),
                    20, 1.0D, 0.1D, 1.0D, 0.05D);
            if (mob.getRandom().nextFloat() < explosionChance) {
                level.explode(mob, mob.getX(), mob.getY(), mob.getZ(), explosionPower,
                        Level.ExplosionInteraction.MOB);
            }
        }
        airTicks = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        airTicks++;
        LivingEntity target = mob.getTarget();
        if (target != null) {
            mob.getLookControl().setLookAt(target, 60.0F, 60.0F);
            if (!mob.onGround()) { // pequena correcao de rumo no ar
                Vec3 toTarget = target.position().subtract(mob.position());
                Vec3 flat = new Vec3(toTarget.x, 0.0D, toTarget.z);
                if (flat.lengthSqr() > 1.0D) {
                    mob.setDeltaMovement(mob.getDeltaMovement().add(flat.normalize().scale(0.02D)));
                }
            }
        }
    }
}
