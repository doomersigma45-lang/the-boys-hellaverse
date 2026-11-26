package com.hellaverse.entity.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Abre portas e quebra blocos que estejam no caminho ate o alvo atual.
 * So age quando o mob tem alvo (ou seja, quando esta indo atras de alguem) e respeita mobGriefing.
 */
public class BreakBlocksToTargetGoal extends Goal {
    private static final double REACH_SQR = 3.5D * 3.5D;
    private static final int DEFAULT_BREAK_TICKS = 10;

    private final Mob mob;
    private final int breakTicks;
    @Nullable
    private BlockPos barrier;
    private int progress;

    public BreakBlocksToTargetGoal(Mob mob) {
        this(mob, DEFAULT_BREAK_TICKS);
    }

    /** @param breakTicks ticks para quebrar cada bloco (maior = mais lento). */
    public BreakBlocksToTargetGoal(Mob mob, int breakTicks) {
        this.mob = mob;
        this.breakTicks = breakTicks;
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
                && isBarrier(mob.level().getBlockState(barrier), barrier)
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
        BlockState state = mob.level().getBlockState(barrier);

        // Portas: so abre.
        if (state.getBlock() instanceof DoorBlock door) {
            mob.swing(InteractionHand.MAIN_HAND);
            door.setOpen(mob, mob.level(), state, barrier, true);
            barrier = null;
            return;
        }

        progress++;
        if (progress % 4 == 0) {
            mob.swing(InteractionHand.MAIN_HAND);
        }
        mob.level().destroyBlockProgress(mob.getId(), barrier, progress * 10 / breakTicks);
        if (progress >= breakTicks) {
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

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /** Primeiro bloco no raio olhos -> olhos do alvo, se estiver ao alcance. */
    @Nullable
    private BlockPos findBarrier(LivingEntity target) {
        BlockHitResult hit = mob.level().clip(new ClipContext(mob.getEyePosition(), target.getEyePosition(),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos pos = hit.getBlockPos();
        if (mob.distanceToSqr(Vec3.atCenterOf(pos)) > REACH_SQR) {
            return null;
        }
        return isBarrier(mob.level().getBlockState(pos), pos) ? pos : null;
    }

    private boolean isBarrier(BlockState state, BlockPos pos) {
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            return false;
        }
        if (state.getBlock() instanceof DoorBlock) {
            return !state.getValue(DoorBlock.OPEN); // porta ja aberta nao e barreira
        }
        // Blocos inquebraveis (bedrock etc.) tem destroySpeed negativo.
        return state.getDestroySpeed(mob.level(), pos) >= 0.0F;
    }
}
