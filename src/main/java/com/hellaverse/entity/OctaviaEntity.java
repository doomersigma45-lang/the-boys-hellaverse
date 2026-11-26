package com.hellaverse.entity;

import com.hellaverse.entity.goal.PinkMagicGoal;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

/**
 * Octavia (Helluva Boss). Neutra: revida quem a atacar e ataca mobs hostis com magia rosa. Ao ser ferida cria um
 * escudo rosa que absorve dano por ate 5 s (ou ate acumular 30 de dano), com recarga de 8 s.
 */
public class OctaviaEntity extends PathfinderMob {
    public static final double MAX_HEALTH = 95.0D;
    public static final double ATTACK_DAMAGE = 7.5D;
    private static final int SHIELD_TICKS = 100;
    private static final float SHIELD_CAPACITY = 30.0F;
    private static final int SHIELD_COOLDOWN = 160;
    private static final DustParticleOptions SHIELD_PARTICLE =
            new DustParticleOptions(new Vector3f(1.0F, 0.4F, 0.8F), 1.0F);

    private int shieldTicks;
    private float shieldRemaining;
    private long shieldReadyTime;

    public OctaviaEntity(EntityType<? extends OctaviaEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new PinkMagicGoal(this));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                target -> target instanceof Enemy));
    }

    // ----------------------------------------------------------------- escudo rosa
    public boolean isShielded() {
        return shieldTicks > 0;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isShielded() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            shieldRemaining -= amount;
            if (level() instanceof ServerLevel server) {
                server.sendParticles(SHIELD_PARTICLE, getX(), getY() + getBbHeight() / 2.0D, getZ(),
                        15, 0.5D, 0.7D, 0.5D, 0.0D);
                playSound(SoundEvents.AMETHYST_BLOCK_HIT, 1.0F, 1.5F);
            }
            if (shieldRemaining <= 0.0F) {
                breakShield();
            }
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && level().getGameTime() >= shieldReadyTime && !isShielded()) {
            shieldTicks = SHIELD_TICKS;
            shieldRemaining = SHIELD_CAPACITY;
            playSound(SoundEvents.AMETHYST_CLUSTER_PLACE, 1.5F, 0.8F);
        }
        return hurt;
    }

    private void breakShield() {
        shieldTicks = 0;
        shieldRemaining = 0.0F;
        shieldReadyTime = level().getGameTime() + SHIELD_COOLDOWN;
        playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 1.5F, 1.0F);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (shieldTicks > 0) {
            if (--shieldTicks == 0) {
                breakShield();
            } else if (level() instanceof ServerLevel server && tickCount % 2 == 0) {
                // bolha rosa: anel de particulas em duas alturas
                for (int i = 0; i < 8; i++) {
                    double angle = i * Math.PI / 4.0D + tickCount * 0.15D;
                    for (double h : new double[] {0.3D, 0.9D, 1.5D}) {
                        server.sendParticles(SHIELD_PARTICLE, getX() + Math.cos(angle) * 0.9D, getY() + h,
                                getZ() + Math.sin(angle) * 0.9D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                    }
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ShieldTicks", shieldTicks);
        tag.putFloat("ShieldRemaining", shieldRemaining);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        shieldTicks = tag.getInt("ShieldTicks");
        shieldRemaining = tag.getFloat("ShieldRemaining");
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
