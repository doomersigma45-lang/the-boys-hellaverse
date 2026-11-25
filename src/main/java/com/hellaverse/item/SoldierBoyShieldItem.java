package com.hellaverse.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;

/**
 * Escudo do Soldier Boy: bloqueia como um escudo normal, causa 30 de dano em combate corpo a corpo
 * (1 base do jogador + 29) e tem mais durabilidade que netherite (2031).
 */
public class SoldierBoyShieldItem extends ShieldItem {
    public static final int DURABILITY = 3000;
    /** Dano total do golpe com o escudo. */
    public static final double TOTAL_DAMAGE = 30.0D;
    /** Bonus de ataque do item (o jogador ja tem 1 de base). */
    public static final double ATTACK_BONUS = TOTAL_DAMAGE - 1.0D;

    private final Multimap<Attribute, AttributeModifier> mainHandModifiers;

    public SoldierBoyShieldItem(Properties properties) {
        super(properties.durability(DURABILITY).fireResistant());
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,
                "Weapon modifier", ATTACK_BONUS, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID,
                "Weapon modifier", -2.4D, AttributeModifier.Operation.ADDITION));
        this.mainHandModifiers = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? mainHandModifiers : super.getDefaultAttributeModifiers(slot);
    }
}
