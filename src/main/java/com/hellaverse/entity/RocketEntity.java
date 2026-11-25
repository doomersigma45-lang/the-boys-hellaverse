package com.hellaverse.entity;

import com.hellaverse.registry.ModEntities;
import com.hellaverse.registry.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Foguete do RPG: acelera em linha reta e explode ao bater em qualquer coisa. */
public class RocketEntity extends AbstractHurtingProjectile implements ItemSupplier {
    private static final float EXPLOSION_POWER = 3.5F;
    private static final int MAX_AGE_TICKS = 200;

    public RocketEntity(EntityType<? extends RocketEntity> type, Level level) {
        super(type, level);
    }

    public RocketEntity(Level level, LivingEntity shooter, Vec3 direction) {
        super(ModEntities.ROCKET.get(),
                shooter.getEyePosition().x + direction.x,
                shooter.getEyePosition().y + direction.y - 0.1D,
                shooter.getEyePosition().z + direction.z,
                direction.x, direction.y, direction.z, level);
        setOwner(shooter);
        setDeltaMovement(direction.scale(0.8D));
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            level().explode(this, getX(), getY(), getZ(), EXPLOSION_POWER, Level.ExplosionInteraction.TNT);
            discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount > MAX_AGE_TICKS) {
            discard();
        }
    }

    @Override
    public boolean isPickable() {
        return false; // nao pode ser rebatido como uma bola de fogo
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.ROCKET.get());
    }
}
