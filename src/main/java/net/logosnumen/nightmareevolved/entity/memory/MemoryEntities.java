package net.logosnumen.nightmareevolved.entity.memory;

import java.util.function.Supplier;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Memories out in the world on their own: one thrown from the hand, and the Covetous Coffer walking after its owner. */
public final class MemoryEntities {
    private static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, NightmareEvolved.MOD_ID);
    public static final Supplier<EntityType<ThrownMemoryEntity>> THROWN_MEMORY = TYPES.register("thrown_memory", () -> EntityType.Builder
        .<ThrownMemoryEntity>of(ThrownMemoryEntity::new, MobCategory.MISC).sized(0.3F, 0.3F).clientTrackingRange(6).updateInterval(5)
        .build("thrown_memory"));
    public static final Supplier<EntityType<WalkingCofferEntity>> WALKING_COFFER = TYPES.register("walking_coffer", () -> EntityType.Builder
        .<WalkingCofferEntity>of(WalkingCofferEntity::new, MobCategory.MISC).sized(0.9F, 0.9F).clientTrackingRange(8).build("walking_coffer"));

    private MemoryEntities() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(MemoryEntities::onAttributes);
    }

    private static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(WALKING_COFFER.get(), WalkingCofferEntity.attributes().build());
    }
}
