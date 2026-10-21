package net.logosnumen.nightmareevolved.memory;

import static net.logosnumen.nightmareevolved.memory.Enchant.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.logosnumen.nightmareevolved.memory.MemoryDef.Kind;
import net.logosnumen.nightmareevolved.memory.MemoryDef.Shape;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;

/**
 * Every Memory the mod knows. First the named Memories of the novel, each with the Rank, Tier and enchantments it has
 * there (the seven Shard Memories of the Forgotten Shore among them); then the shapes the Spell weaves for creatures
 * it has no particular Memory for, which take their Rank and Tier from their prey and roll their own enchantments.
 */
public final class Memories {
    private static final Map<String, MemoryDef> ALL = new LinkedHashMap<>();

    // The Shard Memories of the Forgotten Shore.
    public static final MemoryDef MIDNIGHT_SHARD = add(MemoryDef.named("midnight_shard", Kind.WEAPON, Shape.TACHI, Tier.AWAKENED, 3)
        .with(UNBROKEN).shard().from("carapace_demon").palette(Vfx.Palette.STEEL));
    public static final MemoryDef ZENITH_SHARD = add(MemoryDef.named("zenith_shard", Kind.WEAPON, Shape.SPEAR, Tier.AWAKENED, 5)
        .with(ZENITH).shard().filled().from("iron_spider_matriarch").palette(Vfx.Palette.EARTH));
    public static final MemoryDef DAWN_SHARD = add(MemoryDef.named("dawn_shard", Kind.TOOL, Shape.THING, Tier.ASCENDED, 1)
        .with(DAWN).worn().shard().from("first_lord_of_bright_castle").palette(Vfx.Palette.LIGHT));
    public static final MemoryDef DUSK_SHARD = add(MemoryDef.named("dusk_shard", Kind.WEAPON, Shape.SHIELD, Tier.ASCENDED, 2)
        .with(INDOMITABLE).shard().from("cursed_herald").palette(Vfx.Palette.SHADOW));
    public static final MemoryDef MOONLIGHT_SHARD = add(MemoryDef.named("moonlight_shard", Kind.WEAPON, Shape.STILETTO, Tier.ASCENDED, 1)
        .with(UNSEEN).shard().palette(Vfx.Palette.FROST));
    public static final MemoryDef SUNLIGHT_SHARD = add(MemoryDef.named("sunlight_shard", Kind.WEAPON, Shape.WAR_HAMMER, Tier.ASCENDED, 3)
        .with(SUNLIGHT).shard().filled().palette(Vfx.Palette.LIGHT));
    public static final MemoryDef STARLIGHT_SHARD = add(MemoryDef.named("starlight_shard", Kind.GARMENT, Shape.THING, Tier.ASCENDED, 5)
        .with(STARLIGHT).shard().filled().from("lord_of_the_dead").palette(Vfx.Palette.FROST));

    // Dormant.
    public static final MemoryDef ENDLESS_SPRING = add(MemoryDef.named("endless_spring", Kind.TOOL, Shape.THING, Tier.DORMANT, 4)
        .with(GIFT_OF_WATER, BLESSING_OF_FLESH).palette(Vfx.Palette.FROST));
    public static final MemoryDef SILVER_BELL = add(MemoryDef.named("silver_bell", Kind.TOOL, Shape.THING, Tier.DORMANT, 2)
        .with(SILVER_SONG, SONOROUS).palette(Vfx.Palette.STEEL));
    public static final MemoryDef DREAM_BLADE = add(MemoryDef.named("dream_blade", Kind.WEAPON, Shape.LONGSWORD, Tier.DORMANT, 1)
        .with(DREAMERS_WARD).palette(Vfx.Palette.STEEL));
    public static final MemoryDef EVERTWINE = add(MemoryDef.named("evertwine", Kind.TOOL, Shape.THING, Tier.DORMANT, 1)
        .with(GOLDEN_TWINE).filled().palette(Vfx.Palette.LIGHT));

