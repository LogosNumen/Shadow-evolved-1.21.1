package net.logosnumen.nightmareevolved.soul;

/**
 * The numbers behind a soul: how much essence each rank holds, how much a soul core takes to saturate, what a shard is
 * worth, and how much stronger each rank makes the body. Kept in one place so they are easy to tune.
 */
public final class SoulRules {
    private static final double[] ESSENCE = {0, 20, 50, 100, 200, 400, 800, 1600};
    private static final double[] HEALTH = {0, 0, 4, 10, 20, 34, 52, 76};
    private static final double[] DAMAGE = {0, 0, 1, 2.5, 4.5, 7, 10, 14};
    private static final double[] ARMOR = {0, 0, 1, 3, 5, 8, 11, 15};
    private static final double[] SPEED = {0, 0, 0.02, 0.05, 0.08, 0.12, 0.16, 0.2};
    /** Shards of a soul's own rank it takes to saturate one of its cores. */
    public static final int SHARDS_PER_CORE = 12;

    private SoulRules() {
    }

    /** Essence a soul holds at a rank, raised by a rarer Aspect. */
    public static double maxEssence(Rank rank, AspectTier tier) {
        return ESSENCE[rank.ordinal()] * tier.essence;
    }

    /** Essence a soul recovers each second: a hundredth of what it holds. */
    public static double regenPerSecond(Rank rank, AspectTier tier) {
        return maxEssence(rank, tier) * 0.01;
    }

    /** What a shard of a creature rank (Dormant 0 to Unholy 6) adds to saturation: each rank is worth twice the last. */
    public static double shardValue(int creatureRank) {
        return Math.pow(2.0, Math.max(0, creatureRank));
    }

    /** Saturation one core holds at a rank: a dozen shards of the rank's own creatures. */
    public static double coreCapacity(Rank rank) {
        return SHARDS_PER_CORE * shardValue(Math.max(0, rank.ladder));
    }

    /** Saturation all a rank's cores hold together; full means the soul is ready for its next trial. */
    public static double capacity(Rank rank) {
        return rank.cores * coreCapacity(rank);
    }

    public static double health(Rank rank) {
        return HEALTH[rank.ordinal()];
    }

    public static double damage(Rank rank) {
        return DAMAGE[rank.ordinal()];
    }

    public static double armor(Rank rank) {
        return ARMOR[rank.ordinal()];
    }

    public static double speed(Rank rank) {
        return SPEED[rank.ordinal()];
    }
}
