package net.logosnumen.nightmareevolved.creature.kind;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.dream.Places;
import net.logosnumen.nightmareevolved.dream.Region;
import net.logosnumen.nightmareevolved.entity.creature.DreamCreature;
import net.logosnumen.nightmareevolved.vfx.Vfx;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * One kind of Nightmare Creature of the Dream Realm, as the novel describes it: its rank and class, its size and
 * strength, how it moves and fights, what sets it apart, where it lives, and (for the named ones) the lair it keeps.
 */
public final class Kind {
    /** How a creature gets about. */
    public enum Motion {
        WALK, CLIMB, FLY, HOVER, SWIM, ROOT
    }

    /** What sets a creature apart from the rest of its kind's rank and class. */
    public enum Trait {
        /** Only comes out at night. */
        NIGHT,
        /** Its kin come when one of them is struck. */
        PACK,
        /** Has no soul: leaves no shards and no Memory. */
        SOULLESS,
        /** Fire burns it twice as badly. */
        FIRE_WEAK,
        /** Fire does not touch it. */
        FIREPROOF,
        /** Takes no harm from falling. */
        LIGHT_FALL,
        /** Feeds on what it bites: heals by half of every wound it deals. */
        DRAIN
    }

    /** When a creature walks abroad. */
    public enum Time {
        ANY, DAY, NIGHT
    }

    /** Where in its region a creature is found: on the ground, in the air above it, or in water. */
    public enum Ground {
        SURFACE, AIR, WATER
    }

    /**
     * A place a creature haunts: its region, how often it turns up, how many come together, when and where, and (for
     * those that keep to one spot of a region) the place they keep near and how far from it they go.
     */
    public record Haunt(Region region, int weight, int min, int max, Time time, Ground ground, @Nullable Places.Place near, int radius) {
        public boolean reaches(double x, double z) {
            return this.near == null || Math.hypot(x - this.near.x(), z - this.near.z()) <= this.radius;
        }
    }

    /**
     * A named creature's lair: the place it keeps, how far off it waits from the place's centre, and the height it is
     * found at (or {@link #GROUND} for the open ground there).
     */
    public record Lair(Places.Place place, int dx, int dz, int y) {
        public static final int GROUND = Integer.MIN_VALUE;
    }

    /** Makes the entity for a kind. */
    @FunctionalInterface
    public interface Factory {
        DreamCreature create(EntityType<? extends DreamCreature> type, Level level, Kind kind);
    }

    public final String id;
    public final CreatureRank rank;
    public final CreatureClass creatureClass;
    public final Motion motion;
    public final float width;
    public final float height;
    public final double health;
    public final double damage;
    public final double armor;
    public final double speed;
    public final double follow;
    public final List<Move> moves;
    public final Set<Trait> traits;
    public final List<Haunt> haunts;
    /** What a landed blow leaves in its victim besides the wound. */
    public final List<Supplier<MobEffectInstance>> afflictions;
    @Nullable
    public final Lair lair;
    public final boolean lore;
    /** The kinds that answer this one's call besides its own. */
    public final Set<String> commands;
    public final int looks;
    public final Vfx.Palette palette;
    public final int eggBase;
    public final int eggSpots;
    public final Supplier<SoundEvent> ambient;
    public final Supplier<SoundEvent> hurt;
    public final Supplier<SoundEvent> death;
    public final Supplier<SoundEvent> step;
    /** What makes this kind's entity; null for the plain creature. */
    @Nullable
    public final Factory factory;

    private Kind(Builder b) {
        this.id = b.id;
        this.rank = b.rank;
        this.creatureClass = b.creatureClass;
        this.motion = b.motion;
        this.width = b.width;
        this.height = b.height;
        this.health = b.health;
        this.damage = b.damage;
        this.armor = b.armor;
        this.speed = b.speed;
        this.follow = b.follow;
        this.moves = List.copyOf(b.moves);
        this.traits = b.traits.isEmpty() ? Set.of() : Set.copyOf(b.traits);
        this.haunts = List.copyOf(b.haunts);
        this.afflictions = List.copyOf(b.afflictions);
        this.lair = b.lair;
        this.lore = b.lore;
        this.commands = Set.copyOf(b.commands);
        this.looks = b.looks;
        this.palette = b.palette;
        this.eggBase = b.eggBase;
        this.eggSpots = b.eggSpots;
        this.ambient = b.ambient;
        this.hurt = b.hurt;
        this.death = b.death;
        this.step = b.step;
        this.factory = b.factory;
    }

    public static Builder of(String id, CreatureRank rank, CreatureClass creatureClass) {
        return new Builder(id, rank, creatureClass);
    }

    public boolean has(Trait trait) {
        return this.traits.contains(trait);
    }

    /** Whether a creature of another kind comes when this one calls: its own kind, or one it commands. */
    public boolean answers(Kind other) {
        return other == this || this.commands.contains(other.id);
    }

