package com.hellaverse.entity;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

/** Aplica efeitos permanentes (e invisiveis) em mobs, reaplicando se forem removidos. */
public final class PermanentEffects {
    private PermanentEffects() {}

    /** Efeito de outro mod pelo id; null se nao existir (o mod pode nao ter registrado aquele efeito). */
    @Nullable
    public static MobEffect lookup(String namespace, String path) {
        return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(namespace, path));
    }

    public static void grant(LivingEntity entity, @Nullable MobEffect effect, int amplifier) {
        if (effect == null) {
            return;
        }
        MobEffectInstance current = entity.getEffect(effect);
        if (current == null || current.getAmplifier() < amplifier) {
            entity.addEffect(new MobEffectInstance(effect, MobEffectInstance.INFINITE_DURATION,
                    amplifier, false, false, true));
        }
    }
}
