package com.hellaverse.entity;

import com.hellaverse.entity.goal.LightningStrikeGoal;
import com.hellaverse.entity.goal.TeleportToTargetGoal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Vox (boss). Hostil a jogadores: teletransporta, chama raios, abre portas; imune a raios. */
public class VoxEntity extends AbstractBossEntity {
    public static final double MAX_HEALTH = 550.0D;
    public static final double ATTACK_DAMAGE = 30.0D;
    private static final float LIGHTNING_DAMAGE = 20.0F;

    private long nextEvadeTime;

    public VoxEntity(EntityType<? extends VoxEntity> type, Level level) {
        super(type, level, BossEvent.BossBarColor.RED);
        if (getNavigation() instanceof GroundPathNavigation ground) {
            ground.setCanOpenDoors(true);
        }
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
        goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        goalSelector.addGoal(2, new TeleportToTargetGoal(this, 100, 14.0D));
        goalSelector.addGoal(3, new LightningStrikeGoal(this, LIGHTNING_DAMAGE, 2, 120, 32.0D));
        goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.2D, true));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_LIGHTNING)) {
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        // as vezes se teletransporta para longe ao ser ferido
        if (hurt && level() instanceof ServerLevel server && getRandom().nextFloat() < 0.25F
                && server.getGameTime() >= nextEvadeTime) {
            nextEvadeTime = server.getGameTime() + 60;
            double x = getX() + (getRandom().nextDouble() - 0.5D) * 16.0D;
            double z = getZ() + (getRandom().nextDouble() - 0.5D) * 16.0D;
            server.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 1.0D, getZ(), 20, 0.4D, 0.8D, 0.4D, 0.1D);
            if (randomTeleport(x, getY(), z, true)) {
                playSound(SoundEvents.ENDERMAN_TELEPORT, 1.5F, 1.0F);
            }
        }
        return hurt;
    }
}
