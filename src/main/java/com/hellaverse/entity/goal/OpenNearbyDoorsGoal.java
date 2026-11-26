package com.hellaverse.entity.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Abre portas de mao que estejam por perto. Serve para mobs voadores, onde OpenDoorGoal nao funciona. */
public class OpenNearbyDoorsGoal extends Goal {
    private static final int RADIUS = 2;

    private final Mob mob;

    public OpenNearbyDoorsGoal(Mob mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        if (mob.tickCount % 5 != 0 || mob.getTarget() == null) {
            return false;
        }
        BlockPos base = mob.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-RADIUS, -1, -RADIUS), base.offset(RADIUS, 2, RADIUS))) {
            if (isClosedDoor(mob.level().getBlockState(pos))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        BlockPos base = mob.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-RADIUS, -1, -RADIUS), base.offset(RADIUS, 2, RADIUS))) {
            BlockState state = mob.level().getBlockState(pos);
            if (isClosedDoor(state)) {
                ((DoorBlock) state.getBlock()).setOpen(mob, mob.level(), state, pos.immutable(), true);
            }
        }
    }

    private static boolean isClosedDoor(BlockState state) {
        return state.getBlock() instanceof DoorBlock door && door.type().canOpenByHand()
                && !state.getValue(DoorBlock.OPEN);
    }
}
