package net.logosnumen.nightmareevolved.entity.soulsea;

import java.util.function.Supplier;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** What lives in a Soul Sea, and the body a soul leaves behind while it is there. */
public final class SeaEntities {
    private static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, NightmareEvolved.MOD_ID);
    public static final Supplier<EntityType<VesselEntity>> VESSEL = TYPES.register("vessel", () -> EntityType.Builder
        .<VesselEntity>of(VesselEntity::new, MobCategory.MISC).sized(0.6F, 1.3F).clientTrackingRange(10).build("vessel"));
    public static final Supplier<EntityType<SeaSunEntity>> SUN = TYPES.register("sea_sun", () -> EntityType.Builder
        .<SeaSunEntity>of(SeaSunEntity::new, MobCategory.MISC).sized(2.5F, 2.5F).clientTrackingRange(16).noSummon().build("sea_sun"));
    public static final Supplier<EntityType<SeaMemoryEntity>> MEMORY = TYPES.register("sea_memory", () -> EntityType.Builder
        .<SeaMemoryEntity>of(SeaMemoryEntity::new, MobCategory.MISC).sized(0.7F, 0.7F).clientTrackingRange(10).updateInterval(1).noSummon().build("sea_memory"));
    public static final Supplier<EntityType<SeaEchoEntity>> ECHO = TYPES.register("sea_echo", () -> EntityType.Builder
        .<SeaEchoEntity>of(SeaEchoEntity::new, MobCategory.MISC).sized(1.0F, 2.0F).clientTrackingRange(12).noSummon().build("sea_echo"));
    public static final Supplier<EntityType<SeaShadowEntity>> SHADOW = TYPES.register("sea_shadow", () -> EntityType.Builder
        .<SeaShadowEntity>of(SeaShadowEntity::new, MobCategory.MISC).sized(0.6F, 1.8F).clientTrackingRange(10).noSummon().build("sea_shadow"));
    public static final Supplier<EntityType<SeaHoardEntity>> HOARD = TYPES.register("sea_hoard", () -> EntityType.Builder
        .<SeaHoardEntity>of(SeaHoardEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(10).noSummon().build("sea_hoard"));

    private SeaEntities() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(SeaEntities::onAttributes);
    }

    private static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(VESSEL.get(), VesselEntity.attributes().build());
    }
}
