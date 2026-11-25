package com.hellaverse.entity.goal;

import java.util.EnumSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.Vec3;

/** Joga TNT aceso em arco na direcao do alvo. O mob em si nao sofre dano de explosao (ver a entidade). */
public class ThrowTntGoal extends Goal {
    private static final double MIN_RANGE = 3.0D;
    private static final double MAX_RANGE = 18.0D;
    private static final int FUSE_TICKS = 50;
    private static final int COOLDOWN_TICKS = 80;

    private final Mob mob;
    private long nextAllowedTime;

    public ThrowTntGoal(Mob mob) {
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
        return false; // lance unico por ativacao
    }

    @Override
    public void start() {
        LivingEntity target = mob.getTarget();
        nextAllowedTime = mob.level().getGameTime() + COOLDOWN_TICKS;
        if (target == null || mob.level().isClientSide) {
            return;
        }
        mob.getLookControl().setLookAt(target, 60.0F, 60.0F);
        PrimedTnt tnt = new PrimedTnt(mob.level(), mob.getX(), mob.getEyeY(), mob.getZ(), mob);
        tnt.setFuse(FUSE_TICKS);
        Vec3 delta = target.position().subtract(mob.position());
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        Vec3 flat = horizontal < 0.01D ? Vec3.ZERO : new Vec3(delta.x / horizontal, 0.0D, delta.z / horizontal);
        double speed = Math.min(1.1D, 0.25D + horizontal * 0.06D);
        tnt.setDeltaMovement(flat.x * speed, 0.35D + horizontal * 0.01D, flat.z * speed);
        mob.level().addFreshEntity(tnt);
        mob.playSound(SoundEvents.TNT_PRIMED, 1.0F, 1.0F);
    }
}
