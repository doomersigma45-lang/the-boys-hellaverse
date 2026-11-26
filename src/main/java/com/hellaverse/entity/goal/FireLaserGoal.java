package com.hellaverse.entity.goal;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Laser de fogo (mais fraco que o do Adam): feixe de particulas de chama que precisa de linha de visao,
 * queima quem tocar e acende fogo onde termina (se mobGriefing permitir). Nao destroi blocos.
 */
public class FireLaserGoal extends Goal {
    private static final double MIN_RANGE = 3.0D;
    private static final int FIRE_TICKS = 30;
    private static final int HIT_INTERVAL = 10;

    private final Mob mob;
    private final float damage;
    private final double maxRange;
    private final int cooldown;
    private long nextAllowedTime;
    private int firing;

    public FireLaserGoal(Mob mob, float damagePerHit) {
        this(mob, damagePerHit, 24.0D, 100);
    }

    public FireLaserGoal(Mob mob, float damagePerHit, double maxRange, int cooldownTicks) {
        this.mob = mob;
        this.damage = damagePerHit;
        this.maxRange = maxRange;
        this.cooldown = cooldownTicks;
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
        return firing < FIRE_TICKS && target != null && target.isAlive()
                && inRange(target) && mob.hasLineOfSight(target);
    }

    @Override
    public void start() {
        firing = 0;
        mob.playSound(SoundEvents.BLAZE_SHOOT, 2.0F, 0.8F);
    }

    @Override
    public void stop() {
        nextAllowedTime = mob.level().getGameTime() + cooldown;
        firing = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null || !(mob.level() instanceof ServerLevel level)) {
            return;
        }
        mob.getLookControl().setLookAt(target, 60.0F, 60.0F);
        firing++;
        if (firing % 8 == 0) {
            mob.playSound(SoundEvents.FIRE_AMBIENT, 1.5F, 1.0F);
        }

        Vec3 from = mob.getEyePosition().add(0.0D, -0.2D, 0.0D);
        Vec3 end = target.getEyePosition();
        Vec3 delta = end.subtract(from);
        double length = delta.length();
        Vec3 step = delta.normalize().scale(0.5D);
        Vec3 point = from;
        boolean pulse = firing % HIT_INTERVAL == 0;
        Set<LivingEntity> hit = new LinkedHashSet<>();
        for (double travelled = 0.0D; travelled < length; travelled += 0.5D) {
            level.sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z, 2, 0.05D, 0.05D, 0.05D, 0.0D);
            if (travelled % 2.0D == 0.0D) {
                level.sendParticles(ParticleTypes.LAVA, point.x, point.y, point.z, 1, 0.1D, 0.1D, 0.1D, 0.0D);
            }
            if (pulse && travelled % 1.0D == 0.0D) {
                hit.addAll(level.getEntitiesOfClass(LivingEntity.class,
                        new AABB(point, point).inflate(0.8D), e -> e != mob && e.isAlive()));
            }
            point = point.add(step);
        }
        for (LivingEntity victim : hit) {
            victim.hurt(mob.damageSources().inFire(), damage);
            victim.setSecondsOnFire(5);
        }
        if (pulse && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            BlockPos pos = BlockPos.containing(end);
            if (level.getBlockState(pos).isAir() && Blocks.FIRE.defaultBlockState().canSurvive(level, pos)) {
                level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
            }
        }
    }

    private boolean inRange(LivingEntity target) {
        double distance = mob.distanceTo(target);
        return distance >= MIN_RANGE && distance <= maxRange;
    }
}
