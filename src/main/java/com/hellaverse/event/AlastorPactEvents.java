package com.hellaverse.event;

import com.hellaverse.HellaverseMod;
import com.hellaverse.entity.AlastorEntity;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Acordo com o Alastor. /alastor deal <item> [quantidade 1-15] entrega o item (precisa de um Alastor a ate 16 blocos).
 * A partir do primeiro acordo o jogador paga o preco: a cada segundo ha chance de ser teletransportado para longe,
 * de perder uma pilha de itens do inventario e, mais raramente, de levar um dano quase fatal (sobra meio coracao).
 * O Alastor para de atacar quem tem acordo. /alastor release (so operadores) desfaz o acordo.
 */
@Mod.EventBusSubscriber(modid = HellaverseMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AlastorPactEvents {
    public static final int MAX_ITEMS = 15;
    private static final double DEAL_RANGE = 16.0D;

    // chances por segundo
    private static final float TELEPORT_CHANCE = 1.0F / 300.0F;   // em media a cada 5 min
    private static final float ITEM_LOSS_CHANCE = 1.0F / 60.0F;    // em media a cada 1 min
    private static final float NEAR_DEATH_CHANCE = 1.0F / 600.0F;  // em media a cada 10 min

    private static final SimpleCommandExceptionType TOO_FAR = new SimpleCommandExceptionType(
            Component.translatable("message.hellaverse.pact_too_far"));
    private static final SimpleCommandExceptionType PLAYERS_ONLY = new SimpleCommandExceptionType(
            Component.translatable("message.hellaverse.pact_players_only"));

    private AlastorPactEvents() {}

    // ----------------------------------------------------------------- comandos
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("alastor")
                .then(Commands.literal("deal")
                        .then(Commands.argument("item", ItemArgument.item(event.getBuildContext()))
                                .executes(ctx -> deal(ctx.getSource(), ItemArgument.getItem(ctx, "item"), 1))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, MAX_ITEMS))
                                        .executes(ctx -> deal(ctx.getSource(), ItemArgument.getItem(ctx, "item"),
                                                IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("release")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> release(ctx.getSource()))));
    }

    private static int deal(CommandSourceStack source, ItemInput input, int amount) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            throw PLAYERS_ONLY.create();
        }
        List<AlastorEntity> nearby = player.level().getEntitiesOfClass(AlastorEntity.class,
                player.getBoundingBox().inflate(DEAL_RANGE));
        if (nearby.isEmpty()) {
            throw TOO_FAR.create();
        }
        boolean first = !AlastorPact.isBound(player);
        AlastorPact.bind(player);
        for (AlastorEntity alastor : nearby) {
            if (alastor.getTarget() == player) {
                alastor.setTarget(null);
            }
        }

        int max = input.getItem().getMaxStackSize(new ItemStack(input.getItem()));
        int remaining = amount;
        while (remaining > 0) {
            int count = Math.min(remaining, Math.max(1, max));
            ItemStack stack = input.createItemStack(count, false);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            remaining -= count;
        }

        ItemStack shown = new ItemStack(input.getItem());
        player.sendSystemMessage(Component.translatable(first ? "message.hellaverse.pact_signed"
                : "message.hellaverse.pact_deal", amount, shown.getHoverName()));
        player.level().playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_CHARGE,
                SoundSource.PLAYERS, 1.0F, 0.5F);
        return amount;
    }

    private static int release(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            throw PLAYERS_ONLY.create();
        }
        AlastorPact.release(player);
        player.sendSystemMessage(Component.translatable("message.hellaverse.pact_released"));
        return 1;
    }

    // ----------------------------------------------------------------- preco do acordo
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 20 != 0 || !AlastorPact.isBound(player)
                || player.isCreative() || player.isSpectator() || !player.isAlive()) {
            return;
        }
        if (player.getRandom().nextFloat() < ITEM_LOSS_CHANCE) {
            takeRandomItem(player);
        }
        if (player.getRandom().nextFloat() < TELEPORT_CHANCE) {
            teleportRandomly(player);
        }
        if (player.getRandom().nextFloat() < NEAR_DEATH_CHANCE) {
            nearFatalDamage(player);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.isWasDeath() && AlastorPact.isBound(event.getOriginal())) {
            AlastorPact.bind(event.getEntity());
        }
    }

    private static void takeRandomItem(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        int start = player.getRandom().nextInt(inventory.getContainerSize());
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            int slot = (start + i) % inventory.getContainerSize();
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty()) {
                Component name = stack.getHoverName();
                int count = stack.getCount();
                inventory.setItem(slot, ItemStack.EMPTY);
                player.displayClientMessage(Component.translatable("message.hellaverse.pact_item_taken", count, name), true);
                player.level().playSound(null, player.blockPosition(), SoundEvents.SCULK_CLICKING,
                        SoundSource.PLAYERS, 1.0F, 0.6F);
                return;
            }
        }
    }

    private static void teleportRandomly(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
            boolean ceiling = level.dimensionType().hasCeiling();
            double distance = ceiling ? 30.0D + player.getRandom().nextDouble() * 50.0D
                    : 150.0D + player.getRandom().nextDouble() * 350.0D;
            int x = (int) Math.floor(player.getX() + Math.cos(angle) * distance);
            int z = (int) Math.floor(player.getZ() + Math.sin(angle) * distance);
            BlockPos target = ceiling ? findCaveSpot(level, x, (int) player.getY(), z)
                    : level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
            if (target == null || !level.getWorldBorder().isWithinBounds(target)) {
                continue;
            }
            if (!level.getFluidState(target.below()).isEmpty() && !ceiling) {
                continue; // evita cair em oceano/lava
            }
            player.teleportTo(level, target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D,
                    player.getYRot(), player.getXRot());
            player.fallDistance = 0.0F;
            player.displayClientMessage(Component.translatable("message.hellaverse.pact_teleport"), true);
            level.playSound(null, target, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.5F);
            return;
        }
    }

    /** Em dimensoes com teto: procura um chao com 2 blocos de ar, de cima para baixo. */
    private static BlockPos findCaveSpot(ServerLevel level, int x, int startY, int z) {
        int top = Math.min(level.getMaxBuildHeight() - 10, startY + 24);
        int bottom = level.getMinBuildHeight() + 5;
        for (int y = top; y > bottom; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()
                    && level.getBlockState(pos.below()).isSolid() && level.getFluidState(pos).isEmpty()) {
                return pos;
            }
        }
        return null;
    }

    private static void nearFatalDamage(ServerPlayer player) {
        if (player.getHealth() <= 2.0F) {
            return;
        }
        player.setHealth(1.0F);
        player.level().broadcastEntityEvent(player, (byte) 2); // animacao de dano
        player.displayClientMessage(Component.translatable("message.hellaverse.pact_near_death"), true);
        player.level().playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM,
                SoundSource.PLAYERS, 0.8F, 0.5F);
    }
}
