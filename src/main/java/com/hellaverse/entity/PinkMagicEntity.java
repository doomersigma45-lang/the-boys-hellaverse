package com.hellaverse.entity;

import com.hellaverse.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
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
import org.joml.Vector3f;

/** Magia rosa da Octavia: bolha de energia invisivel (so particulas rosas) que fere por dano magico. */
public class PinkMagicEntity extends AbstractHurtingProjectile {
    public static final float DAMAGE = 8.0F;
    private static final DustParticleOptions PINK = new DustParticleOptions(new Vector3f(1.0F, 0.35F, 0.75F), 1.4F);
    private static final int MAX_AGE_TICKS = 80;

    public PinkMagicEntity(EntityType<? extends PinkMagicEntity> type, Level level) {
        super(type, level);
    }

    public PinkMagicEntity(Level level, LivingEntity shooter, Vec3 from, Vec3 direction) {
        super(ModEntities.PINK_MAGIC.get(), from.x, from.y, from.z, direction.x, direction.y, direction.z, level);
        setOwner(shooter);
        setDeltaMovement(direction.scale(0.9D));
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return PINK;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server) {
            server.sendParticles(PINK, getX(), getY(), getZ(), 4, 0.1D, 0.1D, 0.1D, 0.0D);
            server.sendParticles(ParticleTypes.HEART, getX(), getY(), getZ(), 1, 0.15D, 0.15D, 0.15D, 0.0D);
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
        DamageSource source = damageSources().indirectMagic(this, getOwner());
        target.hurt(source, DAMAGE);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(PINK, getX(), getY(), getZ(), 20, 0.3D, 0.3D, 0.3D, 0.05D);
            playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.2F);
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
