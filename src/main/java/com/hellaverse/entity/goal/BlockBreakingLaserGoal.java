package com.hellaverse.entity.goal;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Laser que atravessa tudo: destroi todo bloco quebravel no caminho (nao precisa de linha de visao)
 * e machuca todo ser vivo que o feixe tocar. Respeita a gamerule mobGriefing para os blocos.
 */
public class BlockBreakingLaserGoal extends Goal {
    private static final DustParticleOptions CORE = new DustParticleOptions(new Vector3f(1.0F, 0.95F, 0.3F), 1.6F);
    private static final DustParticleOptions GLOW = new DustParticleOptions(new Vector3f(1.0F, 0.6F, 0.1F), 1.0F);

    private static final double MIN_RANGE = 4.0D;
    private static final double MAX_RANGE = 48.0D;
    private static final int FIRE_TICKS = 30;
    private static final int COOLDOWN_TICKS = 80;
    private static final int HIT_INTERVAL = 5;

    private final Mob mob;
    private final float damage;
    private long nextAllowedTime;
    private int firing;

    public BlockBreakingLaserGoal(Mob mob, float damagePerHit) {
        this.mob = mob;
        this.damage = damagePerHit;
        setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive()
                && mob.level().getGameTime() >= nextAllowedTime && inRange(target);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        return firing < FIRE_TICKS && target != null && target.isAlive() && inRange(target);
    }

    @Override
    public void start() {
        firing = 0;
        mob.playSound(SoundEvents.BEACON_ACTIVATE, 2.0F, 1.2F);
    }

    @Override
    public void stop() {
        nextAllowedTime = mob.level().getGameTime() + COOLDOWN_TICKS;
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

        Vec3 from = mob.getEyePosition();
        Vec3 dir = target.getEyePosition().subtract(from).normalize();
        double length = Math.min(MAX_RANGE, mob.distanceTo(target) + 2.0D);
        Vec3 step = dir.scale(0.5D);

        boolean griefing = level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        Set<LivingEntity> hit = new LinkedHashSet<>();
        boolean pulse = firing % HIT_INTERVAL == 0;
        Vec3 point = from.add(step);
        for (double travelled = 0.5D; travelled < length; travelled += 0.5D) {
            level.sendParticles(CORE, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            if (travelled % 1.5D == 0.0D) {
                level.sendParticles(GLOW, point.x, point.y, point.z, 1, 0.1D, 0.1D, 0.1D, 0.0D);
            }
            if (griefing) {
                breakBlockAt(level, BlockPos.containing(point));
            }
            if (pulse && travelled % 1.0D == 0.0D) {
                hit.addAll(level.getEntitiesOfClass(LivingEntity.class,
                        new AABB(point, point).inflate(1.0D), e -> e != mob && e.isAlive()));
            }
            point = point.add(step);
        }
        for (LivingEntity victim : hit) {
            victim.hurt(mob.damageSources().mobAttack(mob), damage);
            victim.setSecondsOnFire(4);
        }
    }

    private void breakBlockAt(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            return;
        }
        if (state.getDestroySpeed(level, pos) < 0.0F) {
            return; // bedrock e similares
        }
        level.destroyBlock(pos, false, mob);
    }

    private boolean inRange(LivingEntity target) {
        double distance = mob.distanceTo(target);
        return distance >= MIN_RANGE && distance <= MAX_RANGE;
    }
}
