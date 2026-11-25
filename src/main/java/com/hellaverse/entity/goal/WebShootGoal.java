package com.hellaverse.entity.goal;

import com.hellaverse.entity.WebWeaverEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Atira uma teia no alvo: rastro de particulas, lentidao e uma teia (bloco) onde o alvo esta.
 * As teias criadas somem sozinhas depois de alguns segundos (ver {@link WebWeaverEntity}).
 */
public class WebShootGoal extends Goal {
    private static final DustParticleOptions STRAND = new DustParticleOptions(new Vector3f(0.95F, 0.95F, 0.95F), 1.0F);
    private static final double MIN_RANGE = 3.0D;
    private static final double MAX_RANGE = 14.0D;
    private static final int COOLDOWN_TICKS = 100;

    private final WebWeaverEntity mob;
    private long nextAllowedTime;

    public WebShootGoal(WebWeaverEntity mob) {
        this.mob = mob;
        setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive() || mob.level().getGameTime() < nextAllowedTime) {
            return false;
        }
        double distance = mob.distanceTo(target);
        return distance >= MIN_RANGE && distance <= MAX_RANGE && mob.hasLineOfSight(target);
    }

    @Override
    public boolean canContinueToUse() {
        return false; // disparo instantaneo
    }

    @Override
    public void start() {
        LivingEntity target = mob.getTarget();
        if (target == null || !(mob.level() instanceof ServerLevel level)) {
            return;
        }
        nextAllowedTime = level.getGameTime() + COOLDOWN_TICKS;
        mob.playSound(SoundEvents.SPIDER_AMBIENT, 1.0F, 1.6F);

        Vec3 from = mob.getEyePosition();
        Vec3 to = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        Vec3 delta = to.subtract(from);
        Vec3 step = delta.normalize().scale(0.5D);
        Vec3 point = from;
        for (double travelled = 0.0D; travelled < delta.length(); travelled += 0.5D) {
            level.sendParticles(STRAND, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            point = point.add(step);
        }

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
        BlockPos pos = target.blockPosition();
        if (level.getBlockState(pos).isAir()) {
            level.setBlockAndUpdate(pos, Blocks.COBWEB.defaultBlockState());
            mob.trackWeb(pos);
        }
    }
}
