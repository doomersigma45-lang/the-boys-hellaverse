package com.hellaverse.event;

import com.hellaverse.HellaverseMod;
import com.hellaverse.entity.BlackNoirEntity;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Furtividade do Black Noir: os mobs o enxergam de bem mais perto e, mesmo vendo,
 * levam alguns segundos para "perceber" e marca-lo como alvo. Se ele atacou o mob,
 * a reacao e imediata.
 *
 * Os goals de alvo do vanilla so tentam definir o alvo uma vez ao iniciar, entao nao da para
 * apenas recusar o alvo: guardamos o mob numa lista de espera e definimos o alvo depois do atraso.
 */
@Mod.EventBusSubscriber(modid = HellaverseMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NoirStealthEvents {
    /** Multiplicador do alcance de visao dos outros mobs sobre o Black Noir. */
    private static final double VISIBILITY = 0.25D;
    /** Tempo ate o mob perceber o Black Noir (5 s). */
    private static final long NOTICE_DELAY_TICKS = 100L;
    /** Espera descartada se o mob nao conseguir agir dentro deste prazo. */
    private static final long MAX_PENDING_TICKS = 600L;

    private record Pending(BlackNoirEntity noir, long noticeAt) {}

    /** Somente thread do servidor. Chave fraca: mobs removidos somem sozinhos. */
    private static final Map<Mob, Pending> PENDING = new WeakHashMap<>();
    private static boolean applying;

    private NoirStealthEvents() {}

    @SubscribeEvent
    public static void onVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (event.getEntity() instanceof BlackNoirEntity) {
            event.modifyVisibility(VISIBILITY);
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (applying || !(event.getNewTarget() instanceof BlackNoirEntity noir)
                || !(event.getEntity() instanceof Mob seeker)) {
            return;
        }
        if (seeker.getLastHurtByMob() == noir) {
            return; // ele atacou primeiro: reacao imediata
        }
        long now = seeker.level().getGameTime();
        Pending pending = PENDING.get(seeker);
        if (pending == null || pending.noir() != noir) {
            PENDING.put(seeker, new Pending(noir, now + NOTICE_DELAY_TICKS));
        }
        // Ainda nao percebeu: mantem o alvo que ja tinha.
        event.setNewTarget(seeker.getTarget());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()
                || event.getServer().getTickCount() % 10 != 0) {
            return;
        }
        Iterator<Map.Entry<Mob, Pending>> it = PENDING.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Mob, Pending> entry = it.next();
            Mob mob = entry.getKey();
            Pending pending = entry.getValue();
            long now = mob.level().getGameTime();
            BlackNoirEntity noir = pending.noir();

            if (!mob.isAlive() || !noir.isAlive() || now > pending.noticeAt() + MAX_PENDING_TICKS) {
                it.remove();
                continue;
            }
            if (now < pending.noticeAt()) {
                continue;
            }
            it.remove();
            double range = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
            if (mob.getTarget() == null && mob.level() == noir.level()
                    && mob.distanceToSqr(noir) <= range * range
                    && mob.getSensing().hasLineOfSight(noir)) {
                applying = true;
                try {
                    mob.setTarget(noir);
                } finally {
                    applying = false;
                }
            }
        }
    }
}
