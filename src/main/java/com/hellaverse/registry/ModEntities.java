package com.hellaverse.registry;

import com.hellaverse.HellaverseMod;
import com.hellaverse.entity.AdamEntity;
import com.hellaverse.entity.AlastorEntity;
import com.hellaverse.entity.AsmodeusEntity;
import com.hellaverse.entity.BlackNoirEntity;
import com.hellaverse.entity.BlueFireballEntity;
import com.hellaverse.entity.CherriBombEntity;
import com.hellaverse.entity.FrenchieEntity;
import com.hellaverse.entity.HomelanderEntity;
import com.hellaverse.entity.KimikoEntity;
import com.hellaverse.entity.LamplighterEntity;
import com.hellaverse.entity.MoxxieEntity;
import com.hellaverse.entity.RocketEntity;
import com.hellaverse.entity.ShadowDemonEntity;
import com.hellaverse.entity.SoldierBoyEntity;
import com.hellaverse.entity.WebWeaverEntity;
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

    public static final RegistryObject<EntityType<WebWeaverEntity>> WEB_WEAVER = ENTITY_TYPES.register("web_weaver",
            () -> EntityType.Builder.of(WebWeaverEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build(HellaverseMod.MODID + ":web_weaver"));

    public static final RegistryObject<EntityType<AdamEntity>> ADAM = ENTITY_TYPES.register("adam",
            () -> EntityType.Builder.of(AdamEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(16)
                    .build(HellaverseMod.MODID + ":adam"));

    public static final RegistryObject<EntityType<SoldierBoyEntity>> SOLDIER_BOY = ENTITY_TYPES.register("soldier_boy",
            () -> EntityType.Builder.of(SoldierBoyEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(16)
                    .build(HellaverseMod.MODID + ":soldier_boy"));

    public static final RegistryObject<EntityType<LamplighterEntity>> LAMPLIGHTER = ENTITY_TYPES.register("lamplighter",
            () -> EntityType.Builder.of(LamplighterEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build(HellaverseMod.MODID + ":lamplighter"));

    public static final RegistryObject<EntityType<CherriBombEntity>> CHERRI_BOMB = ENTITY_TYPES.register("cherri_bomb",
            () -> EntityType.Builder.of(CherriBombEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build(HellaverseMod.MODID + ":cherri_bomb"));

    public static final RegistryObject<EntityType<AlastorEntity>> ALASTOR = ENTITY_TYPES.register("alastor",
            () -> EntityType.Builder.of(AlastorEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(16)
                    .build(HellaverseMod.MODID + ":alastor"));

    public static final RegistryObject<EntityType<ShadowDemonEntity>> SHADOW_DEMON = ENTITY_TYPES.register("shadow_demon",
            () -> EntityType.Builder.of(ShadowDemonEntity::new, MobCategory.MISC)
                    .sized(0.4F, 1.2F)
                    .clientTrackingRange(10)
                    .build(HellaverseMod.MODID + ":shadow_demon"));

    public static final RegistryObject<EntityType<FrenchieEntity>> FRENCHIE = ENTITY_TYPES.register("frenchie",
            () -> EntityType.Builder.of(FrenchieEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build(HellaverseMod.MODID + ":frenchie"));

    public static final RegistryObject<EntityType<MoxxieEntity>> MOXXIE = ENTITY_TYPES.register("moxxie",
            () -> EntityType.Builder.of(MoxxieEntity::new, MobCategory.CREATURE)
                    .sized(0.5F, 1.4F)
                    .clientTrackingRange(10)
                    .build(HellaverseMod.MODID + ":moxxie"));

    public static final RegistryObject<EntityType<AsmodeusEntity>> ASMODEUS = ENTITY_TYPES.register("asmodeus",
            () -> EntityType.Builder.of(AsmodeusEntity::new, MobCategory.MONSTER)
                    .sized(1.5F, AsmodeusEntity.STANDING_HEIGHT)
                    .clientTrackingRange(16)
                    .build(HellaverseMod.MODID + ":asmodeus"));

    public static final RegistryObject<EntityType<BlueFireballEntity>> BLUE_FIREBALL = ENTITY_TYPES.register("blue_fireball",
            () -> EntityType.Builder.<BlueFireballEntity>of(BlueFireballEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(HellaverseMod.MODID + ":blue_fireball"));

    public static final RegistryObject<EntityType<RocketEntity>> ROCKET = ENTITY_TYPES.register("rocket",
            () -> EntityType.Builder.<RocketEntity>of(RocketEntity::new, MobCategory.MISC)
                    .sized(0.4F, 0.4F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(HellaverseMod.MODID + ":rocket"));

    private ModEntities() {}

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(BLACK_NOIR.get(), BlackNoirEntity.createAttributes().build());
        event.put(HOMELANDER.get(), HomelanderEntity.createAttributes().build());
        event.put(KIMIKO.get(), KimikoEntity.createAttributes().build());
        event.put(WEB_WEAVER.get(), WebWeaverEntity.createAttributes().build());
        event.put(LAMPLIGHTER.get(), LamplighterEntity.createAttributes().build());
        event.put(CHERRI_BOMB.get(), CherriBombEntity.createAttributes().build());
        event.put(ALASTOR.get(), AlastorEntity.createAttributes().build());
        event.put(SHADOW_DEMON.get(), ShadowDemonEntity.createAttributes().build());
        event.put(FRENCHIE.get(), FrenchieEntity.createAttributes().build());
        event.put(MOXXIE.get(), MoxxieEntity.createAttributes().build());
        event.put(ASMODEUS.get(), AsmodeusEntity.createAttributes().build());
        event.put(ADAM.get(), AdamEntity.createAttributes().build());
        event.put(SOLDIER_BOY.get(), SoldierBoyEntity.createAttributes().build());
    }
}
