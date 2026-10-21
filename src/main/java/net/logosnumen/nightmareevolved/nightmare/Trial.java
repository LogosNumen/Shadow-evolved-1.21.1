package net.logosnumen.nightmareevolved.nightmare;

import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.logosnumen.nightmareevolved.colossus.ColossusKind;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * The trials the Spell sets, one for each step up the ladder. The First Nightmare makes an Aspirant a Sleeper; the
 * Awakening, which is no Nightmare but the Dream Realm itself (a Sleeper must find their way to a Gateway), makes a
 * Sleeper Awakened; each Nightmare after it, reached by challenging a Seed of Nightmare of its Category, raises the soul
 * again. Each Nightmare has its waves of Nightmare Creatures, a guardian (colossi guard the deepest), and a look of its
 * own.
 */
public enum Trial {
    FIRST_NIGHTMARE(Rank.ASPIRANT, Objective.SLAY, CreatureRank.DORMANT, CreatureRank.AWAKENED, 3,
        List.of(EntityType.ZOMBIE, EntityType.HUSK, EntityType.SPIDER, EntityType.SKELETON), EntityType.ZOMBIE, null,
        new Theme(Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, Blocks.STONE, Blocks.STONE_BRICKS, Blocks.SOUL_LANTERN)),
    AWAKENING(Rank.SLEEPER, Objective.GATEWAY, CreatureRank.DORMANT, CreatureRank.AWAKENED, 0,
        List.of(EntityType.ZOMBIE, EntityType.SPIDER, EntityType.SKELETON, EntityType.CAVE_SPIDER), null, null,
        new Theme(Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS, Blocks.SOUL_LANTERN)),
    SECOND_NIGHTMARE(Rank.AWAKENED, Objective.SLAY, CreatureRank.AWAKENED, CreatureRank.FALLEN, 3,
        List.of(EntityType.VINDICATOR, EntityType.PILLAGER, EntityType.HUSK, EntityType.STRAY, EntityType.SPIDER), EntityType.EVOKER, null,
        new Theme(Blocks.DEEPSLATE, Blocks.COBBLED_DEEPSLATE, Blocks.TUFF, Blocks.DEEPSLATE_BRICKS, Blocks.SOUL_LANTERN)),
    THIRD_NIGHTMARE(Rank.MASTER, Objective.SLAY, CreatureRank.FALLEN, CreatureRank.CORRUPTED, 3,
        List.of(EntityType.WITHER_SKELETON, EntityType.PIGLIN_BRUTE, EntityType.VINDICATOR, EntityType.BLAZE), EntityType.RAVAGER, null,
        new Theme(Blocks.BLACKSTONE, Blocks.BASALT, Blocks.POLISHED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.LANTERN)),
    FOURTH_NIGHTMARE(Rank.SAINT, Objective.SLAY, CreatureRank.CORRUPTED, CreatureRank.GREAT, 3,
        List.of(EntityType.WITHER_SKELETON, EntityType.RAVAGER, EntityType.VINDICATOR, EntityType.EVOKER), null, ColossusKind.CAIRN,
        new Theme(Blocks.CALCITE, Blocks.BONE_BLOCK, Blocks.DRIPSTONE_BLOCK, Blocks.SMOOTH_BASALT, Blocks.SOUL_LANTERN)),
    FIFTH_NIGHTMARE(Rank.SOVEREIGN, Objective.SLAY, CreatureRank.GREAT, CreatureRank.CURSED, 4,
        List.of(EntityType.RAVAGER, EntityType.WITHER_SKELETON, EntityType.PIGLIN_BRUTE, EntityType.EVOKER), null, ColossusKind.GRAVEWYRM,
        new Theme(Blocks.NETHERRACK, Blocks.CRIMSON_NYLIUM, Blocks.NETHER_WART_BLOCK, Blocks.RED_NETHER_BRICKS, Blocks.SHROOMLIGHT)),
    SIXTH_NIGHTMARE(Rank.SACRED, Objective.SLAY, CreatureRank.CURSED, CreatureRank.UNHOLY, 4,
        List.of(EntityType.RAVAGER, EntityType.WITHER_SKELETON, EntityType.EVOKER, EntityType.PIGLIN_BRUTE), null, ColossusKind.CHAINBOUND,
        new Theme(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.SCULK, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.SOUL_LANTERN));

    /**
     * What a trial asks: slay its waves and guardian, or (for the Awakening) find a way out of the Dream Realm through a
     * Gateway.
     */
    public enum Objective {
        SLAY,
        GATEWAY
    }

    /** The blocks a trial's ground and ruins are made of, and its lights. */
    public record Theme(Block ground, Block patch, Block deep, Block ruin, Block light) {
    }

    public final Rank from;
    public final Objective objective;
    public final CreatureRank creatures;
    public final CreatureRank guardianRank;
    public final int waves;
    public final List<EntityType<?>> pool;
    @Nullable
    public final EntityType<?> guardian;
    @Nullable
    public final ColossusKind colossus;
    public final Theme theme;

    Trial(Rank from, Objective objective, CreatureRank creatures, CreatureRank guardianRank, int waves, List<EntityType<?>> pool,
        @Nullable EntityType<?> guardian, @Nullable ColossusKind colossus, Theme theme) {
        this.from = from;
        this.objective = objective;
        this.creatures = creatures;
        this.guardianRank = guardianRank;
        this.waves = waves;
        this.pool = pool;
        this.guardian = guardian;
        this.colossus = colossus;
        this.theme = theme;
    }

    public Rank to() {
        return this.from.next();
    }

    /** The trial's position in the ladder, from 1 (the First Nightmare). */
    public int depth() {
        return this.ordinal() + 1;
    }

    /** The class a creature of this trial takes, by its place in the wave: deeper trials send fiercer kinds. */
    public CreatureClass classFor(int index) {
        CreatureClass[] all = CreatureClass.values();
        int low = Math.min(all.length - 3, this.ordinal() / 2);
        return all[Math.min(all.length - 2, low + index % 3)];
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public Component title() {
        return Component.translatable("trial.nightmare_evolved." + this.id());
    }

    /** The trial a soul of this rank faces next, or null once it is Divine. */
    @Nullable
    public static Trial next(Rank rank) {
        for (Trial trial : values()) {
            if (trial.from == rank) {
                return trial;
            }
        }

        return null;
    }
}
