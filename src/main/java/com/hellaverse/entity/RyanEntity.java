package com.hellaverse.entity;

import com.hellaverse.entity.goal.BreakBlocksToTargetGoal;
import com.hellaverse.entity.goal.LaserVisionGoal;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Ryan (The Boys). Voa (creative_flight), dispara laser pelos olhos (laser_eyes_advanced), recebe 19% menos dano
 * e quebra blocos / abre portas no caminho do alvo, porem mais devagar que o Homelander.
 * Neutro: ataca mobs hostis e revida quem o atacar.
 */
public class RyanEntity extends PathfinderMob {
    public static final double MAX_HEALTH = 250.0D;
    public static final double ATTACK_DAMAGE = 35.0D;
    /** Todo dano recebido e multiplicado por isto (-19%). */
    private static final float DAMAGE_TAKEN_FACTOR = 0.81F;
    /** Dano por pulso do laser (um pulso a cada 5 ticks durante o disparo). */
    /** Quebra blocos mais devagar que o Homelander (10 ticks). */
    private static final int BREAK_TICKS = 18;
    private static final float LASER_DAMAGE = 10.0F;

    public RyanEntity(EntityType<? extends RyanEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FLYING_SPEED, 0.05D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new LaserVisionGoal(this, LASER_DAMAGE));
        goalSelector.addGoal(1, new BreakBlocksToTargetGoal(this, BREAK_TICKS));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        // Revida quem o atacar (inclusive jogadores).
        targetSelector.addGoal(0, new HurtByTargetGoal(this));
        // Identifica mobs hostis ate 64 blocos de distancia, sem precisar ve-los (mustSee = false).
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Mob.class, 20, false, false,
                target -> target instanceof Enemy));
    }

    // ----------------------------------------------------------------- voo
    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (isControlledByLocalInstance()) {
            if (isInWater()) {
                moveRelative(0.02F, travelVector);
                move(MoverType.SELF, getDeltaMovement());
                setDeltaMovement(getDeltaMovement().scale(0.8D));
            } else if (isInLava()) {
                moveRelative(0.02F, travelVector);
                move(MoverType.SELF, getDeltaMovement());
                setDeltaMovement(getDeltaMovement().scale(0.5D));
            } else {
                moveRelative(getSpeed(), travelVector);
                move(MoverType.SELF, getDeltaMovement());
                setDeltaMovement(getDeltaMovement().scale(0.91D));
            }
        }
        calculateEntityAnimation(false);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    // ----------------------------------------------------------------- efeitos e dano
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (tickCount % 40 == 0) {
            // Efeitos do Compound V (ignorados se o id nao existir).
            PermanentEffects.grant(this, PermanentEffects.lookup("compound_v", "laser_eyes_advanced"), 0);
            PermanentEffects.grant(this, PermanentEffects.lookup("compound_v", "creative_flight"), 0);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) { // /kill e vazio continuam funcionando
            amount *= DAMAGE_TAKEN_FACTOR;
        }
        return super.hurt(source, amount);
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
