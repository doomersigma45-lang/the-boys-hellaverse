package com.hellaverse.entity;

import com.hellaverse.entity.goal.LeapSlamGoal;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Bombsight (boss). Hostil a jogadores: da grandes saltos ate o alvo e, ao pousar, pode explodir.
 * Tem Resistencia III permanente, e nenhum outro efeito.
 */
public class BombsightEntity extends AbstractBossEntity {
    public static final double MAX_HEALTH = 250.0D;
    public static final double ATTACK_DAMAGE = 45.0D;
    /** Resistencia III = amplificador 2 (e o unico efeito dele). Ajuste aqui para mais ou menos resistencia. */
    private static final int RESISTANCE_AMPLIFIER = 2;
    private static final float SLAM_EXPLOSION_POWER = 3.0F;
    private static final float SLAM_EXPLOSION_CHANCE = 0.6F;

    public BombsightEntity(EntityType<? extends BombsightEntity> type, Level level) {
        super(type, level, BossEvent.BossBarColor.YELLOW);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new LeapSlamGoal(this, SLAM_EXPLOSION_POWER, SLAM_EXPLOSION_CHANCE));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, true));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (tickCount % 40 == 0) {
            PermanentEffects.grant(this, MobEffects.DAMAGE_RESISTANCE, RESISTANCE_AMPLIFIER);
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false; // os saltos nao o machucam
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() == this || source.getDirectEntity() == this) {
            return false; // a propria explosao nao o machuca
        }
        return super.hurt(source, amount);
    }
}
