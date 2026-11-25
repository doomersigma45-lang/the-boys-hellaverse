package com.hellaverse.entity;

import com.hellaverse.entity.goal.WebShootGoal;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * Web Weaver (The Boys). Neutro e passivo: so revida quem o atacar. Escala paredes, abre portas
 * e prende o alvo com teias. As teias que ele cria somem depois de {@value #WEB_LIFETIME_TICKS} ticks.
 */
public class WebWeaverEntity extends PathfinderMob {
    public static final double MAX_HEALTH = 30.0D;
    public static final double ATTACK_DAMAGE = 6.5D;
    private static final int WEB_LIFETIME_TICKS = 200;

    /** Teias criadas por este mob e quando expiram (tempo de jogo). */
    private final Map<BlockPos, Long> webs = new ConcurrentHashMap<>();

    public WebWeaverEntity(EntityType<? extends WebWeaverEntity> type, Level level) {
        super(type, level);
        if (getNavigation() instanceof GroundPathNavigation ground) {
            ground.setCanOpenDoors(true);
        }
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        goalSelector.addGoal(2, new WebShootGoal(this));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    // ----------------------------------------------------------------- escalar paredes
    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    public boolean onClimbable() {
        return horizontalCollision;
    }

    // ----------------------------------------------------------------- teias
    public void trackWeb(BlockPos pos) {
        webs.put(pos.immutable(), level().getGameTime() + WEB_LIFETIME_TICKS);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide && !webs.isEmpty() && tickCount % 10 == 0) {
            long now = level().getGameTime();
            Iterator<Map.Entry<BlockPos, Long>> it = webs.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<BlockPos, Long> entry = it.next();
                if (now >= entry.getValue()) {
                    if (level().getBlockState(entry.getKey()).is(Blocks.COBWEB)) {
                        level().removeBlock(entry.getKey(), false);
                    }
                    it.remove();
                }
            }
        }
    }

    /** Ao morrer ou sumir, limpa as teias que ainda estiverem de pe. */
    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide) {
            for (BlockPos pos : webs.keySet()) {
                if (level().getBlockState(pos).is(Blocks.COBWEB)) {
                    level().removeBlock(pos, false);
                }
            }
            webs.clear();
        }
        super.remove(reason);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }
}
