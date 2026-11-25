package com.hellaverse.item;

import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Arma de fogo de acerto instantaneo (hitscan). Cada tiro gasta 1 municao do tipo da arma.
 * Armas automaticas atiram enquanto o botao direito estiver pressionado; as demais, um tiro por clique.
 * As balas usam o dano "thrown", que conta como projetil (Black Noir e Homelander sao imunes).
 */
public class GunItem extends Item {
    /** Configuracao de cada arma. Angulos em graus, alcance em blocos, intervalo em ticks. */
    public record Stats(float damage, int interval, float spread, double range, boolean automatic,
                        SoundEvent sound, float pitch) {}

    private static final DustParticleOptions TRACER = new DustParticleOptions(new Vector3f(1.0F, 0.9F, 0.4F), 0.6F);
    private static final int USE_DURATION = 72000;

    private final Supplier<? extends Item> ammo;
    protected final Stats stats;

    public GunItem(Properties properties, Supplier<? extends Item> ammo, Stats stats) {
        super(properties);
        this.ammo = ammo;
        this.stats = stats;
    }

    // ----------------------------------------------------------------- uso
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!hasAmmo(player)) {
            if (!level.isClientSide) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8F, 1.6F);
            }
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            fire(level, player);
        }
        if (stats.automatic()) {
            player.startUsingItem(hand);
        } else {
            player.getCooldowns().addCooldown(this, stats.interval());
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (level.isClientSide || !stats.automatic() || !(entity instanceof Player player)) {
            return;
        }
        int used = getUseDuration(stack) - remaining;
        if (used > 0 && used % stats.interval() == 0 && !fire(level, player)) {
            player.stopUsingItem();
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            player.getCooldowns().addCooldown(this, stats.interval()); // evita burlar a cadencia soltando e apertando
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return stats.automatic() ? USE_DURATION : 0;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    // ----------------------------------------------------------------- tiro
    /** Gasta 1 municao e dispara. Retorna false se nao havia municao. */
    protected boolean fire(Level level, Player player) {
        if (!consumeAmmo(player)) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8F, 1.6F);
            return false;
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                stats.sound(), SoundSource.PLAYERS, 1.2F, stats.pitch());
        shoot((ServerLevel) level, player);
        return true;
    }

    protected void shoot(ServerLevel level, Player player) {
        shootFrom(level, player, stats, 0.0F);
    }

    public Stats stats() {
        return stats;
    }

    /** Tiro instantaneo a partir de qualquer ser vivo (jogador ou mob). extraSpread soma graus de dispersao. */
    public static void shootFrom(ServerLevel level, LivingEntity player, Stats stats, float extraSpread) {
        Vec3 from = player.getEyePosition();
        float spread = stats.spread() + extraSpread;
        float yaw = player.getYRot() + (float) (level.random.nextGaussian() * spread);
        float pitch = player.getXRot() + (float) (level.random.nextGaussian() * spread);
        Vec3 dir = Vec3.directionFromRotation(pitch, yaw);
        Vec3 to = from.add(dir.scale(stats.range()));

        BlockHitResult blockHit = level.clip(new ClipContext(from, to,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 end = blockHit.getType() == HitResult.Type.MISS ? to : blockHit.getLocation();

        AABB box = new AABB(from, end).inflate(1.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level, player, from, end, box,
                e -> !e.isSpectator() && e.isPickable());
        if (entityHit != null) {
            end = entityHit.getLocation();
            var target = entityHit.getEntity();
            target.invulnerableTime = 0; // permite tiros em sequencia
            target.hurt(level.damageSources().thrown(player, player), stats.damage());
        }

        drawTracer(level, from.add(dir.scale(1.2D)), end);
        level.sendParticles(ParticleTypes.CRIT, end.x, end.y, end.z, 3, 0.05D, 0.05D, 0.05D, 0.1D);
        Vec3 muzzle = from.add(dir.scale(0.9D));
        level.sendParticles(ParticleTypes.SMOKE, muzzle.x, muzzle.y - 0.1D, muzzle.z, 2, 0.03D, 0.03D, 0.03D, 0.01D);
    }

    private static void drawTracer(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 delta = to.subtract(from);
        double length = delta.length();
        if (length < 0.1D) {
            return;
        }
        Vec3 step = delta.normalize().scale(1.5D);
        Vec3 point = from;
        for (double travelled = 0.0D; travelled < length; travelled += 1.5D) {
            level.sendParticles(TRACER, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            point = point.add(step);
        }
    }

    // ----------------------------------------------------------------- municao
    protected Item ammoItem() {
        return ammo.get();
    }

    protected boolean hasAmmo(Player player) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        Item item = ammoItem();
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) {
                return true;
            }
        }
        return player.getOffhandItem().is(item);
    }

    protected boolean consumeAmmo(Player player) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        Item item = ammoItem();
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(item)) {
            offhand.shrink(1);
            return true;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hellaverse.gun_ammo", ammoItem().getDescription()));
    }
}
