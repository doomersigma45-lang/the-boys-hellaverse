package com.hellaverse.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;

/** Pe de cabra do Billy Butcher: arma corpo a corpo (5 de dano no total, ataque rapido). */
public class CrowbarItem extends Item {
    public static final double TOTAL_DAMAGE = 5.0D;

    private final Multimap<Attribute, AttributeModifier> mainHandModifiers;

    public CrowbarItem(Properties properties) {
        super(properties.durability(350));
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,
                "Weapon modifier", TOTAL_DAMAGE - 1.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID,
                "Weapon modifier", -2.2D, AttributeModifier.Operation.ADDITION));
        this.mainHandModifiers = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? mainHandModifiers : super.getDefaultAttributeModifiers(slot);
    }
}
