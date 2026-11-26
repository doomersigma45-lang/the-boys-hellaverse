package com.hellaverse.entity;

import com.hellaverse.entity.goal.LightningStrikeGoal;
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
import net.minecraft.tags.DamageTypeTags;

/**
 * Stormfront (boss voadora). Hostil a jogadores: chama raios, voa, abre portas e tem o efeito
 * compound_v:stormfront permanente. Imune a raios.
 */
public class StormfrontEntity extends AbstractBossEntity {
    public static final double MAX_HEALTH = 350.0D;
    public static final double ATTACK_DAMAGE = 25.0D;
    private static final float LIGHTNING_DAMAGE = 12.0F;

    public StormfrontEntity(EntityType<? extends StormfrontEntity> type, Level level) {
        super(type, level, BossEvent.BossBarColor.WHITE);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FLYING_SPEED, 0.06D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new OpenNearbyDoorsGoal(this));
        goalSelector.addGoal(2, new LightningStrikeGoal(this, LIGHTNING_DAMAGE, 3, 90, 40.0D));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        targetSelector.addGoal(0, new HurtByTargetGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (tickCount % 40 == 0) {
            PermanentEffects.grant(this, PermanentEffects.lookup("compound_v", "stormfront"), 0);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_LIGHTNING)) {
            return false;
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
