package com.hellaverse.entity;

import com.hellaverse.registry.ModEntities;
import java.util.EnumSet;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Demonio de sombra: pequeno servo (HP 5, dano 6) que protege o dono (jogador com o Alastor Staff ou o proprio
 * Alastor), ataca quem machuca o dono ou quem ele esta atacando e some depois de 30 s.
 */
public class ShadowDemonEntity extends PathfinderMob {
    public static final double MAX_HEALTH = 5.0D;
    public static final double ATTACK_DAMAGE = 6.0D;
    private static final int LIFETIME_TICKS = 600;

    @Nullable
    private UUID ownerId;
    private int life = LIFETIME_TICKS;

    public ShadowDemonEntity(EntityType<? extends ShadowDemonEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.36D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    /** Invoca {@code count} demonios em volta do dono. */
    public static int summon(ServerLevel level, LivingEntity owner, int count) {
        int spawned = 0;
        for (int i = 0; i < count; i++) {
            ShadowDemonEntity demon = ModEntities.SHADOW_DEMON.get().create(level);
            if (demon == null) {
                continue;
            }
            double angle = level.random.nextDouble() * Math.PI * 2.0D;
            double distance = 1.5D + level.random.nextDouble() * 1.5D;
            demon.moveTo(owner.getX() + Math.cos(angle) * distance, owner.getY(),
                    owner.getZ() + Math.sin(angle) * distance, level.random.nextFloat() * 360.0F, 0.0F);
            demon.ownerId = owner.getUUID();
            level.addFreshEntity(demon);
            level.sendParticles(ParticleTypes.SQUID_INK, demon.getX(), demon.getY() + 0.5D, demon.getZ(),
                    12, 0.3D, 0.4D, 0.3D, 0.02D);
            level.sendParticles(ParticleTypes.SCULK_SOUL, demon.getX(), demon.getY() + 0.5D, demon.getZ(),
                    4, 0.2D, 0.3D, 0.2D, 0.02D);
            spawned++;
        }
        return spawned;
    }

    @Nullable
    public LivingEntity getOwner() {
        if (ownerId != null && level() instanceof ServerLevel server
                && server.getEntity(ownerId) instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    public boolean isOwnedBy(@Nullable LivingEntity entity) {
        return entity != null && ownerId != null && ownerId.equals(entity.getUUID());
    }

    /** Dono, ou outro demonio do mesmo dono. */
    private boolean isFriendly(@Nullable LivingEntity other) {
        if (other == null) {
            return false;
        }
        if (isOwnedBy(other)) {
            return true;
        }
        return other instanceof ShadowDemonEntity demon && ownerId != null && ownerId.equals(demon.ownerId);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3D, true));
        goalSelector.addGoal(3, new FollowMasterGoal(this));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new DefendMasterGoal(this));
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (isFriendly(target)) {
            return;
        }
        super.setTarget(target);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isFriendly(source.getEntity() instanceof LivingEntity attacker ? attacker : null)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            if (random.nextInt(2) == 0) {
                level().addParticle(ParticleTypes.SMOKE, getRandomX(0.5D), getRandomY(), getRandomZ(0.5D), 0.0D, 0.02D, 0.0D);
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (--life <= 0 || (ownerId != null && getOwner() == null)
                || (getOwner() != null && !getOwner().isAlive())) {
            if (level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SQUID_INK, getX(), getY() + 0.5D, getZ(), 10, 0.3D, 0.4D, 0.3D, 0.02D);
            }
            discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
        tag.putInt("Life", life);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        life = tag.contains("Life") ? tag.getInt("Life") : LIFETIME_TICKS;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VEX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
    }

    // ------------------------------------------------------------------ goals
    /** Escolhe como alvo quem feriu o dono, quem o dono feriu, ou o alvo do dono (se for um mob). */
    private static class DefendMasterGoal extends TargetGoal {
        private final ShadowDemonEntity demon;

        DefendMasterGoal(ShadowDemonEntity demon) {
            super(demon, false);
            this.demon = demon;
            setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            LivingEntity owner = demon.getOwner();
            if (owner == null) {
                return false;
            }
            LivingEntity candidate = null;
            if (owner.getLastHurtByMob() != null && owner.tickCount - owner.getLastHurtByMobTimestamp() < 200) {
                candidate = owner.getLastHurtByMob();
            } else if (owner.getLastHurtMob() != null && owner.tickCount - owner.getLastHurtMobTimestamp() < 200) {
                candidate = owner.getLastHurtMob();
            } else if (owner instanceof Mob mob) {
                candidate = mob.getTarget();
            }
            if (candidate == null || !candidate.isAlive() || demon.isFriendly(candidate) || candidate == owner) {
                return false;
            }
            this.targetMob = candidate;
            return true;
        }
    }

    /** Fica perto do dono; se ficar muito longe, teleporta. */
    private static class FollowMasterGoal extends Goal {
        private final ShadowDemonEntity demon;

        FollowMasterGoal(ShadowDemonEntity demon) {
            this.demon = demon;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity owner = demon.getOwner();
            return owner != null && demon.distanceToSqr(owner) > 36.0D;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity owner = demon.getOwner();
            return owner != null && demon.distanceToSqr(owner) > 9.0D && !demon.getNavigation().isDone();
        }

        @Override
        public void tick() {
            LivingEntity owner = demon.getOwner();
            if (owner == null) {
                return;
            }
            if (demon.distanceToSqr(owner) > 576.0D) {
                demon.moveTo(owner.getX(), owner.getY(), owner.getZ());
                demon.getNavigation().stop();
            } else if (demon.tickCount % 10 == 0) {
                demon.getNavigation().moveTo(owner, 1.3D);
            }
        }
    }
}