    @Override
    public String toString() {
        return this.id;
    }

    public static final class Builder {
        private final String id;
        private final CreatureRank rank;
        private final CreatureClass creatureClass;
        private Motion motion = Motion.WALK;
        private float width = 0.8F;
        private float height = 1.8F;
        private double health = 30.0;
        private double damage = 5.0;
        private double armor = 2.0;
        private double speed = 0.25;
        private double follow = 32.0;
        private final List<Move> moves = new ArrayList<>();
        private final Set<Trait> traits = EnumSet.noneOf(Trait.class);
        private final List<Haunt> haunts = new ArrayList<>();
        private final List<Supplier<MobEffectInstance>> afflictions = new ArrayList<>();
        @Nullable
        private Lair lair;
        private boolean lore;
        private final Set<String> commands = new HashSet<>();
        private int looks = 1;
        private Vfx.Palette palette = Vfx.Palette.SHADOW;
        private int eggBase = 0x303030;
        private int eggSpots = 0xA02020;
        private Supplier<SoundEvent> ambient = () -> SoundEvents.SPIDER_AMBIENT;
        private Supplier<SoundEvent> hurt = () -> SoundEvents.SPIDER_HURT;
        private Supplier<SoundEvent> death = () -> SoundEvents.SPIDER_DEATH;
        private Supplier<SoundEvent> step = () -> SoundEvents.SPIDER_STEP;
        @Nullable
        private Factory factory;

        private Builder(String id, CreatureRank rank, CreatureClass creatureClass) {
            this.id = id;
            this.rank = rank;
            this.creatureClass = creatureClass;
        }

        public Builder motion(Motion motion) {
            this.motion = motion;
            return this;
        }

        public Builder size(float width, float height) {
            this.width = width;
            this.height = height;
            return this;
        }

        /** Health, attack damage, armour and walking speed, before anything its rank adds. */
        public Builder stats(double health, double damage, double armor, double speed) {
            this.health = health;
            this.damage = damage;
            this.armor = armor;
            this.speed = speed;
            return this;
        }

        public Builder follow(double follow) {
            this.follow = follow;
            return this;
        }

        public Builder move(Move move) {
            this.moves.add(move);
            return this;
        }

        public Builder traits(Trait... traits) {
            this.traits.addAll(List.of(traits));
            return this;
        }

        public Builder haunt(Region region, int weight, int min, int max) {
            return this.haunt(region, weight, min, max, Time.ANY, this.motion == Motion.FLY ? Ground.AIR : this.motion == Motion.SWIM ? Ground.WATER : Ground.SURFACE);
        }

        public Builder haunt(Region region, int weight, int min, int max, Time time, Ground ground) {
            this.haunts.add(new Haunt(region, weight, min, max, time, ground, null, 0));
            return this;
        }

        /** A haunt kept to within {@code radius} blocks of a place. */
        public Builder hauntNear(Region region, Places.Place near, int radius, int weight, int min, int max, Time time) {
            Ground ground = this.motion == Motion.FLY ? Ground.AIR : this.motion == Motion.SWIM ? Ground.WATER : Ground.SURFACE;
            this.haunts.add(new Haunt(region, weight, min, max, time, ground, near, radius));
            return this;
        }

        public Builder lair(Places.Place place, int dx, int dz) {
            this.lair = new Lair(place, dx, dz, Lair.GROUND);
            return this;
        }

        /** A lair at a height of its own: inside a tower, high in the air. */
        public Builder lair(Places.Place place, int dx, int dz, int y) {
            this.lair = new Lair(place, dx, dz, y);
            return this;
        }

        /** A landed blow leaves this in its victim. */
        public Builder afflicts(Supplier<MobEffectInstance> affliction) {
            this.afflictions.add(affliction);
            return this;
        }

        /** The creature is one the novel names: a Memory of its own may be left by it. */
        public Builder lore() {
            this.lore = true;
            return this;
        }

        /** Kinds that answer this one's call. */
        public Builder commands(String... kinds) {
            this.commands.addAll(List.of(kinds));
            return this;
        }

        public Builder looks(int looks) {
            this.looks = looks;
            return this;
        }

        public Builder palette(Vfx.Palette palette) {
            this.palette = palette;
            return this;
        }

        public Builder egg(int base, int spots) {
            this.eggBase = base;
            this.eggSpots = spots;
            return this;
        }

        public Builder sounds(Supplier<SoundEvent> ambient, Supplier<SoundEvent> hurt, Supplier<SoundEvent> death, Supplier<SoundEvent> step) {
            this.ambient = ambient;
            this.hurt = hurt;
            this.death = death;
            this.step = step;
            return this;
        }

        public Builder factory(Factory factory) {
            this.factory = factory;
            return this;
        }

        public Kind build() {
            return new Kind(this);
        }
    }
}
