package net.logosnumen.nightmareevolved.entity.soul;

import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The bodies Aspects make: constructs in their master's shape, and swords moved by will. */
public final class SoulEntities {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, NightmareEvolved.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<SoulConstructEntity>> CONSTRUCT = ENTITY_TYPES.register(
        "soul_construct", () -> EntityType.Builder.of(SoulConstructEntity::new, MobCategory.MISC).sized(0.6F, 1.8F).clientTrackingRange(10).build("soul_construct")
    );
    public static final DeferredHolder<EntityType<?>, EntityType<FlyingSwordEntity>> FLYING_SWORD = ENTITY_TYPES.register(
        "flying_sword", () -> EntityType.Builder.<FlyingSwordEntity>of(FlyingSwordEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(8)
            .updateInterval(1).build("flying_sword")
    );

    private SoulEntities() {
    }

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
        modBus.addListener(SoulEntities::onAttributes);
    }

    private static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(CONSTRUCT.get(), SoulConstructEntity.createAttributes().build());
    }
}
