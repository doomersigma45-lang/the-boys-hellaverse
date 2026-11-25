package com.hellaverse.entity;

import com.hellaverse.entity.goal.BlueFireballGoal;
import com.hellaverse.entity.goal.GrabAndBarrageGoal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Asmodeus (boss gigante de 5 blocos). Hostil a jogadores: solta projeteis de fogo azul, segura o alvo no ar
 * e despeja ate 7 projeteis nele, abre portas e se abaixa para passar por vaos baixos. Imune a fogo.
 */
public class AsmodeusEntity extends AbstractBossEntity {
    public static final double MAX_HEALTH = 2500.0D;
    public static final double ATTACK_DAMAGE = 75.0D;
    public static final float STANDING_HEIGHT = 5.0F;
    private static final EntityDimensions CROUCHING_DIMENSIONS = EntityDimensions.scalable(0.9F, 1.9F);
    private static final int MIN_CROUCH_TICKS = 40;

    private int crouchSince;

    public AsmodeusEntity(EntityType<? extends AsmodeusEntity> type, Level level) {
        super(type, level, BossEvent.BossBarColor.BLUE);
        if (getNavigation() instanceof GroundPathNavigation ground) {
            ground.setCanOpenDoors(true);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        goalSelector.addGoal(2, new GrabAndBarrageGoal(this));
        goalSelector.addGoal(3, new BlueFireballGoal(this));
        goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.1D, true));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ----------------------------------------------------------------- agachar
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return pose == Pose.CROUCHING ? CROUCHING_DIMENSIONS : super.getDimensions(pose);
    }

    private boolean standingFitsAt(Vec3 pos) {
        double half = getType().getDimensions().width / 2.0D;
        AABB box = new AABB(pos.x - half, pos.y, pos.z - half, pos.x + half, pos.y + STANDING_HEIGHT, pos.z + half)
                .deflate(0.02D);
        return level().noCollision(this, box);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (tickCount % 5 != 0) {
            return;
        }
        Vec3 move = getDeltaMovement();
        Vec3 ahead = new Vec3(move.x, 0.0D, move.z);
        ahead = ahead.lengthSqr() > 1.0E-4D ? ahead.normalize().scale(1.3D) : Vec3.ZERO;
        boolean fits = standingFitsAt(position()) && standingFitsAt(position().add(ahead));
        boolean noPath = getTarget() != null && getNavigation().getPath() == null && distanceTo(getTarget()) > 5.0F;
        boolean crouching = getPose() == Pose.CROUCHING;

        if (!crouching && (!fits || noPath)) {
            setPose(Pose.CROUCHING);
            crouchSince = tickCount;
        } else if (crouching && fits && !noPath && tickCount - crouchSince > MIN_CROUCH_TICKS) {
            setPose(Pose.STANDING);
        }
    }

    // ----------------------------------------------------------------- fogo azul
    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            for (int i = 0; i < 4; i++) {
                level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, getRandomX(1.0D), getRandomY(), getRandomZ(1.0D),
                        0.0D, 0.03D, 0.0D);
            }
        }
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE)) {
            return false;
        }
        return super.hurt(source, amount);
    }
}
