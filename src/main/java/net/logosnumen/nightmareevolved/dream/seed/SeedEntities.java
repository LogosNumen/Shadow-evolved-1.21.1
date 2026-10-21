package net.logosnumen.nightmareevolved.dream.seed;

import java.util.function.Supplier;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * What the Seeds of Nightmare bring with them: the Gates they open in the waking world, the two knives that can cut the
 * Dark Seed's chains, and the strange gold coins of the Chained Isles that the obsidian knife is bought with.
 */
public final class SeedEntities {
    private static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, NightmareEvolved.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NightmareEvolved.MOD_ID);
    public static final Supplier<EntityType<NightmareGateEntity>> GATE = TYPES.register("nightmare_gate", () -> EntityType.Builder
        .<NightmareGateEntity>of(NightmareGateEntity::new, MobCategory.MISC).sized(3.0F, 10.0F).clientTrackingRange(24).updateInterval(20).noSummon()
        .build("nightmare_gate"));
    /** Cut from a single piece of pristine white stone; given by the Night Temple's black altar for blood shed on it. */
    public static final DeferredItem<SwordItem> IVORY_KNIFE = ITEMS.register("ivory_knife", () -> new SwordItem(Tiers.IRON,
        new Item.Properties().rarity(Rarity.RARE).attributes(SwordItem.createAttributes(Tiers.IRON, 1, -1.6F))));
    /** Cut from a single piece of black stone, crude and sharp; given by the Sanctuary's white altar for a thousand coins. */
    public static final DeferredItem<SwordItem> OBSIDIAN_KNIFE = ITEMS.register("obsidian_knife", () -> new SwordItem(Tiers.DIAMOND,
        new Item.Properties().rarity(Rarity.RARE).attributes(SwordItem.createAttributes(Tiers.DIAMOND, 1, -1.6F))));
    /** A strange gold coin from the ruins of the Chained Isles. */
    public static final DeferredItem<Item> ANCIENT_COIN = ITEMS.register("ancient_coin", () -> new Item(new Item.Properties()));

    private SeedEntities() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        ITEMS.register(modBus);
    }
}
