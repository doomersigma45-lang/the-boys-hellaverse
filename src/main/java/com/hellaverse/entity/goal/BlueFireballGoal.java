package com.hellaverse.entity.goal;

import com.hellaverse.entity.BlueFireballEntity;
import java.util.EnumSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/** Dispara um projetil de fogo azul contra o alvo, com uma carga curta antes. */
public class BlueFireballGoal extends Goal {
    private static final double MIN_RANGE = 5.0D;
    private static final double MAX_RANGE = 40.0D;
    private static final int CHARGE_TICKS = 12;
    private static final int COOLDOWN_TICKS = 35;

    private final Mob mob;
    private long nextAllowedTime;
    private int charge;

    public BlueFireballGoal(Mob mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.LOOK));
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
        LivingEntity target = mob.getTarget();
        return charge < CHARGE_TICKS && target != null && target.isAlive();
    }

    @Override
    public void start() {
        charge = 0;
        mob.playSound(SoundEvents.BLAZE_AMBIENT, 2.0F, 0.5F);
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
            Vec3 from = mob.getEyePosition();
            Vec3 dir = target.getBoundingBox().getCenter().subtract(from).normalize();
            mob.level().addFreshEntity(new BlueFireballEntity(mob.level(), mob, from.add(dir.scale(1.5D)), dir));
            mob.playSound(SoundEvents.GHAST_SHOOT, 2.0F, 0.6F);
        }
    }
}
