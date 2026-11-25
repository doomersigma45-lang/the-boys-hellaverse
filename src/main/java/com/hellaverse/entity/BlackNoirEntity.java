package com.hellaverse.entity;

import com.hellaverse.entity.goal.BreakWindowGoal;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Black Noir (The Boys).
 * Neutro: so ataca quem o atacou e mobs hostis. Imune a projeteis, explosoes quase nao o machucam,
 * escala paredes, e quebra portas e janelas para alcancar o alvo. Os mobs demoram a perceber
 * a presenca dele (ver {@link com.hellaverse.event.NoirStealthEvents}).
 */
public class BlackNoirEntity extends PathfinderMob {
    public static final double MAX_HEALTH = 130.0D;
    public static final int SHARPNESS_LEVEL = 7;
    /** Dano total desejado por golpe, ja contando a espada. */
    public static final double TOTAL_ATTACK_DAMAGE = 35.0D;
    /** Modificador de ataque da espada de netherite (vanilla). */
    private static final double NETHERITE_SWORD_DAMAGE = 7.0D;
    /** Bonus de Afiacao (vanilla): 1 + 0,5 por nivel acima do primeiro = 4,0 no nivel 7. */
    private static final double SHARPNESS_BONUS = 1.0D + (SHARPNESS_LEVEL - 1) * 0.5D;
    /** Atributo base: a espada e a Afiacao somam por cima, fechando 35 de dano por golpe. */
    public static final double BASE_ATTACK_DAMAGE = TOTAL_ATTACK_DAMAGE - NETHERITE_SWORD_DAMAGE - SHARPNESS_BONUS;
    /** Dano de explosao e multiplicado por este valor (-90%). */
    private static final float EXPLOSION_DAMAGE_FACTOR = 0.1F;
    private static final int DOOR_BREAK_TICKS = 40;

    public BlackNoirEntity(EntityType<? extends BlackNoirEntity> type, Level level) {
        super(type, level);
        if (getNavigation() instanceof GroundPathNavigation ground) {
            ground.setCanOpenDoors(true); // faz o pathfinding considerar portas como passagem
        }
        setPersistenceRequired();
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
        goalSelector.addGoal(1, new BreakDoorGoal(this, DOOR_BREAK_TICKS, difficulty -> true));
        goalSelector.addGoal(1, new BreakWindowGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        // Neutro: revida quem o atacar e caca mobs hostis.
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                target -> target instanceof Enemy));
    }

    // ----------------------------------------------------------------- escalar paredes
    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    public boolean onClimbable() {
        return horizontalCollision;
    }

    // ----------------------------------------------------------------- efeitos permanentes
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (tickCount % 40 == 0) {
            PermanentEffects.grant(this, MobEffects.DAMAGE_RESISTANCE, 2); // Resistencia III
            PermanentEffects.grant(this, PermanentEffects.lookup("compound_v", "generic"), 0);
        }
    }

    // ----------------------------------------------------------------- dano
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_PROJECTILE) || source.getDirectEntity() instanceof Projectile) {
            // Imune a projeteis, mas quem atirou vira alvo.
            retaliateAgainst(source.getEntity());
            return false;
        }
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            amount *= EXPLOSION_DAMAGE_FACTOR;
        }
        return super.hurt(source, amount);
    }

    private void retaliateAgainst(@Nullable Entity attacker) {
        if (attacker instanceof LivingEntity living && living != this && canAttack(living)) {
            if (living instanceof Player player && (player.isCreative() || player.isSpectator())) {
                return;
            }
            setTarget(living);
        }
    }

    // ----------------------------------------------------------------- equipamento
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
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        sword.enchant(Enchantments.SHARPNESS, SHARPNESS_LEVEL);
        setItemSlot(EquipmentSlot.MAINHAND, sword);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    // ----------------------------------------------------------------- sons (voz de jogador)
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }
}
