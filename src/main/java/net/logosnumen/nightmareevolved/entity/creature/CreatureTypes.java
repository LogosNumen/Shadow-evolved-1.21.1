package net.logosnumen.nightmareevolved.entity.creature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.creature.kind.Kind;
import net.logosnumen.nightmareevolved.creature.kind.Kinds;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The entity type, spawn egg and attributes of every kind of Dream Realm creature, and the bolts they loose. */
public final class CreatureTypes {
    private static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, NightmareEvolved.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NightmareEvolved.MOD_ID);
    private static final Map<String, DeferredHolder<EntityType<?>, EntityType<DreamCreature>>> BY_ID = new LinkedHashMap<>();
    private static final Map<String, DeferredItem<DeferredSpawnEggItem>> EGGS = new LinkedHashMap<>();
    private static final Map<EntityType<?>, Kind> KINDS = new HashMap<>();
    public static final Supplier<EntityType<CreatureBolt>> BOLT = TYPES.register("creature_bolt", () -> EntityType.Builder
        .<CreatureBolt>of(CreatureBolt::new, MobCategory.MISC).sized(0.4F, 0.4F).clientTrackingRange(8).updateInterval(2).noSummon().build("creature_bolt"));

    static {
        for (Kind kind : Kinds.all()) {
            DeferredHolder<EntityType<?>, EntityType<DreamCreature>> type = TYPES.register(kind.id, () -> build(kind));
            BY_ID.put(kind.id, type);
            EGGS.put(kind.id, ITEMS.register(kind.id + "_spawn_egg", () -> new DeferredSpawnEggItem(type, kind.eggBase, kind.eggSpots, new Item.Properties())));
        }
    }

    private CreatureTypes() {
    }

    private static EntityType<DreamCreature> build(Kind kind) {
        Kind.Factory factory = kind.factory != null ? kind.factory : DreamCreature::new;
        EntityType.Builder<DreamCreature> builder = EntityType.Builder.<DreamCreature>of((type, level) -> factory.create(type, level, kind), MobCategory.MONSTER)
            .sized(kind.width, kind.height)
            .clientTrackingRange(kind.height > 5.0F ? 12 : 8);
        if (kind.has(Kind.Trait.FIREPROOF)) {
            builder.fireImmune();
        }

        EntityType<DreamCreature> type = builder.build(kind.id);
        KINDS.put(type, kind);
        return type;
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(CreatureTypes::onAttributes);
        NeoForge.EVENT_BUS.addListener(DreamCreature::onFinalizeSpawn);
    }

    public static EntityType<DreamCreature> type(Kind kind) {
        return BY_ID.get(kind.id).get();
    }

    /** Every kind's entity type, in the order the kinds are listed. */
    public static List<EntityType<DreamCreature>> types() {
        List<EntityType<DreamCreature>> types = new ArrayList<>();
        BY_ID.values().forEach(holder -> types.add(holder.get()));
        return types;
    }

    @Nullable
    public static Kind kindOf(EntityType<?> type) {
        return KINDS.get(type);
    }

    public static List<Item> eggs() {
        List<Item> eggs = new ArrayList<>();
        EGGS.values().forEach(egg -> eggs.add(egg.get()));
        return eggs;
    }

    private static void onAttributes(EntityAttributeCreationEvent event) {
        for (Kind kind : Kinds.all()) {
            event.put(type(kind), DreamCreature.attributes(kind).build());
        }
    }
}
