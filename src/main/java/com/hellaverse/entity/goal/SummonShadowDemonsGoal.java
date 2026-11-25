package com.hellaverse.entity.goal;

import com.hellaverse.entity.ShadowDemonEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/** Invoca grupos de demonios de sombra em volta do mob enquanto ele tem um alvo. */
public class SummonShadowDemonsGoal extends Goal {
    private final Mob mob;
    private final int count;
    private final int cooldown;
    private final int maxAlive;
    private long nextAllowedTime;

    public SummonShadowDemonsGoal(Mob mob, int count, int cooldownTicks, int maxAlive) {
        this.mob = mob;
        this.count = count;
        this.cooldown = cooldownTicks;
        this.maxAlive = maxAlive;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive() || mob.level().getGameTime() < nextAllowedTime
                || !(mob.level() instanceof ServerLevel level)) {
            return false;
        }
        return level.getEntitiesOfClass(ShadowDemonEntity.class, mob.getBoundingBox().inflate(48.0D),
                d -> d.isOwnedBy(mob)).size() < maxAlive;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        nextAllowedTime = mob.level().getGameTime() + cooldown;
        if (mob.level() instanceof ServerLevel level) {
            ShadowDemonEntity.summon(level, mob, count);
            mob.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 2.0F, 0.7F);
        }
    }
}
