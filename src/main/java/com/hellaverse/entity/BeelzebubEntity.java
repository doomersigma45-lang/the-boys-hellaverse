package com.hellaverse.entity;

import com.hellaverse.entity.goal.OpenNearbyDoorsGoal;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;


import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Beelzebub (boss voadora). Hostil a jogadores, abre portas e e bem mais resistente que o Homelander
 * (-50% de dano recebido, mais Resistencia III). Ao chegar na metade da vida entra em furia: +50% de dano,
 * mais velocidade, Forca e barra de vida vermelha.
 */
public class BeelzebubEntity extends AbstractBossEntity {
    public static final double MAX_HEALTH = 2100.0D;
    public static final double ATTACK_DAMAGE = 90.0D;
    /** Homelander usa 0,6. */
    private static final float DAMAGE_TAKEN_FACTOR = 0.5F;
    private static final UUID ENRAGE_DAMAGE_ID = UUID.fromString("5d0a6e1c-3b57-4c0e-9a1d-7f2b8e4c1a01");
    private static final UUID ENRAGE_SPEED_ID = UUID.fromString("5d0a6e1c-3b57-4c0e-9a1d-7f2b8e4c1a02");

    public BeelzebubEntity(EntityType<? extends BeelzebubEntity> type, Level level) {
        super(type, level, BossEvent.BossBarColor.PINK);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FLYING_SPEED, 0.06D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new OpenNearbyDoorsGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        targetSelector.addGoal(0, new HurtByTargetGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ----------------------------------------------------------------- resistencia e furia
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (tickCount % 40 == 0) {
            PermanentEffects.grant(this, MobEffects.DAMAGE_RESISTANCE, 2); // Resistencia III
        }
        if (!isEnraged() && getHealth() <= getMaxHealth() / 2.0F) {
            enrage();
        }
    }

    public boolean isEnraged() {
        AttributeInstance damage = getAttribute(Attributes.ATTACK_DAMAGE);
        return damage != null && damage.getModifier(ENRAGE_DAMAGE_ID) != null;
    }

    private void enrage() {
        AttributeInstance damage = getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (damage != null) {
            damage.addTransientModifier(new AttributeModifier(ENRAGE_DAMAGE_ID, "Beelzebub enrage",
                    0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL)); // 90 -> 135
        }
        if (speed != null) {
            speed.addTransientModifier(new AttributeModifier(ENRAGE_SPEED_ID, "Beelzebub enrage",
                    0.3D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        PermanentEffects.grant(this, MobEffects.DAMAGE_BOOST, 1);
        setBossBarColor(BossEvent.BossBarColor.RED);
        playSound(SoundEvents.ENDER_DRAGON_GROWL, 3.0F, 0.7F);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1.0D, getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide && isEnraged()) {
            level().addParticle(ParticleTypes.FLAME, getRandomX(0.6D), getRandomY(), getRandomZ(0.6D), 0.0D, 0.04D, 0.0D);
            level().addParticle(ParticleTypes.ANGRY_VILLAGER, getRandomX(0.5D), getY() + getBbHeight(), getRandomZ(0.5D), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) { // /kill e vazio continuam funcionando
            amount *= DAMAGE_TAKEN_FACTOR;
        }
        return super.hurt(source, amount);
    }

    // ----------------------------------------------------------------- voo
    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(true);
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
}
