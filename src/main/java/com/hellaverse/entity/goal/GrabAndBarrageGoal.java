package com.hellaverse.entity.goal;

import com.hellaverse.entity.BlueFireballEntity;
import java.util.EnumSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * Segura o alvo no ar a sua frente e dispara de 4 a 7 projeteis de fogo azul a queima-roupa, depois solta.
 * O alvo e puxado por velocidade a cada tick (funciona em jogadores e mobs).
 */
public class GrabAndBarrageGoal extends Goal {
    private static final double GRAB_RANGE = 7.0D;
    private static final int LIFT_TICKS = 20;
    private static final int SHOT_INTERVAL = 8;
    private static final int RELEASE_DELAY = 10;
    private static final int COOLDOWN_TICKS = 300;

    private final Mob mob;
    private long nextAllowedTime;
    private int ticks;
    private int shots;

    public GrabAndBarrageGoal(Mob mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive() && mob.level().getGameTime() >= nextAllowedTime
                && mob.distanceTo(target) <= GRAB_RANGE;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive() && mob.distanceTo(target) <= GRAB_RANGE + 6.0D
                && ticks < LIFT_TICKS + shots * SHOT_INTERVAL + RELEASE_DELAY;
    }

    @Override
    public void start() {
        ticks = 0;
        shots = 4 + mob.getRandom().nextInt(4); // 4 a 7
        mob.getNavigation().stop();
        mob.playSound(SoundEvents.RAVAGER_ROAR, 2.0F, 0.6F);
    }

    @Override
    public void stop() {
        nextAllowedTime = mob.level().getGameTime() + COOLDOWN_TICKS;
        ticks = 0;
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
        mob.getNavigation().stop();
        mob.getLookControl().setLookAt(target, 60.0F, 60.0F);
        ticks++;

        Vec3 toTarget = target.position().subtract(mob.position());
        Vec3 flat = new Vec3(toTarget.x, 0.0D, toTarget.z);
        flat = flat.lengthSqr() < 1.0E-4D ? mob.getViewVector(1.0F) : flat.normalize();
        Vec3 hold = mob.position().add(flat.x * 2.0D, mob.getBbHeight() * 0.6D, flat.z * 2.0D);

        Vec3 pull = hold.subtract(target.position()).scale(0.4D);
        if (pull.length() > 1.5D) {
            pull = pull.normalize().scale(1.5D);
        }
        target.setDeltaMovement(pull);
        target.hurtMarked = true;
        target.fallDistance = 0.0F;

        int sinceLift = ticks - LIFT_TICKS;
        if (sinceLift >= 0 && sinceLift % SHOT_INTERVAL == 0 && sinceLift / SHOT_INTERVAL < shots
                && !mob.level().isClientSide) {
            Vec3 from = mob.getEyePosition();
            Vec3 dir = target.getBoundingBox().getCenter().subtract(from).normalize();
            mob.level().addFreshEntity(new BlueFireballEntity(mob.level(), mob, from.add(dir.scale(1.5D)), dir));
            mob.playSound(SoundEvents.GHAST_SHOOT, 2.0F, 0.7F);
        }
    }
}
