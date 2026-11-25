package com.hellaverse.entity;

import com.hellaverse.entity.goal.ChestBlastGoal;
import com.hellaverse.item.SoldierBoyShieldItem;
import com.hellaverse.registry.ModItems;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
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
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Soldier Boy (The Boys). Boss hostil: ataca jogadores. Imune a projeteis,
 * Resistencia II e -25% de dano (menos que o Homelander), explosao de peito (chest blast)
 * e escudo proprio que cai ao morrer.
 */
public class SoldierBoyEntity extends AbstractBossEntity {
    public static final double MAX_HEALTH = 450.0D;
    public static final double TOTAL_ATTACK_DAMAGE = 75.0D;
    /** Atributo base: o escudo soma +29 por cima, fechando 75 por golpe. */
    public static final double BASE_ATTACK_DAMAGE = TOTAL_ATTACK_DAMAGE - SoldierBoyShieldItem.ATTACK_BONUS;
    /** Reducao extra de dano (somada a Resistencia II). Homelander usa 0,6 (-40%). */
    private static final float DAMAGE_TAKEN_FACTOR = 0.75F;

    public SoldierBoyEntity(EntityType<? extends SoldierBoyEntity> type, Level level) {
        super(type, level, BossEvent.BossBarColor.BLUE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, BASE_ATTACK_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new ChestBlastGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ----------------------------------------------------------------- efeitos e dano
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (tickCount % 40 == 0) {
            PermanentEffects.grant(this, MobEffects.DAMAGE_RESISTANCE, 1); // Resistencia II
            PermanentEffects.grant(this, PermanentEffects.lookup("compound_v", "chest_blast"), 0);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_PROJECTILE) || source.getDirectEntity() instanceof Projectile) {
            return false; // imune a projeteis
        }
        if (source.getDirectEntity() == this || source.getEntity() == this) {
            return false; // a propria explosao de peito nao o machuca
        }
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            amount *= DAMAGE_TAKEN_FACTOR;
        }
        return super.hurt(source, amount);
    }

    // ----------------------------------------------------------------- escudo
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
        populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        return data;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.SOLDIER_BOY_SHIELD.get()));
        setDropChance(EquipmentSlot.MAINHAND, 2.0F); // acima de 1: sempre cai, sem dano aleatorio
    }

}
