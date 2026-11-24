package com.hellaverse.entity.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

/**
 * Quebra janelas (vidro e paineis) e portas fechadas que estejam entre o mob e o alvo.
 * Respeita a gamerule mobGriefing.
 */
public class BreakWindowGoal extends Goal {
    private static final double REACH_SQR = 3.0D * 3.0D;
    private static final int BREAK_TICKS = 20;

    private final Mob mob;
    @Nullable
    private BlockPos barrier;
    private int progress;

    public BreakWindowGoal(Mob mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (!mob.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return false;
        }
        barrier = findBarrier(target);
        return barrier != null;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive() && barrier != null
                && isBreakable(mob.level().getBlockState(barrier))
                && mob.distanceToSqr(Vec3.atCenterOf(barrier)) <= REACH_SQR;
    }

    @Override
    public void start() {
        progress = 0;
    }

    @Override
    public void tick() {
        if (barrier == null) {
            return;
        }
        mob.getLookControl().setLookAt(Vec3.atCenterOf(barrier));
        progress++;
        if (progress % 5 == 0) {
            mob.swing(InteractionHand.MAIN_HAND);
        }
        mob.level().destroyBlockProgress(mob.getId(), barrier, progress * 10 / BREAK_TICKS);
        if (progress >= BREAK_TICKS) {
            mob.level().destroyBlock(barrier, false, mob);
            mob.level().destroyBlockProgress(mob.getId(), barrier, -1);
            barrier = null;
        }
    }

    @Override
    public void stop() {
        if (barrier != null) {
            mob.level().destroyBlockProgress(mob.getId(), barrier, -1);
        }
        barrier = null;
        progress = 0;
    }

    /** Raio dos olhos do mob ate os olhos do alvo: se o primeiro bloco for vidro/porta, e a barreira. */
    @Nullable
    private BlockPos findBarrier(LivingEntity target) {
        Vec3 from = mob.getEyePosition();
        Vec3 to = target.getEyePosition();
        BlockHitResult hit = mob.level().clip(new ClipContext(from, to,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos pos = hit.getBlockPos();
        if (mob.distanceToSqr(Vec3.atCenterOf(pos)) > REACH_SQR) {
            return null;
        }
        return isBreakable(mob.level().getBlockState(pos)) ? pos : null;
    }

    private static boolean isBreakable(BlockState state) {
        return state.is(Tags.Blocks.GLASS) || state.is(Tags.Blocks.GLASS_PANES) || state.is(BlockTags.DOORS);
    }
}
