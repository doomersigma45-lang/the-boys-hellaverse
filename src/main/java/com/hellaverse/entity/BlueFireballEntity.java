package com.hellaverse.entity;

import com.hellaverse.registry.ModEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Projetil de fogo azul do Asmodeus: invisivel, desenhado so com particulas de chama azul. */
public class BlueFireballEntity extends AbstractHurtingProjectile {
    public static final float DAMAGE = 15.0F;
    private static final int MAX_AGE_TICKS = 100;

    public BlueFireballEntity(EntityType<? extends BlueFireballEntity> type, Level level) {
        super(type, level);
    }

    public BlueFireballEntity(Level level, LivingEntity shooter, Vec3 from, Vec3 direction) {
        super(ModEntities.BLUE_FIREBALL.get(), from.x, from.y, from.z,
                direction.x, direction.y, direction.z, level);
        setOwner(shooter);
        setDeltaMovement(direction.scale(1.0D));
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.SOUL_FIRE_FLAME;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY(), getZ(), 5, 0.12D, 0.12D, 0.12D, 0.01D);
            if (tickCount > MAX_AGE_TICKS) {
                discard();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide) {
            return;
        }
        Entity target = result.getEntity();
        Entity owner = getOwner();
        target.invulnerableTime = 0; // permite varios projeteis seguidos
        DamageSource source = damageSources().thrown(this, owner);
        if (target.hurt(source, DAMAGE)) {
            target.setSecondsOnFire(6);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY(), getZ(), 25, 0.3D, 0.3D, 0.3D, 0.06D);
            playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 1.0F, 0.8F);
            discard();
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }
}
