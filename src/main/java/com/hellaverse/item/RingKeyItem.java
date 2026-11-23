package com.hellaverse.item;

import com.hellaverse.Ring;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

/**
 * Chave provisoria para viajar entre as dimensoes (ate existirem portais de verdade).
 * Botao direito: vai para o anel da chave (mesmas coordenadas X/Z). Usada dentro do anel: volta ao Overworld.
 */
public class RingKeyItem extends Item {
    private final Ring ring;

    public RingKeyItem(Ring ring, Properties properties) {
        super(properties);
        this.ring = ring;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        boolean insideRing = level.dimension().equals(ring.dimension());
        ServerLevel destination = serverPlayer.server.getLevel(insideRing ? Level.OVERWORLD : ring.dimension());
        if (destination == null) {
            serverPlayer.displayClientMessage(Component.translatable("message.hellaverse.no_dimension"), true);
            return InteractionResultHolder.fail(stack);
        }

        teleport(serverPlayer, destination);
        player.getCooldowns().addCooldown(this, 40);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    private static void teleport(ServerPlayer player, ServerLevel destination) {
        int x = Mth.floor(player.getX());
        int z = Mth.floor(player.getZ());
        // Forca o carregamento do chunk de destino para a altura do terreno ser conhecida.
        destination.getChunk(x >> 4, z >> 4);
        int y = destination.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        if (y <= destination.getMinBuildHeight()) {
            y = destination.getSeaLevel() + 1;
        }
        BlockPos target = new BlockPos(x, y, z);
        Vec3 pos = Vec3.atBottomCenterOf(target);

        player.fallDistance = 0.0F;
        player.changeDimension(destination, new ITeleporter() {
            @Override
            public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld,
                                            Function<ServerLevel, PortalInfo> defaultPortalInfo) {
                return new PortalInfo(pos, Vec3.ZERO, entity.getYRot(), entity.getXRot());
            }

            @Override
            public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
                return false;
            }
        });
    }
}
