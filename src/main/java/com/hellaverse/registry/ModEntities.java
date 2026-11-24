package com.hellaverse.registry;

import com.hellaverse.HellaverseMod;
import com.hellaverse.entity.BlackNoirEntity;
import com.hellaverse.entity.HomelanderEntity;
import com.hellaverse.entity.KimikoEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = HellaverseMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HellaverseMod.MODID);

    public static final RegistryObject<EntityType<BlackNoirEntity>> BLACK_NOIR = ENTITY_TYPES.register("black_noir",
            () -> EntityType.Builder.of(BlackNoirEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build(HellaverseMod.MODID + ":black_noir"));

    public static final RegistryObject<EntityType<HomelanderEntity>> HOMELANDER = ENTITY_TYPES.register("homelander",
            () -> EntityType.Builder.of(HomelanderEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(16)
                    .build(HellaverseMod.MODID + ":homelander"));

    public static final RegistryObject<EntityType<KimikoEntity>> KIMIKO = ENTITY_TYPES.register("kimiko",
            () -> EntityType.Builder.of(KimikoEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build(HellaverseMod.MODID + ":kimiko"));

    private ModEntities() {}

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(BLACK_NOIR.get(), BlackNoirEntity.createAttributes().build());
        event.put(HOMELANDER.get(), HomelanderEntity.createAttributes().build());
        event.put(KIMIKO.get(), KimikoEntity.createAttributes().build());
    }
}
