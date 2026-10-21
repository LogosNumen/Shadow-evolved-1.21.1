package net.logosnumen.nightmareevolved.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.logosnumen.nightmareevolved.soul.Tier;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * One kind of Memory: a named Memory of the novel (Midnight Shard, Puppeteer's Shroud and the rest), or one of the
 * shapes the Spell weaves for creatures it has no particular Memory for. A definition says what kind of thing the
 * Memory is, how it is carried, its usual Rank and Tier, and its enchantments; a generated Memory rolls its own
 * enchantments and takes its Rank and Tier from its prey.
 */
public final class MemoryDef {
    /** What the Spell calls the Memory's type in its runes. */
    public enum Kind {
        WEAPON,
        ARMOR,
        CHARM,
        TOOL,
        GARMENT,
        LINEAGE;

        public Component title() {
            return Component.translatable("memory.nightmare_evolved.kind." + this.name().toLowerCase(Locale.ROOT));
        }
    }

    /** How a summoned Memory sits with its owner. */
    public enum Carry {
        /** Appears in the hand, as an item. */
        HAND,
        /** Is put on, one layer for each armour slot. */
        ARMOR,
        /** Is worn without taking a slot: a charm at the throat, a garment over armour, a crown, a mask, a ring. */
        WORN,
        /** Appears in the hand to be consumed. */
        CONSUMED
    }

    /** The form a Memory's item takes, and for weapons how hard and fast they strike and how far they reach. */
    public enum Shape {
        TACHI(4.5, -2.3, 0.0),
        LONGSWORD(4.5, -2.5, 0.0),
        BLADE(4.0, -2.4, 0.0),
        STILETTO(2.5, -1.4, 0.0),
        DAGGER(2.5, -1.6, 0.0),
        SPEAR(4.0, -2.8, 1.0),
        GREATSWORD(7.0, -3.0, 0.5),
        WAR_HAMMER(6.5, -3.1, 0.0),
        MALLET(4.0, -2.9, 0.0),
        AXE(6.0, -3.0, 0.0),
        BOW(0.0, 0.0, 0.0),
        ARROW(0.0, 0.0, 0.0),
        SHIELD(0.0, 0.0, 0.0),
        ARMOR(0.0, 0.0, 0.0),
        THING(0.0, 0.0, 0.0);

        public final double damage;
        public final double speed;
        public final double reach;

        Shape(double damage, double speed, double reach) {
            this.damage = damage;
            this.speed = speed;
            this.reach = reach;
        }

        public boolean melee() {
            return this.damage > 0.0;
        }
    }

    public final String id;
    public final Kind kind;
    public final Carry carry;
    public final Shape shape;
    public final Tier rank;
    public final int tier;
    public final List<Enchant> enchants;
    /** Armour layers, head to feet; empty for anything else. */
    public final List<EquipmentSlot> layers;
    /** A Memory the novel names; false for the shapes the Spell weaves for anonymous prey. */
    public final boolean named;
    /** One of the seven Shard Memories of the Forgotten Shore, which outlive their owners. */
    public final boolean shard;
    /** The novel shows the Memory but not its powers, so the mod gives it some in its spirit. */
    public final boolean filled;
    /** The creature that leaves it behind, by its lore id, or empty if the novel doesn't say. */
    public final String source;
    public final Vfx.Palette palette;

    private MemoryDef(Builder b) {
        this.id = b.id;
        this.kind = b.kind;
        this.carry = b.carry;
        this.shape = b.shape;
        this.rank = b.rank;
        this.tier = b.tier;
        this.enchants = List.copyOf(b.enchants);
        this.layers = List.copyOf(b.layers);
        this.named = b.named;
        this.shard = b.shard;
        this.filled = b.filled;
        this.source = b.source;
        this.palette = b.palette;
    }

    public static Builder named(String id, Kind kind, Shape shape, Tier rank, int tier) {
        return new Builder(id, kind, shape, rank, tier, true);
    }

    public static Builder generic(String id, Kind kind, Shape shape) {
        return new Builder(id, kind, shape, Tier.DORMANT, 1, false);
    }

    public Component title() {
        return Component.translatable("memory.nightmare_evolved." + this.id);
    }

    public Component description() {
        return Component.translatable("memory.nightmare_evolved." + this.id + ".desc");
    }

    public boolean armor() {
        return this.carry == Carry.ARMOR;
    }

    /** How many pieces the Memory has: one, or one per armour layer. */
    public int pieces() {
        return this.armor() ? this.layers.size() : 1;
    }

    public static final class Builder {
        private final String id;
        private final Kind kind;
        private final Shape shape;
        private final Tier rank;
        private final int tier;
        private final boolean named;
        private Carry carry;
        private final List<Enchant> enchants = new ArrayList<>();
        private final List<EquipmentSlot> layers = new ArrayList<>();
        private boolean shard;
        private boolean filled;
        private String source = "";
        private Vfx.Palette palette = Vfx.Palette.SOUL;

        private Builder(String id, Kind kind, Shape shape, Tier rank, int tier, boolean named) {
            this.id = id;
            this.kind = kind;
            this.shape = shape;
            this.rank = rank;
            this.tier = tier;
            this.named = named;
            this.carry = switch (kind) {
                case ARMOR -> Carry.ARMOR;
                case CHARM, GARMENT -> Carry.WORN;
                case LINEAGE -> Carry.CONSUMED;
                default -> Carry.HAND;
            };
            if (kind == Kind.ARMOR) {
                this.layers.addAll(List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET));
            }
        }

        public Builder with(Enchant... enchants) {
            this.enchants.addAll(List.of(enchants));
            return this;
        }

        /** Worn rather than held, though the runes call it a tool: a crown, a mask, a ring. */
        public Builder worn() {
            this.carry = Carry.WORN;
            return this;
        }

        /** The armour layers this Memory has, if not all four. */
        public Builder layers(EquipmentSlot... layers) {
            this.layers.clear();
            this.layers.addAll(List.of(layers));
            return this;
        }

        public Builder shard() {
            this.shard = true;
            return this;
        }

        public Builder filled() {
            this.filled = true;
            return this;
        }

        public Builder from(String source) {
            this.source = source;
            return this;
        }

        public Builder palette(Vfx.Palette palette) {
            this.palette = palette;
            return this;
        }

        public MemoryDef build() {
            return new MemoryDef(this);
        }
    }
}
