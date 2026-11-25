package com.hellaverse.item;

import com.hellaverse.entity.ShadowDemonEntity;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Alastor Staff: botao direito invoca demonios de sombra que protegem o jogador por 30 s. */
public class AlastorStaffItem extends Item {
    private static final int SUMMON_COUNT = 3;
    private static final int MAX_DEMONS = 9;
    private static final int COOLDOWN_TICKS = 400;

    public AlastorStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            int alive = server.getEntitiesOfClass(ShadowDemonEntity.class, player.getBoundingBox().inflate(64.0D),
                    d -> d.isOwnedBy(player)).size();
            if (alive + SUMMON_COUNT > MAX_DEMONS) {
                return InteractionResultHolder.fail(stack);
            }
            ShadowDemonEntity.summon(server, player, SUMMON_COUNT);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EVOKER_PREPARE_SUMMON, player.getSoundSource(), 1.5F, 0.7F);
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        }
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hellaverse.alastor_staff"));
    }
}