    // Awakened.
    public static final MemoryDef AZURE_BLADE = add(MemoryDef.named("azure_blade", Kind.WEAPON, Shape.BLADE, Tier.AWAKENED, 1)
        .from("carapace_scavenger").palette(Vfx.Palette.FROST));
    public static final MemoryDef STARLIGHT_LEGION_ARMOR = add(MemoryDef.named("starlight_legion_armor", Kind.ARMOR, Shape.ARMOR, Tier.AWAKENED, 2)
        .from("carapace_centurion").palette(Vfx.Palette.STEEL));
    public static final MemoryDef DARK_WING = add(MemoryDef.named("dark_wing", Kind.GARMENT, Shape.THING, Tier.AWAKENED, 1)
        .with(GLIDE).from("flesh_reaver").palette(Vfx.Palette.FROST));
    public static final MemoryDef PUPPETEERS_SHROUD = add(MemoryDef.named("puppeteers_shroud", Kind.ARMOR, Shape.ARMOR, Tier.AWAKENED, 5)
        .with(ENHANCED_DURABILITY, DOUBTLESS, BLESSING_OF_SPIRIT).layers(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
        .from("mountain_king").palette(Vfx.Palette.SHADOW));
    public static final MemoryDef BLOOD_BLOSSOM = add(MemoryDef.named("blood_blossom", Kind.CHARM, Shape.THING, Tier.AWAKENED, 2)
        .with(FLOWER_OF_EVIL).from("blood_flower").palette(Vfx.Palette.BLOOD));
    public static final MemoryDef BROKEN_OATH = add(MemoryDef.named("broken_oath", Kind.CHARM, Shape.THING, Tier.AWAKENED, 1)
        .with(OATHBREAKER).from("golem_lord").palette(Vfx.Palette.SHADOW));
    public static final MemoryDef PROWLING_THORN = add(MemoryDef.named("prowling_thorn", Kind.WEAPON, Shape.DAGGER, Tier.AWAKENED, 2)
        .with(ROSE_OF_BETRAYAL).palette(Vfx.Palette.ACID));
    public static final MemoryDef EXTRAORDINARY_ROCK = add(MemoryDef.named("extraordinary_rock", Kind.TOOL, Shape.THING, Tier.AWAKENED, 1)
        .with(NOT_REALLY).from("rolling_stone").palette(Vfx.Palette.EARTH));
    public static final MemoryDef HEAVENLY_BURDEN = add(MemoryDef.named("heavenly_burden", Kind.TOOL, Shape.THING, Tier.AWAKENED, 3)
        .with(UNBURDENED).palette(Vfx.Palette.LIGHT));
    public static final MemoryDef ESSENCE_QUIVER = add(MemoryDef.named("essence_quiver", Kind.TOOL, Shape.ARROW, Tier.AWAKENED, 2)
        .with(ESSENCE_ARROWS).filled().palette(Vfx.Palette.RUNE));
    public static final MemoryDef SILENT_DAGGER = add(MemoryDef.named("silent_dagger", Kind.WEAPON, Shape.DAGGER, Tier.AWAKENED, 1)
        .with(HUSH).palette(Vfx.Palette.SHADOW));
    public static final MemoryDef ENCHANTED_BAG = add(MemoryDef.named("enchanted_bag", Kind.TOOL, Shape.THING, Tier.AWAKENED, 1)
        .with(SPILLING_BAG).palette(Vfx.Palette.EARTH));

    // Ascended.
    public static final MemoryDef BLOOD_ARROW = add(MemoryDef.named("blood_arrow", Kind.WEAPON, Shape.ARROW, Tier.ASCENDED, 1)
        .with(RAIN_OF_BLOOD, RESTITUTION).from("corpse_eater").palette(Vfx.Palette.BLOOD));
    public static final MemoryDef BLACKHORN_BOW = add(MemoryDef.named("blackhorn_bow", Kind.WEAPON, Shape.BOW, Tier.ASCENDED, 1)
        .with(UNYIELDING_HORN).filled().palette(Vfx.Palette.EARTH));
    public static final MemoryDef FULL_QUIVER = add(MemoryDef.named("full_quiver", Kind.WEAPON, Shape.ARROW, Tier.ASCENDED, 1)
        .with(ENDLESS_VOLLEY).palette(Vfx.Palette.STEEL));
    public static final MemoryDef STRIKE_OF_THUNDER = add(MemoryDef.named("strike_of_thunder", Kind.WEAPON, Shape.ARROW, Tier.ASCENDED, 2)
        .with(CAGED_LIGHTNING).palette(Vfx.Palette.RUNE));
    public static final MemoryDef CRUEL_SIGHT = add(MemoryDef.named("cruel_sight", Kind.WEAPON, Shape.LONGSWORD, Tier.ASCENDED, 4)
        .with(SHAPESHIFTER, LIGHT_EATER, GHOST_BLADE, DARK_MIRROR).from("mirror_beast").palette(Vfx.Palette.FROST));
    public static final MemoryDef COVETOUS_COFFER = add(MemoryDef.named("covetous_coffer", Kind.TOOL, Shape.THING, Tier.ASCENDED, 4)
        .with(MENDACIOUS_COFFER, CAPACIOUS_CHEST, LOCOMOTIVE_CHIFFONIER, LURID_TRUNK).from("mordant_mimic").palette(Vfx.Palette.EARTH));
    public static final MemoryDef MORGANS_WARBOW = add(MemoryDef.named("morgans_warbow", Kind.WEAPON, Shape.BOW, Tier.ASCENDED, 4)
        .with(SOUL_ARROWS, UNBENDING, BURDEN_OF_PEACE, DEATH_DEALER).palette(Vfx.Palette.STEEL));
    public static final MemoryDef MEMORY_OF_FIRE = add(MemoryDef.named("memory_of_fire", Kind.CHARM, Shape.THING, Tier.ASCENDED, 1)
        .with(SCORCHING_HEAT).palette(Vfx.Palette.FIRE));
    public static final MemoryDef MEMORY_OF_ICE = add(MemoryDef.named("memory_of_ice", Kind.CHARM, Shape.THING, Tier.ASCENDED, 1)
        .with(BITTER_COLD).palette(Vfx.Palette.FROST));
    public static final MemoryDef MIRROR_OF_TRUTH = add(MemoryDef.named("mirror_of_truth", Kind.TOOL, Shape.THING, Tier.ASCENDED, 7)
        .with(SEARCHING_REFLECTION).from("defiled_seeker_of_truth").palette(Vfx.Palette.FROST));
    public static final MemoryDef PATIENT_AVENGER = add(MemoryDef.named("patient_avenger", Kind.WEAPON, Shape.SHIELD, Tier.ASCENDED, 3)
        .with(TENACIOUS, BURNING_HEART, COLD_STEEL).palette(Vfx.Palette.FIRE));
    public static final MemoryDef BITTER_CUSP = add(MemoryDef.named("bitter_cusp", Kind.TOOL, Shape.THING, Tier.ASCENDED, 1)
        .with(BITTER_DRAUGHT).filled().from("swarm_of_black_tongues").palette(Vfx.Palette.ACID));
    public static final MemoryDef NAMELESS_SUN = add(MemoryDef.named("nameless_sun", Kind.WEAPON, Shape.LONGSWORD, Tier.ASCENDED, 6)
        .with(RADIANCE).filled().from("crimson_terror").palette(Vfx.Palette.LIGHT));
    public static final MemoryDef MANTLE_OF_THE_UNDERWORLD = add(MemoryDef.named("mantle_of_the_underworld", Kind.ARMOR, Shape.ARMOR, Tier.ASCENDED, 6)
        .with(LIVING_STONE, FEATHER_OF_TRUTH, STALWART, UNDERWORLD_ARMAMENT, PRINCE_OF_THE_UNDERWORLD, SOULBOUND_RELIC).palette(Vfx.Palette.EARTH));
    public static final MemoryDef BAG_OF_WITHHOLDING = add(MemoryDef.named("bag_of_withholding", Kind.TOOL, Shape.THING, Tier.ASCENDED, 1)
        .with(WITHHOLDING).palette(Vfx.Palette.EARTH));
    public static final MemoryDef DIRE_WARNING_CHARM = add(MemoryDef.named("dire_warning", Kind.CHARM, Shape.THING, Tier.ASCENDED, 1)
        .with(DIRE_WARNING).filled().palette(Vfx.Palette.FIRE));
    public static final MemoryDef ALTAR_OF_DENIAL = add(MemoryDef.named("altar_of_denial", Kind.TOOL, Shape.THING, Tier.ASCENDED, 1)
        .with(DENIAL).worn().filled().palette(Vfx.Palette.LIGHT));

    // Transcendent.
    public static final MemoryDef ANANKES_MANTLE = add(MemoryDef.named("anankes_mantle", Kind.GARMENT, Shape.THING, Tier.TRANSCENDENT, 1)
        .with(ENDURING, CONCEALING, TASTELESS).palette(Vfx.Palette.SHADOW));
    public static final MemoryDef DYING_WISH = add(MemoryDef.named("dying_wish", Kind.CHARM, Shape.THING, Tier.TRANSCENDENT, 1)
        .with(DEATH_WISH).from("solvane").palette(Vfx.Palette.BLOOD));
    public static final MemoryDef SHROUD_OF_GRACELESS_DUSK = add(MemoryDef.named("shroud_of_graceless_dusk", Kind.ARMOR, Shape.ARMOR, Tier.TRANSCENDENT, 6)
        .with(SHARPENED_MIND, DUSK_WELLSPRING, BLESSING_OF_SPIRIT, BLESSING_OF_FLESH, SUBLIME_SILK, SYBILS_BLESSING)
        .layers(EquipmentSlot.CHEST, EquipmentSlot.LEGS).from("sybil_of_the_fallen_grace").palette(Vfx.Palette.SHADOW));
    public static final MemoryDef SIN_OF_SOLACE = add(MemoryDef.named("sin_of_solace", Kind.WEAPON, Shape.LONGSWORD, Tier.TRANSCENDENT, 5)
        .with(SINISTER_WHISPER, PERFECT_JADE, OMEN_OF_DREAD, HIDEOUS_TRUTH, CURSED).from("remnant_of_the_jade_queen").palette(Vfx.Palette.ACID));
    public static final MemoryDef STIFLED_SCREAM = add(MemoryDef.named("stifled_scream", Kind.CHARM, Shape.THING, Tier.TRANSCENDENT, 3)
        .with(SILENT_MIGHT, WORD_OF_POWER).from("voiceless_prelate").palette(Vfx.Palette.SHADOW));
    public static final MemoryDef UNDYING_CHAIN = add(MemoryDef.named("undying_chain", Kind.ARMOR, Shape.ARMOR, Tier.TRANSCENDENT, 3)
        .with(CHAINS_OF_LONGING, BRILLIANT_WILL, UNDYING).layers(EquipmentSlot.CHEST).from("wormvine").palette(Vfx.Palette.STEEL));
    public static final MemoryDef DARK_SHAPER = add(MemoryDef.named("dark_shaper", Kind.TOOL, Shape.MALLET, Tier.TRANSCENDENT, 4)
        .with(SCULPTORS_WILL).palette(Vfx.Palette.SHADOW));

    // Supreme, Sacred and Divine.
    public static final MemoryDef CROWN_OF_TWILIGHT = add(MemoryDef.named("crown_of_twilight", Kind.TOOL, Shape.THING, Tier.SUPREME, 1)
        .with(LEGACY_OF_TWILIGHT, KINGS_RESENTMENT, ROYAL_PROMISE).worn().from("daeron_of_the_twilight_sea").palette(Vfx.Palette.FROST));
    public static final MemoryDef CROWN_OF_DAWN = add(MemoryDef.named("crown_of_dawn", Kind.TOOL, Shape.THING, Tier.SUPREME, 1)
        .with(DAWN).worn().palette(Vfx.Palette.LIGHT));
    public static final MemoryDef BEAST_LOCKET = add(MemoryDef.named("beast_locket", Kind.TOOL, Shape.THING, Tier.SUPREME, 1)
        .with(POCKET_MEADOW).palette(Vfx.Palette.EARTH));
    public static final MemoryDef CONDEMNATION = add(MemoryDef.named("condemnation", Kind.WEAPON, Shape.GREATSWORD, Tier.SACRED, 6)
        .with(JUDGEMENT).filled().from("condemnation").palette(Vfx.Palette.STEEL));
    public static final MemoryDef EVENING_STAR = add(MemoryDef.named("evening_star", Kind.CHARM, Shape.THING, Tier.SACRED, 1)
        .with(BOUNDLESS_POWER).palette(Vfx.Palette.LIGHT));
    public static final MemoryDef WEAVERS_MASK = add(MemoryDef.named("weavers_mask", Kind.TOOL, Shape.THING, Tier.DIVINE, 7)
        .with(MANTLE_OF_LIES, WHERE_IS_MY_EYE, SIMPLE_TRICK).worn().palette(Vfx.Palette.SHADOW));
    public static final MemoryDef SHADOW_LANTERN = add(MemoryDef.named("shadow_lantern", Kind.TOOL, Shape.THING, Tier.DIVINE, 1)
        .with(GATES_OF_SHADOW).palette(Vfx.Palette.SHADOW));
    public static final MemoryDef DROP_OF_ICHOR = add(MemoryDef.named("drop_of_ichor", Kind.LINEAGE, Shape.THING, Tier.SUPREME, 4)
        .with(WEAVERS_LINEAGE).from("vile_thieving_birds_spawn").palette(Vfx.Palette.BLOOD));

    // The shapes the Spell weaves for anonymous prey.
    public static final MemoryDef BLADE = add(MemoryDef.generic("blade", Kind.WEAPON, Shape.BLADE));
    public static final MemoryDef SPEAR = add(MemoryDef.generic("spear", Kind.WEAPON, Shape.SPEAR));
    public static final MemoryDef AXE = add(MemoryDef.generic("axe", Kind.WEAPON, Shape.AXE));
    public static final MemoryDef DAGGER = add(MemoryDef.generic("dagger", Kind.WEAPON, Shape.DAGGER));
    public static final MemoryDef BOW = add(MemoryDef.generic("bow", Kind.WEAPON, Shape.BOW));
    public static final MemoryDef SHIELD = add(MemoryDef.generic("shield", Kind.WEAPON, Shape.SHIELD));
    public static final MemoryDef BONE_MAIL = add(MemoryDef.generic("bone_mail", Kind.ARMOR, Shape.ARMOR));
    public static final MemoryDef CHITIN_MAIL = add(MemoryDef.generic("chitin_mail", Kind.ARMOR, Shape.ARMOR));
    public static final MemoryDef PLATE_MAIL = add(MemoryDef.generic("plate_mail", Kind.ARMOR, Shape.ARMOR));
    public static final MemoryDef CHARM = add(MemoryDef.generic("charm", Kind.CHARM, Shape.THING));
    public static final MemoryDef CLOAK = add(MemoryDef.generic("cloak", Kind.GARMENT, Shape.THING));
    public static final MemoryDef LANTERN = add(MemoryDef.generic("lantern", Kind.TOOL, Shape.THING).with(WAYFINDER));
    public static final MemoryDef CANTEEN = add(MemoryDef.generic("canteen", Kind.TOOL, Shape.THING).with(SPRING_WATER));
    public static final MemoryDef HORN = add(MemoryDef.generic("horn", Kind.TOOL, Shape.THING).with(RALLYING_CALL));

    /** The Shard Memories, which manifest the Oath Keys once all seven are held by one soul. */
    public static final List<MemoryDef> SHARDS = List.of(MIDNIGHT_SHARD, ZENITH_SHARD, DAWN_SHARD, DUSK_SHARD, MOONLIGHT_SHARD, SUNLIGHT_SHARD, STARLIGHT_SHARD);
    /** Memories the Spell may give at the end of a First Nightmare, besides one woven from its guardian. */
    public static final List<MemoryDef> FIRST_NIGHTMARE = List.of(ENDLESS_SPRING, SILVER_BELL, DREAM_BLADE, EVERTWINE);

    private Memories() {
    }

    private static MemoryDef add(MemoryDef.Builder builder) {
        MemoryDef def = builder.build();
        ALL.put(def.id, def);
        return def;
    }

    public static List<MemoryDef> all() {
        return List.copyOf(ALL.values());
    }

    @Nullable
    public static MemoryDef byId(String id) {
        return ALL.get(id);
    }

    public static List<MemoryDef> named() {
        return ALL.values().stream().filter(d -> d.named).toList();
    }

    /** The named Memory a creature of the novel leaves, by its lore id, if any. */
    @Nullable
    public static MemoryDef leftBy(String creature) {
        for (MemoryDef def : ALL.values()) {
            if (def.named && def.source.equals(creature)) {
                return def;
            }
        }

        return null;
    }

    /** A new Memory of a definition: a named one as the novel has it, a generic one plain until rolled. */
    public static MemoryData data(MemoryDef def, UUID owner) {
        return new MemoryData(UUID.randomUUID(), def.id, "", def.rank.ordinal(), def.tier, def.enchants.stream().map(Enchant::id).toList(), owner, 0);
    }

    /**
     * Weaves a Memory from slain prey: a shape that suits the kind of creature it was, its Rank (as a soul's) from the
     * creature's, its Tier from the creature's class, a name from both, and enchantments rolled for its Tier.
     */
    public static MemoryData weave(LivingEntity prey, UUID owner, RandomSource random) {
        Family family = Family.of(prey);
        MemoryDef def = shapeFor(prey, family, random);
        int rank = Creatures.rankOf(prey).ordinal();
        int tier = Creatures.classOf(prey).ordinal() + 1;
        return roll(def, family, rank, tier, owner, random);
    }

    /** Weaves a Memory for a kind of creature of a Rank and Tier, in a shape that suits it. */
    public static MemoryData weave(Family family, int rank, int tier, UUID owner, RandomSource random) {
        return roll(shapeFor(null, family, random), family, rank, tier, owner, random);
    }

    /** A generic Memory of a shape, Rank and Tier, named for a kind of creature, with its enchantments rolled. */
    public static MemoryData roll(MemoryDef def, Family family, int rank, int tier, UUID owner, RandomSource random) {
        List<String> enchants = new ArrayList<>(def.enchants.stream().map(Enchant::id).toList());
        List<Enchant> pool = new ArrayList<>(Enchant.pool(def.kind));
        Collections.shuffle(pool, new java.util.Random(random.nextLong()));
        int count = capacity(tier) - enchants.size();
        for (int i = 0; i < pool.size() && count > 0; i++) {
            enchants.add(pool.get(i).id());
            count--;
        }

        String name = family.id() + "." + random.nextInt(EPITHETS) + "|" + def.id + "." + random.nextInt(NOUNS);
        return new MemoryData(UUID.randomUUID(), def.id, name, rank, tier, enchants, owner, 0);
    }

    /** How many enchantments a Memory's weave can hold: more anchors for each Tier. */
    public static int capacity(int tier) {
        return 1 + (tier >= 3 ? 1 : 0) + (tier >= 5 ? 1 : 0) + (tier >= 7 ? 1 : 0);
    }

    /** How many epithets and nouns the language file has for each family and shape. */
    public static final int EPITHETS = 5;
    public static final int NOUNS = 4;

    /** "Hollow Fang": a generated Memory's name, from the kind of creature it came from and its shape. */
    public static Component name(MemoryData data) {
        MemoryDef def = byId(data.def());
        if (data.name().isEmpty() || def == null) {
            return def != null ? def.title() : Component.literal(data.def());
        }

        String[] parts = data.name().split("\\|");
        if (parts.length != 2) {
            return def.title();
        }

        return Component.translatable("memory.nightmare_evolved.generic_name", Component.translatable("memory.nightmare_evolved.epithet." + parts[0]),
            Component.translatable("memory.nightmare_evolved.noun." + parts[1]));
    }

    private static MemoryDef shapeFor(@Nullable LivingEntity prey, Family family, RandomSource random) {
        if (prey != null && (prey.getMainHandItem().getItem() instanceof BowItem || prey.getMainHandItem().getItem() instanceof CrossbowItem)) {
            return random.nextBoolean() ? BOW : DAGGER;
        }

        List<MemoryDef> choices = switch (family) {
            case UNDEAD -> List.of(BLADE, AXE, BONE_MAIL, SHIELD, LANTERN);
            case ARTHROPOD -> List.of(DAGGER, CLOAK, CHITIN_MAIL, CHARM);
            case BEAST -> List.of(SPEAR, BOW, CLOAK, HORN);
            case AQUATIC -> List.of(SPEAR, CHARM, CANTEEN);
            case FIRE -> List.of(LANTERN, CHARM, BLADE);
            case ENDER -> List.of(CHARM, LANTERN, DAGGER, CLOAK);
            case SPIRIT -> List.of(CLOAK, CHARM, LANTERN);
            case CONSTRUCT -> List.of(SHIELD, PLATE_MAIL, AXE);
            case HUMAN -> List.of(BLADE, BOW, HORN, CANTEEN, PLATE_MAIL);
            case STRANGE -> List.of(CHARM, CANTEEN, HORN, BLADE);
        };
        return choices.get(random.nextInt(choices.size()));
    }
}
